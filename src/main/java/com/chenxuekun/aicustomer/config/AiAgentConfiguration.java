package com.chenxuekun.aicustomer.config;

import com.chenxuekun.aicustomer.agent.AiCustomerAssistant;
import com.chenxuekun.aicustomer.agent.CustomerServiceAgent;
import com.chenxuekun.aicustomer.agent.LangChainCustomerServiceAgent;
import com.chenxuekun.aicustomer.agent.tool.CustomerServiceTools;
import com.chenxuekun.aicustomer.memory.ConversationSummarizer;
import com.chenxuekun.aicustomer.memory.LlmConversationSummarizer;
import com.chenxuekun.aicustomer.memory.PersistentChatMemoryProvider;
import com.chenxuekun.aicustomer.service.ConversationMemoryService;
import com.chenxuekun.aicustomer.service.SessionContextService;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiAgentConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.ai.mode", havingValue = "ai")
    ChatModel dashScopeChatModel(AiProperties properties) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException("AI 模式需要设置环境变量 DASHSCOPE_API_KEY");
        }
        return OpenAiChatModel.builder()
                .baseUrl(properties.getBaseUrl())
                .apiKey(properties.getApiKey())
                .modelName(properties.getModelName())
                .timeout(Duration.ofSeconds(properties.getTimeoutSeconds()))
                .logRequests(false)
                .logResponses(false)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.mode", havingValue = "ai")
    ConversationSummarizer conversationSummarizer(ChatModel chatModel) {
        return new LlmConversationSummarizer(chatModel);
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.mode", havingValue = "ai")
    ChatMemoryProvider chatMemoryProvider(ConversationMemoryService memoryService,
                                          ConversationSummarizer summarizer) {
        return new PersistentChatMemoryProvider(memoryService, summarizer);
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.mode", havingValue = "ai")
    AiCustomerAssistant aiCustomerAssistant(ChatModel chatModel,
                                             ChatMemoryProvider memoryProvider,
                                             CustomerServiceTools tools) {
        return AiServices.builder(AiCustomerAssistant.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryProvider)
                .tools(tools)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.ai.mode", havingValue = "ai")
    CustomerServiceAgent langChainCustomerServiceAgent(AiCustomerAssistant assistant,
                                                        SessionContextService sessionContextService) {
        return new LangChainCustomerServiceAgent(assistant, sessionContextService);
    }
}
