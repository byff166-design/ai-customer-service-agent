package com.chenxuekun.aicustomer.agent;

import com.chenxuekun.aicustomer.service.SessionContextService;

public class LangChainCustomerServiceAgent implements CustomerServiceAgent {
    private final AiCustomerAssistant assistant;
    private final SessionContextService sessionContextService;

    public LangChainCustomerServiceAgent(AiCustomerAssistant assistant,
                                         SessionContextService sessionContextService) {
        this.assistant = assistant;
        this.sessionContextService = sessionContextService;
    }

    @Override
    public String chat(String sessionId, String message) {
        return assistant.answer(sessionId, sessionContextService.build(sessionId), message);
    }

    @Override
    public String mode() {
        return "ai";
    }
}
