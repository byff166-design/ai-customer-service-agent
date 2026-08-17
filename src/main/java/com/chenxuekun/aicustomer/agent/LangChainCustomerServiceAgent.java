package com.chenxuekun.aicustomer.agent;

public class LangChainCustomerServiceAgent implements CustomerServiceAgent {
    private final AiCustomerAssistant assistant;

    public LangChainCustomerServiceAgent(AiCustomerAssistant assistant) {
        this.assistant = assistant;
    }

    @Override
    public String chat(String sessionId, String message) {
        return assistant.answer(sessionId, message);
    }

    @Override
    public String mode() {
        return "ai";
    }
}
