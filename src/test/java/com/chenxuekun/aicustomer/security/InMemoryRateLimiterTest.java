package com.chenxuekun.aicustomer.security;

import com.chenxuekun.aicustomer.config.RateLimitProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRateLimiterTest {
    @Test
    void shouldLimitPerCustomerWithoutAffectingAnotherCustomer() {
        RateLimitProperties properties = new RateLimitProperties();
        properties.setMaxRequestsPerMinute(2);
        InMemoryRateLimiter limiter = new InMemoryRateLimiter(properties);

        assertThat(limiter.tryAcquire("CUSTOMER-A")).isTrue();
        assertThat(limiter.tryAcquire("CUSTOMER-A")).isTrue();
        assertThat(limiter.tryAcquire("CUSTOMER-A")).isFalse();
        assertThat(limiter.tryAcquire("CUSTOMER-B")).isTrue();
    }
}
