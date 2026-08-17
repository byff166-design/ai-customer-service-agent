package com.chenxuekun.aicustomer.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
        String code,
        String message,
        Map<String, String> details,
        LocalDateTime timestamp
) {
}
