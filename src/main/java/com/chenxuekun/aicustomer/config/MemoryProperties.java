package com.chenxuekun.aicustomer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.memory")
public class MemoryProperties {
    private int compactionPoolSize = 2;

    public int getCompactionPoolSize() { return compactionPoolSize; }
    public void setCompactionPoolSize(int compactionPoolSize) {
        this.compactionPoolSize = Math.max(1, compactionPoolSize);
    }
}
