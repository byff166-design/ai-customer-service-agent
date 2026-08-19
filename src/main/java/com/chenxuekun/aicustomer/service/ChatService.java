package com.chenxuekun.aicustomer.service;

import com.chenxuekun.aicustomer.agent.CustomerServiceAgent;
import com.chenxuekun.aicustomer.agent.ToolInvocationContext;
import com.chenxuekun.aicustomer.agent.tool.CustomerServiceTools;
import com.chenxuekun.aicustomer.dto.ChatRequest;
import com.chenxuekun.aicustomer.dto.ChatResponse;
import com.chenxuekun.aicustomer.observability.StructuredEventLogger;
import com.chenxuekun.aicustomer.observability.TraceContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ChatService {
    private final CustomerServiceAgent agent;
    private final ToolInvocationContext context;
    private final ToolCallLogService logService;
    private final CustomerServiceTools tools;
    private final TraceContext traceContext;
    private final StructuredEventLogger eventLogger;

    public ChatService(CustomerServiceAgent agent,
                       ToolInvocationContext context,
                       ToolCallLogService logService,
                       CustomerServiceTools tools,
                       TraceContext traceContext,
                       StructuredEventLogger eventLogger) {
        this.agent = agent;
        this.context = context;
        this.logService = logService;
        this.tools = tools;
        this.traceContext = traceContext;
        this.eventLogger = eventLogger;
    }

    public ChatResponse chat(ChatRequest request) {
        boolean ownsTrace = !traceContext.isActive();
        if (ownsTrace) {
            traceContext.begin();
        }
        String mode = agent.mode();
        String traceId = traceContext.currentId();
        long requestStartedAt = System.nanoTime();
        context.begin(request.sessionId(), mode);
        eventLogger.info("agentRequest", request.sessionId(), mode, 0,
                "收到客服消息", Map.of("messageLength", request.message().length()));
        try {
            long llmStartedAt = System.nanoTime();
            String answer;
            try {
                answer = agent.chat(request.sessionId(), request.message());
                long llmCostMs = elapsedMillis(llmStartedAt);
                eventLogger.info("llmCall", request.sessionId(), mode, llmCostMs,
                        mode.equals("ai") ? "大模型调用完成" : "规则引擎调用完成",
                        Map.of("success", true));
            } catch (RuntimeException exception) {
                eventLogger.warn("llmCall", request.sessionId(), mode, elapsedMillis(llmStartedAt),
                        mode.equals("ai") ? "大模型调用失败" : "规则引擎调用失败",
                        Map.of("success", false), exception);
                throw exception;
            }
            if (context.hasFailure() && !context.toolNames().contains("transferToHuman")) {
                eventLogger.warn("agentFallback", request.sessionId(), mode, elapsedMillis(requestStartedAt),
                        "业务工具失败，执行转人工兜底",
                        Map.of("transferredToHuman", true), null);
                String fallback = tools.transferToHuman("业务工具执行失败，系统自动兜底");
                answer = answer + System.lineSeparator() + fallback;
            }
            List<String> calls = context.toolNames();
            ChatResponse response = new ChatResponse(
                    request.sessionId(),
                    answer,
                    calls,
                    calls.contains("transferToHuman"),
                    mode,
                    LocalDateTime.now(),
                    traceId
            );
            eventLogger.info("agentResponse", request.sessionId(), mode, elapsedMillis(requestStartedAt),
                    "客服响应完成", Map.of("transferredToHuman", response.transferredToHuman()));
            return response;
        } catch (RuntimeException exception) {
            long costMs = elapsedMillis(requestStartedAt);
            logService.record(request.sessionId(), "agentFallback", request.message(), exception.getMessage(), false, costMs);
            eventLogger.warn("agentFallback", request.sessionId(), mode, costMs,
                    "Agent 异常，执行转人工兜底", Map.of("transferredToHuman", true), exception);
            ChatResponse response = new ChatResponse(
                    request.sessionId(),
                    "当前智能客服暂时不可用，已为您登记转人工处理。",
                    List.of("agentFallback", "transferToHuman"),
                    true,
                    mode,
                    LocalDateTime.now(),
                    traceId
            );
            eventLogger.info("agentResponse", request.sessionId(), mode, costMs,
                    "客服兜底响应完成", Map.of("transferredToHuman", true));
            return response;
        } finally {
            context.clear();
            if (ownsTrace) {
                traceContext.clear();
            }
        }
    }

    private long elapsedMillis(long startedAt) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
    }
}
