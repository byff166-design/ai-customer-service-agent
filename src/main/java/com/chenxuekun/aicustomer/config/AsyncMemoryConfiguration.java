package com.chenxuekun.aicustomer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncMemoryConfiguration {
    @Bean("compactionExecutor")
    Executor compactionExecutor(MemoryProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCompactionPoolSize());
        executor.setMaxPoolSize(properties.getCompactionPoolSize());
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("memory-compact-");
        executor.initialize();
        return executor;
    }
}
