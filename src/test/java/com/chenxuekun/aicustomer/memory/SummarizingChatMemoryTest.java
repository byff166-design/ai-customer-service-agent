package com.chenxuekun.aicustomer.memory;

import com.chenxuekun.aicustomer.entity.ConversationSummary;
import com.chenxuekun.aicustomer.service.ConversationMemoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SummarizingChatMemoryTest {

    @Test
    void shouldCompactOldMessagesAndPersistSummary() {
        ConversationMemoryService memoryService = mock(ConversationMemoryService.class);
        when(memoryService.find("SESSION-1")).thenReturn(Optional.empty());
        ConversationSummarizer summarizer = (existing, messages) -> "用户询问过订单物流，问题已处理";
        SummarizingChatMemory memory = new SummarizingChatMemory(
                "SESSION-1", 40, 24, 10, memoryService, summarizer);

        for (int index = 0; index < 12; index++) {
            memory.add(UserMessage.from("问题" + index));
            memory.add(AiMessage.from("回答" + index));
        }

        assertThat(memory.summary()).isEqualTo("用户询问过订单物流，问题已处理");
        assertThat(memory.messages()).hasSize(10);
        verify(memoryService).save("SESSION-1", "用户询问过订单物流，问题已处理", 14);
    }

    @Test
    void shouldRestorePersistedSummary() {
        ConversationMemoryService memoryService = mock(ConversationMemoryService.class);
        ConversationSummary stored = new ConversationSummary();
        stored.setSessionId("SESSION-2");
        stored.setSummary("已有摘要");
        stored.setSummarizedMessageCount(8L);
        when(memoryService.find("SESSION-2")).thenReturn(Optional.of(stored));

        SummarizingChatMemory memory = new SummarizingChatMemory(
                "SESSION-2", 40, 24, 10, memoryService, (existing, messages) -> "新摘要");

        assertThat(memory.summary()).isEqualTo("已有摘要");
    }
}
