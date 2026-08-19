package com.chenxuekun.aicustomer.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ChatResponse(
        String sessionId,
        String answer,
        List<String> toolCalls,
        boolean transferredToHuman,
        String mode,
        LocalDateTime answeredAt,
        String traceId
) {
    public ChatResponse(String sessionId,
                        String answer,
                        List<String> toolCalls,
                        boolean transferredToHuman,
                        String mode,
                        LocalDateTime answeredAt) {
        this(sessionId, answer, toolCalls, transferredToHuman, mode, answeredAt, null);
    }
}
