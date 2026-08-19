package com.chenxuekun.aicustomer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    private int maxRequestsPerMinute = 30;

    public int getMaxRequestsPerMinute() { return maxRequestsPerMinute; }
    public void setMaxRequestsPerMinute(int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = Math.max(1, maxRequestsPerMinute);
    }
}
