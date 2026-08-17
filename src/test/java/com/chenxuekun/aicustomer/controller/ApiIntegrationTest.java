package com.chenxuekun.aicustomer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldChatSuccessfully() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sessionId":"API-1","message":"查询 ORD1001 的物流"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("mock"))
                .andExpect(jsonPath("$.toolCalls[0]").value("getLogistics"))
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("顺丰速运")));
    }

    @Test
    void shouldValidateBlankFields() throws Exception {
        mockMvc.perform(post("/api/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":\"\",\"message\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details.sessionId").exists())
                .andExpect(jsonPath("$.details.message").exists());
    }

    @Test
    void shouldReturnOrder() throws Exception {
        mockMvc.perform(get("/api/v1/orders/ORD1002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value("ORD1002"))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void shouldReturnNotFoundForUnknownOrder() throws Exception {
        mockMvc.perform(get("/api/v1/orders/ORD9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void shouldExposeHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
