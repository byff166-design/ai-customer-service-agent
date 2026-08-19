package com.chenxuekun.aicustomer.observability;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TraceContextTest {

    @Test
    void shouldCreateUniqueTraceIdsConcurrently() throws InterruptedException {
        TraceContext context = new TraceContext();
        Set<String> ids = ConcurrentHashMap.newKeySet();
        ExecutorService executor = Executors.newFixedThreadPool(8);

        for (int index = 0; index < 200; index++) {
            executor.submit(() -> {
                ids.add(context.begin());
                context.clear();
            });
        }

        executor.shutdown();
        assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        assertThat(ids).hasSize(200).allMatch(id -> id.matches("\\d{14}[A-F0-9]{12}"));
    }

    @Test
    void shouldAcceptSafeUpstreamTraceAndClearIt() {
        TraceContext context = new TraceContext();

        assertThat(context.begin("UPSTREAM-TRACE-001")).isEqualTo("UPSTREAM-TRACE-001");
        assertThat(context.currentId()).isEqualTo("UPSTREAM-TRACE-001");

        context.clear();
        assertThat(context.currentId()).isNull();
    }
}
