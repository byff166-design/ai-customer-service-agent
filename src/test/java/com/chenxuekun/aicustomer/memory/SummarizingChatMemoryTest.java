package com.chenxuekun.aicustomer.memory;

import com.chenxuekun.aicustomer.entity.ConversationSummary;
import com.chenxuekun.aicustomer.service.ConversationMemoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.timeout;

class SummarizingChatMemoryTest {

    @Test
    void shouldCompactOldMessagesAndPersistSummary() {
        ConversationMemoryService memoryService = mock(ConversationMemoryService.class);
        when(memoryService.find("SESSION-1", "SESSION-1")).thenReturn(Optional.empty());
        ConversationSummarizer summarizer = (existing, messages) -> "用户询问过订单物流，问题已处理";
        SummarizingChatMemory memory = new SummarizingChatMemory(
                "SESSION-1", 40, 24, 10, memoryService, summarizer);

        for (int index = 0; index < 12; index++) {
            memory.add(UserMessage.from("问题" + index));
            memory.add(AiMessage.from("回答" + index));
        }

        assertThat(memory.summary()).isEqualTo("用户询问过订单物流，问题已处理");
        assertThat(memory.messages()).hasSize(10);
        verify(memoryService).save("SESSION-1", "SESSION-1", "用户询问过订单物流，问题已处理", 14);
    }

    @Test
    void shouldRestorePersistedSummary() {
        ConversationMemoryService memoryService = mock(ConversationMemoryService.class);
        ConversationSummary stored = new ConversationSummary();
        stored.setSessionId("SESSION-2");
        stored.setSummary("已有摘要");
        stored.setSummarizedMessageCount(8L);
        when(memoryService.find("SESSION-2", "SESSION-2")).thenReturn(Optional.of(stored));

        SummarizingChatMemory memory = new SummarizingChatMemory(
                "SESSION-2", 40, 24, 10, memoryService, (existing, messages) -> "新摘要");

        assertThat(memory.summary()).isEqualTo("已有摘要");
    }

    @Test
    void shouldCompactAsynchronouslyWithoutBlockingRequestThread() throws Exception {
        ConversationMemoryService memoryService = mock(ConversationMemoryService.class);
        when(memoryService.find("CUSTOMER-1", "SESSION-3")).thenReturn(Optional.empty());
        CountDownLatch summarizerStarted = new CountDownLatch(1);
        CountDownLatch releaseSummarizer = new CountDownLatch(1);
        ConversationSummarizer summarizer = (existing, messages) -> {
            summarizerStarted.countDown();
            try {
                releaseSummarizer.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            return "异步摘要";
        };
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            SummarizingChatMemory memory = new SummarizingChatMemory(
                    "CUSTOMER-1|SESSION-3", 100, 24, 10, memoryService, summarizer, executor);
            for (int index = 0; index < 12; index++) {
                memory.add(UserMessage.from("问题" + index));
                memory.add(AiMessage.from("回答" + index));
            }

            assertThat(summarizerStarted.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(memory.summary()).isEmpty();
            releaseSummarizer.countDown();
            verify(memoryService, timeout(1000)).save("CUSTOMER-1", "SESSION-3", "异步摘要", 14);
        } finally {
            releaseSummarizer.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void shouldKeepMessagesWhenAsynchronousSummaryFails() {
        ConversationMemoryService memoryService = mock(ConversationMemoryService.class);
        when(memoryService.find("CUSTOMER-2", "SESSION-4")).thenReturn(Optional.empty());
        SummarizingChatMemory memory = new SummarizingChatMemory(
                "CUSTOMER-2|SESSION-4", 100, 24, 10, memoryService,
                (existing, messages) -> { throw new IllegalStateException("model unavailable"); },
                Runnable::run);

        for (int index = 0; index < 12; index++) {
            memory.add(UserMessage.from("问题" + index));
            memory.add(AiMessage.from("回答" + index));
        }

        assertThat(memory.messages()).hasSize(24);
        assertThat(memory.summary()).isEmpty();
    }
}
