package com.chenxuekun.aicustomer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank(message = "sessionId 不能为空")
        @Size(max = 64, message = "sessionId 不能超过64个字符")
        String sessionId,

        @NotBlank(message = "message 不能为空")
        @Size(max = 1000, message = "message 不能超过1000个字符")
        String message
) {
}
