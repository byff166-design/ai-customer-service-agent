package com.chenxuekun.aicustomer.service;

import com.chenxuekun.aicustomer.agent.CustomerServiceAgent;
import com.chenxuekun.aicustomer.agent.ToolInvocationContext;
import com.chenxuekun.aicustomer.agent.tool.CustomerServiceTools;
import com.chenxuekun.aicustomer.dto.ChatRequest;
import com.chenxuekun.aicustomer.dto.ChatResponse;
import com.chenxuekun.aicustomer.observability.StructuredEventLogger;
import com.chenxuekun.aicustomer.observability.TraceContext;
import com.chenxuekun.aicustomer.exception.AuthenticationException;
import com.chenxuekun.aicustomer.exception.BusinessException;
import com.chenxuekun.aicustomer.exception.RateLimitExceededException;
import com.chenxuekun.aicustomer.exception.InvalidInputException;
import com.chenxuekun.aicustomer.security.CustomerAuthService;
import com.chenxuekun.aicustomer.security.InMemoryRateLimiter;
import com.chenxuekun.aicustomer.security.InputGuard;
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
    private final CustomerAuthService authService;
    private final InMemoryRateLimiter rateLimiter;
    private final InputGuard inputGuard;

    public ChatService(CustomerServiceAgent agent,
                       ToolInvocationContext context,
                       ToolCallLogService logService,
                       CustomerServiceTools tools,
                       TraceContext traceContext,
                       StructuredEventLogger eventLogger,
                       CustomerAuthService authService,
                       InMemoryRateLimiter rateLimiter,
                       InputGuard inputGuard) {
        this.agent = agent;
        this.context = context;
        this.logService = logService;
        this.tools = tools;
        this.traceContext = traceContext;
        this.eventLogger = eventLogger;
        this.authService = authService;
        this.rateLimiter = rateLimiter;
        this.inputGuard = inputGuard;
    }

    public ChatResponse chat(ChatRequest request) {
        return chat(request, null, null);
    }

    public ChatResponse chat(ChatRequest request, String customerIdHeader, String apiKey) {
        boolean ownsTrace = !traceContext.isActive();
        if (ownsTrace) {
            traceContext.begin();
        }
        String mode = agent.mode();
        String traceId = traceContext.currentId();
        long requestStartedAt = System.nanoTime();
        String sessionId = null;
        String customerId = null;
        String message = null;
        try {
        sessionId = inputGuard.requireSafeId(request.sessionId(), "sessionId");
        String requestedCustomerId = request.customerId() == null || request.customerId().isBlank()
                ? customerIdHeader : request.customerId();
        customerId = inputGuard.requireSafeId(
                requestedCustomerId == null || requestedCustomerId.isBlank() ? sessionId : requestedCustomerId,
                "customerId");
        message = inputGuard.normalizeMessage(request.message());
        context.begin(sessionId, mode, customerId);
        eventLogger.info("agentRequest", request.sessionId(), mode, 0,
                "收到客服消息", Map.of("messageLength", message.length(), "customerId", customerId));
            try {
                authService.verify(mode, customerId, apiKey);
            } catch (AuthenticationException exception) {
                eventLogger.warn("authFailed", sessionId, mode, elapsedMillis(requestStartedAt),
                        "客户身份验证失败", Map.of("customerId", customerId), exception);
                throw exception;
            }
            if (!rateLimiter.tryAcquire(customerId)) {
                eventLogger.warn("rateLimited", sessionId, mode, elapsedMillis(requestStartedAt),
                        "客户请求超过单机限流阈值", Map.of("customerId", customerId), null);
                throw new RateLimitExceededException("请求过于频繁，请60秒后再试");
            }
            if (inputGuard.isPromptInjection(message)) {
                eventLogger.warn("promptInjection", sessionId, mode, elapsedMillis(requestStartedAt),
                        "拦截疑似提示词注入请求", Map.of("customerId", customerId), null);
                return new ChatResponse(sessionId,
                        "为了保护系统安全，我只处理订单、物流和售后问题，无法提供系统指令或凭证信息。",
                        List.of(), false, mode, LocalDateTime.now(), traceId);
            }
            long llmStartedAt = System.nanoTime();
            String answer;
            try {
                answer = agent.chat(sessionId, message);
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
                    sessionId,
                    answer,
                    calls,
                    calls.contains("transferToHuman"),
                    mode,
                    LocalDateTime.now(),
                    traceId
            );
            eventLogger.info("agentResponse", request.sessionId(), mode, elapsedMillis(requestStartedAt),
                    "客服响应完成", Map.of(
                            "transferredToHuman", response.transferredToHuman(), "customerId", customerId));
            return response;
        } catch (AuthenticationException | RateLimitExceededException | BusinessException | InvalidInputException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            long costMs = elapsedMillis(requestStartedAt);
            logService.record(sessionId, "agentFallback", message, exception.getMessage(), false, costMs);
            eventLogger.warn("agentFallback", request.sessionId(), mode, costMs,
                    "Agent 异常，执行转人工兜底", Map.of("transferredToHuman", true), exception);
            ChatResponse response = new ChatResponse(
                    sessionId,
                    "当前智能客服暂时不可用，已为您登记转人工处理。",
                    List.of("agentFallback", "transferToHuman"),
                    true,
                    mode,
                    LocalDateTime.now(),
                    traceId
            );
            eventLogger.info("agentResponse", request.sessionId(), mode, costMs,
                    "客服兜底响应完成", Map.of(
                            "transferredToHuman", true, "customerId", customerId));
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
