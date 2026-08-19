package com.chenxuekun.aicustomer.memory;

import com.chenxuekun.aicustomer.service.ConversationMemoryService;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

public class PersistentChatMemoryProvider implements ChatMemoryProvider {
    private final ConcurrentMap<Object, ChatMemory> memories = new ConcurrentHashMap<>();
    private final ConversationMemoryService memoryService;
    private final ConversationSummarizer summarizer;
    private final Executor compactionExecutor;

    public PersistentChatMemoryProvider(ConversationMemoryService memoryService,
                                        ConversationSummarizer summarizer,
                                        Executor compactionExecutor) {
        this.memoryService = memoryService;
        this.summarizer = summarizer;
        this.compactionExecutor = compactionExecutor;
    }

    @Override
    public ChatMemory get(Object memoryId) {
        return memories.computeIfAbsent(memoryId,
                id -> new SummarizingChatMemory(id, 100, 24, 12, memoryService, summarizer, compactionExecutor));
    }
}
