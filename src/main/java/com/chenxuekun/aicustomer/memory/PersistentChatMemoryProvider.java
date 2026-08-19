package com.chenxuekun.aicustomer.memory;

import com.chenxuekun.aicustomer.service.ConversationMemoryService;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PersistentChatMemoryProvider implements ChatMemoryProvider {
    private final ConcurrentMap<Object, ChatMemory> memories = new ConcurrentHashMap<>();
    private final ConversationMemoryService memoryService;
    private final ConversationSummarizer summarizer;

    public PersistentChatMemoryProvider(ConversationMemoryService memoryService,
                                        ConversationSummarizer summarizer) {
        this.memoryService = memoryService;
        this.summarizer = summarizer;
    }

    @Override
    public ChatMemory get(Object memoryId) {
        return memories.computeIfAbsent(memoryId,
                id -> new SummarizingChatMemory(id, 40, 24, 10, memoryService, summarizer));
    }
}
