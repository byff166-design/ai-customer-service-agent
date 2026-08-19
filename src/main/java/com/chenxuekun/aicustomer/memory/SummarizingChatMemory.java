package com.chenxuekun.aicustomer.memory;

import com.chenxuekun.aicustomer.entity.ConversationSummary;
import com.chenxuekun.aicustomer.service.ConversationMemoryService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class SummarizingChatMemory implements ChatMemory {
    private final Object id;
    private final MessageWindowChatMemory delegate;
    private final ConversationMemoryService memoryService;
    private final ConversationSummarizer summarizer;
    private final int compactThreshold;
    private final int retainedMessages;
    private final Executor compactionExecutor;
    private final AtomicBoolean compactionInFlight = new AtomicBoolean();
    private final AtomicLong generation = new AtomicLong();
    private final String customerId;
    private final String sessionId;

    private volatile String summary;
    private long summarizedMessageCount;

    public SummarizingChatMemory(Object id,
                                 int maxMessages,
                                 int compactThreshold,
                                 int retainedMessages,
                                 ConversationMemoryService memoryService,
                                 ConversationSummarizer summarizer) {
        this(id, maxMessages, compactThreshold, retainedMessages, memoryService, summarizer, Runnable::run);
    }

    public SummarizingChatMemory(Object id,
                                 int maxMessages,
                                 int compactThreshold,
                                 int retainedMessages,
                                 ConversationMemoryService memoryService,
                                 ConversationSummarizer summarizer,
                                 Executor compactionExecutor) {
        if (compactThreshold >= maxMessages || retainedMessages >= compactThreshold) {
            throw new IllegalArgumentException("会话记忆阈值配置不合法");
        }
        this.id = id;
        this.delegate = MessageWindowChatMemory.withMaxMessages(maxMessages);
        this.memoryService = memoryService;
        this.summarizer = summarizer;
        this.compactThreshold = compactThreshold;
        this.retainedMessages = retainedMessages;
        this.compactionExecutor = compactionExecutor;
        String[] identity = parseIdentity(id);
        this.customerId = identity[0];
        this.sessionId = identity[1];
        memoryService.find(customerId, sessionId).ifPresent(this::restore);
    }

    @Override
    public Object id() {
        return id;
    }

    @Override
    public synchronized void add(ChatMessage message) {
        delegate.add(message);
        compactIfNecessary();
    }

    @Override
    public synchronized List<ChatMessage> messages() {
        return delegate.messages();
    }

    @Override
    public synchronized void clear() {
        delegate.clear();
        summary = null;
        summarizedMessageCount = 0;
        generation.incrementAndGet();
        memoryService.delete(customerId, sessionId);
    }

    public synchronized String summary() {
        return summary == null ? "" : summary;
    }

    private synchronized void compactIfNecessary() {
        if (!compactionInFlight.compareAndSet(false, true)) {
            return;
        }
        List<ChatMessage> messages = delegate.messages();
        if (messages.size() < compactThreshold) {
            compactionInFlight.set(false);
            return;
        }
        int retainFrom = Math.max(1, messages.size() - retainedMessages);
        while (retainFrom < messages.size() && !(messages.get(retainFrom) instanceof UserMessage)) {
            retainFrom++;
        }
        if (retainFrom >= messages.size()) {
            compactionInFlight.set(false);
            return;
        }
        List<ChatMessage> compacted = new ArrayList<>(messages.subList(0, retainFrom));
        List<ChatMessage> retained = new ArrayList<>(messages.subList(retainFrom, messages.size()));
        delegate.clear();
        retained.forEach(delegate::add);
        String previousSummary = summary;
        long previousCount = summarizedMessageCount;
        long currentGeneration = generation.get();
        try {
            compactionExecutor.execute(() -> compactAsync(
                    previousSummary, previousCount, List.copyOf(compacted), currentGeneration));
        } catch (RuntimeException exception) {
            compactionInFlight.set(false);
            throw exception;
        }
    }

    private void compactAsync(String previousSummary,
                              long previousCount,
                              List<ChatMessage> compacted,
                              long expectedGeneration) {
        boolean compactedSuccessfully = false;
        try {
            String updated = summarizer.summarize(previousSummary, compacted);
            synchronized (this) {
                if (generation.get() != expectedGeneration) {
                    return;
                }
                if (updated == null || updated.isBlank()) {
                    restoreCompactedMessages(compacted);
                    return;
                }
                String nextSummary = updated.trim();
                long nextCount = previousCount + compacted.size();
                memoryService.save(customerId, sessionId, nextSummary, nextCount);
                summary = nextSummary;
                summarizedMessageCount = nextCount;
                compactedSuccessfully = true;
            }
        } catch (RuntimeException exception) {
            synchronized (this) {
                if (generation.get() == expectedGeneration) {
                    restoreCompactedMessages(compacted);
                }
            }
        } finally {
            compactionInFlight.set(false);
            if (compactedSuccessfully) {
                compactIfNecessary();
            }
        }
    }

    private void restoreCompactedMessages(List<ChatMessage> compacted) {
        List<ChatMessage> current = new ArrayList<>(delegate.messages());
        delegate.clear();
        compacted.forEach(delegate::add);
        current.forEach(delegate::add);
    }

    private void restore(ConversationSummary stored) {
        summary = stored.getSummary();
        summarizedMessageCount = stored.getSummarizedMessageCount() == null
                ? 0 : stored.getSummarizedMessageCount();
    }

    private String[] parseIdentity(Object memoryId) {
        String value = String.valueOf(memoryId);
        int separator = value.indexOf('|');
        if (separator <= 0 || separator == value.length() - 1) {
            return new String[]{value, value};
        }
        return new String[]{value.substring(0, separator), value.substring(separator + 1)};
    }
}
