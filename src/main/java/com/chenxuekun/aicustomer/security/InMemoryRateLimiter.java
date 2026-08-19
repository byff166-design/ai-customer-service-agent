package com.chenxuekun.aicustomer.security;

import com.chenxuekun.aicustomer.config.RateLimitProperties;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class InMemoryRateLimiter {
    private static final long WINDOW_MILLIS = 60_000;
    private final ConcurrentMap<String, Window> windows = new ConcurrentHashMap<>();
    private final RateLimitProperties properties;
    private final Clock clock;

    @Autowired
    public InMemoryRateLimiter(RateLimitProperties properties) {
        this(properties, Clock.systemUTC());
    }

    InMemoryRateLimiter(RateLimitProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public boolean tryAcquire(String customerId) {
        long now = clock.millis();
        Window window = windows.compute(customerId, (key, current) -> {
            if (current == null || now - current.startedAt >= WINDOW_MILLIS) {
                return new Window(now, new AtomicInteger(1));
            }
            current.count.incrementAndGet();
            return current;
        });
        return window.count.get() <= properties.getMaxRequestsPerMinute();
    }

    private record Window(long startedAt, AtomicInteger count) { }
}
