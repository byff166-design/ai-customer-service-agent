package com.chenxuekun.aicustomer.service;

import com.chenxuekun.aicustomer.agent.CustomerServiceAgent;
import com.chenxuekun.aicustomer.agent.ToolInvocationContext;
import com.chenxuekun.aicustomer.dto.ChatRequest;
import com.chenxuekun.aicustomer.dto.ChatResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {
    private final CustomerServiceAgent agent;
    private final ToolInvocationContext context;
    private final ToolCallLogService logService;

    public ChatService(CustomerServiceAgent agent,
                       ToolInvocationContext context,
                       ToolCallLogService logService) {
        this.agent = agent;
        this.context = context;
        this.logService = logService;
    }

    public ChatResponse chat(ChatRequest request) {
        context.begin(request.sessionId());
        try {
            String answer = agent.chat(request.sessionId(), request.message());
            List<String> calls = context.toolNames();
            return new ChatResponse(
                    request.sessionId(),
                    answer,
                    calls,
                    calls.contains("transferToHuman"),
                    agent.mode(),
                    LocalDateTime.now()
            );
        } catch (RuntimeException exception) {
            logService.record(request.sessionId(), "agentFallback", request.message(), exception.getMessage(), false);
            return new ChatResponse(
                    request.sessionId(),
                    "当前智能客服暂时不可用，已为您登记转人工处理。",
                    List.of("agentFallback", "transferToHuman"),
                    true,
                    agent.mode(),
                    LocalDateTime.now()
            );
        } finally {
            context.clear();
        }
    }
}
