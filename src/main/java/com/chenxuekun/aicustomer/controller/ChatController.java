package com.chenxuekun.aicustomer.controller;

import com.chenxuekun.aicustomer.dto.ChatRequest;
import com.chenxuekun.aicustomer.dto.ChatResponse;
import com.chenxuekun.aicustomer.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "与智能客服对话", description = "mock 模式可离线演示，ai 模式由通义千问选择并调用业务工具。")
    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request);
    }
}
