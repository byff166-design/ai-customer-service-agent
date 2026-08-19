package com.chenxuekun.aicustomer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.rate-limit.max-requests-per-minute=2")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimitApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn429WithStableErrorCode() throws Exception {
        for (int index = 0; index < 2; index++) {
            mockMvc.perform(post("/api/v1/chat")
                            .header("X-Customer-Id", "RATE-CUSTOMER")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"sessionId\":\"RATE-" + index + "\",\"message\":\"你好\"}"))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/api/v1/chat")
                        .header("X-Customer-Id", "RATE-CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":\"RATE-3\",\"message\":\"你好\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }
}
