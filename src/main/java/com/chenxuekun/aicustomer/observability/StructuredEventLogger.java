package com.chenxuekun.aicustomer.observability;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Component
public class StructuredEventLogger {
    private static final Logger LOG = LoggerFactory.getLogger("BUSINESS_EVENT");
    private static final Set<String> CORE_KEYS = Set.of("sessionId", "mode", "event", "costMs");

    public void info(String event,
                     String sessionId,
                     String mode,
                     long costMs,
                     String message,
                     Map<String, ?> details) {
        write(false, event, sessionId, mode, costMs, message, details, null);
    }

    public void warn(String event,
                     String sessionId,
                     String mode,
                     long costMs,
                     String message,
                     Map<String, ?> details,
                     Throwable error) {
        write(true, event, sessionId, mode, costMs, message, details, error);
    }

    private void write(boolean warning,
                       String event,
                       String sessionId,
                       String mode,
                       long costMs,
                       String message,
                       Map<String, ?> details,
                       Throwable error) {
        Set<String> temporaryKeys = new LinkedHashSet<>(CORE_KEYS);
        put("event", event);
        put("sessionId", sessionId);
        put("mode", mode);
        put("costMs", Long.toString(Math.max(0, costMs)));
        details.forEach((key, value) -> {
            if (value != null) {
                put(key, String.valueOf(value));
                temporaryKeys.add(key);
            }
        });
        try {
            if (warning) {
                LOG.warn(message, error);
            } else {
                LOG.info(message);
            }
        } finally {
            temporaryKeys.forEach(MDC::remove);
        }
    }

    private void put(String key, String value) {
        MDC.put(key, value == null ? "" : value);
    }
}
