package com.chenxuekun.aicustomer.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ChatResponse(
        String sessionId,
        String answer,
        List<String> toolCalls,
        boolean transferredToHuman,
        String mode,
        LocalDateTime answeredAt
) {
}
