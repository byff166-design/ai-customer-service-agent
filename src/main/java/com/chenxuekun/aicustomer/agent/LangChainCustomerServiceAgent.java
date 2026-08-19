package com.chenxuekun.aicustomer.agent;

import com.chenxuekun.aicustomer.service.SessionContextService;

public class LangChainCustomerServiceAgent implements CustomerServiceAgent {
    private final AiCustomerAssistant assistant;
    private final SessionContextService sessionContextService;
    private final ToolInvocationContext invocationContext;

    public LangChainCustomerServiceAgent(AiCustomerAssistant assistant,
                                         SessionContextService sessionContextService,
                                         ToolInvocationContext invocationContext) {
        this.assistant = assistant;
        this.sessionContextService = sessionContextService;
        this.invocationContext = invocationContext;
    }

    @Override
    public String chat(String sessionId, String message) {
        return assistant.answer(invocationContext.conversationKey(),
                sessionContextService.build(invocationContext.customerId(), sessionId), message);
    }

    @Override
    public String mode() {
        return "ai";
    }
}
