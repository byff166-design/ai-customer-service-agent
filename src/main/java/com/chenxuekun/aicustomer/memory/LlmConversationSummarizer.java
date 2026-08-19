package com.chenxuekun.aicustomer.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatModel;

import java.util.List;

public class LlmConversationSummarizer implements ConversationSummarizer {
    private final ChatModel chatModel;

    public LlmConversationSummarizer(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public String summarize(String existingSummary, List<ChatMessage> messages) {
        StringBuilder prompt = new StringBuilder("""
                你是客服会话压缩器。请把历史对话压缩为不超过600字的事实摘要。
                仅保留订单号、工单号、用户诉求、已执行操作、处理结果和未解决事项。
                不得添加原对话中不存在的内容，不要输出标题或解释。
                """);
        if (existingSummary != null && !existingSummary.isBlank()) {
            prompt.append("\n已有摘要：\n").append(existingSummary);
        }
        prompt.append("\n待压缩消息：\n");
        for (ChatMessage message : messages) {
            prompt.append(message.type()).append(": ").append(message).append('\n');
        }
        return chatModel.chat(prompt.toString()).trim();
    }
}
