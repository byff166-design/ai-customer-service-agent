package com.chenxuekun.aicustomer.service;

import com.chenxuekun.aicustomer.entity.ConversationSummary;
import com.chenxuekun.aicustomer.mapper.ConversationSummaryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ConversationMemoryServiceIntegrationTest {
    @Autowired
    private ConversationMemoryService memoryService;
    @Autowired
    private ConversationSummaryMapper mapper;

    @BeforeEach
    void cleanSummaries() {
        mapper.delete(null);
    }

    @Test
    void shouldPersistAndUpdateSummary() {
        memoryService.save("MEMORY-1", "第一次摘要", 12);
        memoryService.save("MEMORY-1", "更新后的摘要", 24);

        ConversationSummary stored = memoryService.find("MEMORY-1").orElseThrow();
        assertThat(stored.getSummary()).isEqualTo("更新后的摘要");
        assertThat(stored.getSummarizedMessageCount()).isEqualTo(24);
        assertThat(stored.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldDeleteSummaryWhenMemoryIsCleared() {
        memoryService.save("MEMORY-2", "待删除摘要", 6);

        memoryService.delete("MEMORY-2");

        assertThat(memoryService.find("MEMORY-2")).isEmpty();
    }
}
