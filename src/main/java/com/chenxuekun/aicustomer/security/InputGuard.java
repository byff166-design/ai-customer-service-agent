package com.chenxuekun.aicustomer.security;

import com.chenxuekun.aicustomer.exception.InvalidInputException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class InputGuard {
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    private static final List<String> INJECTION_MARKERS = List.of(
            "ignore previous", "ignore all previous", "system prompt", "developer message",
            "忽略以上", "忽略之前", "系统提示词", "泄露api key", "查看api key"
    );

    public String requireSafeId(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim();
        if (!SAFE_ID.matcher(normalized).matches()) {
            throw new InvalidInputException(fieldName + " 只允许字母、数字、点、下划线和连字符，长度1-64");
        }
        return normalized;
    }

    public String normalizeMessage(String message) {
        return message.trim().replaceAll("(?:\\r?\\n){3,}", "\n\n");
    }

    public boolean isPromptInjection(String message) {
        String normalized = message.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        return INJECTION_MARKERS.stream().anyMatch(normalized::contains);
    }
}
