package com.chenxuekun.aicustomer.memory;

import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

@FunctionalInterface
public interface ConversationSummarizer {
    String summarize(String existingSummary, List<ChatMessage> messages);
}
