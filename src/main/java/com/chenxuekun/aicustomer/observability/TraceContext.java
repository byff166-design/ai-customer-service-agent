package com.chenxuekun.aicustomer.observability;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class TraceContext {
    public static final String HEADER_NAME = "X-Trace-Id";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Pattern SAFE_TRACE_ID = Pattern.compile("[A-Za-z0-9._-]{8,64}");

    private final ThreadLocal<String> current = new ThreadLocal<>();

    public String begin(String requestedTraceId) {
        String traceId = isSafe(requestedTraceId) ? requestedTraceId : generate();
        current.set(traceId);
        MDC.put("traceId", traceId);
        return traceId;
    }

    public String begin() {
        return begin(null);
    }

    public String currentId() {
        return current.get();
    }

    public boolean isActive() {
        return current.get() != null;
    }

    public void clear() {
        current.remove();
        MDC.remove("traceId");
    }

    private boolean isSafe(String traceId) {
        return traceId != null && SAFE_TRACE_ID.matcher(traceId).matches();
    }

    private String generate() {
        String suffix = UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase();
        return TIME_FORMAT.format(LocalDateTime.now()) + suffix;
    }
}
