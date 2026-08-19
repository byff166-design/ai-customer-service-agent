package com.chenxuekun.aicustomer.memory;

import com.chenxuekun.aicustomer.entity.ConversationSummary;
import com.chenxuekun.aicustomer.service.ConversationMemoryService;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.util.ArrayList;
import java.util.List;

public class SummarizingChatMemory implements ChatMemory {
    private final Object id;
    private final MessageWindowChatMemory delegate;
    private final ConversationMemoryService memoryService;
    private final ConversationSummarizer summarizer;
    private final int compactThreshold;
    private final int retainedMessages;

    private String summary;
    private long summarizedMessageCount;

    public SummarizingChatMemory(Object id,
                                 int maxMessages,
                                 int compactThreshold,
                                 int retainedMessages,
                                 ConversationMemoryService memoryService,
                                 ConversationSummarizer summarizer) {
        if (compactThreshold >= maxMessages || retainedMessages >= compactThreshold) {
            throw new IllegalArgumentException("会话记忆阈值配置不合法");
        }
        this.id = id;
        this.delegate = MessageWindowChatMemory.withMaxMessages(maxMessages);
        this.memoryService = memoryService;
        this.summarizer = summarizer;
        this.compactThreshold = compactThreshold;
        this.retainedMessages = retainedMessages;
        memoryService.find(String.valueOf(id)).ifPresent(this::restore);
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
        memoryService.delete(String.valueOf(id));
    }

    public synchronized String summary() {
        return summary == null ? "" : summary;
    }

    private void compactIfNecessary() {
        List<ChatMessage> messages = delegate.messages();
        if (messages.size() < compactThreshold) {
            return;
        }
        int retainFrom = Math.max(1, messages.size() - retainedMessages);
        while (retainFrom < messages.size() && !(messages.get(retainFrom) instanceof UserMessage)) {
            retainFrom++;
        }
        if (retainFrom >= messages.size()) {
            return;
        }
        List<ChatMessage> compacted = new ArrayList<>(messages.subList(0, retainFrom));
        List<ChatMessage> retained = new ArrayList<>(messages.subList(retainFrom, messages.size()));
        String updated = summarizer.summarize(summary, List.copyOf(compacted));
        if (updated == null || updated.isBlank()) {
            return;
        }
        summary = updated.trim();
        summarizedMessageCount += compacted.size();
        memoryService.save(String.valueOf(id), summary, summarizedMessageCount);
        delegate.clear();
        retained.forEach(delegate::add);
    }

    private void restore(ConversationSummary stored) {
        summary = stored.getSummary();
        summarizedMessageCount = stored.getSummarizedMessageCount() == null
                ? 0 : stored.getSummarizedMessageCount();
    }
}
