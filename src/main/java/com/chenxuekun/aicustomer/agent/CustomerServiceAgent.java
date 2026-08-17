package com.chenxuekun.aicustomer.agent;

public interface CustomerServiceAgent {
    String chat(String sessionId, String message);

    String mode();
}
