package com.chenxuekun.aicustomer.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chenxuekun.aicustomer.entity.ToolCallLog;
import com.chenxuekun.aicustomer.mapper.ToolCallLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ToolCallLogService {
    private final ToolCallLogMapper logMapper;

    public ToolCallLogService(ToolCallLogMapper logMapper) {
        this.logMapper = logMapper;
    }

    public void record(String sessionId, String toolName, String request, String result, boolean success) {
        ToolCallLog log = new ToolCallLog();
        log.setSessionId(safe(sessionId, 64));
        log.setToolName(safe(toolName, 64));
        log.setRequestSummary(safe(request, 500));
        log.setResultSummary(safe(result, 1000));
        log.setSuccess(success);
        log.setCreatedAt(LocalDateTime.now());
        logMapper.insert(log);
    }

    public List<ToolCallLog> listRecent() {
        return logMapper.selectList(Wrappers.<ToolCallLog>lambdaQuery()
                .orderByDesc(ToolCallLog::getCreatedAt)
                .last("LIMIT 50"));
    }

    private String safe(String text, int limit) {
        if (text == null) {
            return "";
        }
        String sanitized = text.replaceAll("[\\r\\n]+", " ").trim();
        return sanitized.length() <= limit ? sanitized : sanitized.substring(0, limit);
    }
}
