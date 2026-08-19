package com.chenxuekun.aicustomer.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chenxuekun.aicustomer.entity.ToolCallLog;
import com.chenxuekun.aicustomer.mapper.ToolCallLogMapper;
import com.chenxuekun.aicustomer.observability.TraceContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ToolCallLogService {
    private final ToolCallLogMapper logMapper;
    private final TraceContext traceContext;

    public ToolCallLogService(ToolCallLogMapper logMapper, TraceContext traceContext) {
        this.logMapper = logMapper;
        this.traceContext = traceContext;
    }

    public void record(String sessionId, String toolName, String request, String result, boolean success) {
        record(sessionId, toolName, request, result, success, 0);
    }

    public void record(String sessionId,
                       String toolName,
                       String request,
                       String result,
                       boolean success,
                       long costMs) {
        ToolCallLog log = new ToolCallLog();
        log.setTraceId(safe(traceContext.currentId(), 64));
        log.setSessionId(safe(sessionId, 64));
        log.setToolName(safe(toolName, 64));
        log.setRequestSummary(safe(request, 500));
        log.setResultSummary(safe(result, 1000));
        log.setSuccess(success);
        log.setCostMs(Math.max(0, costMs));
        log.setCreatedAt(LocalDateTime.now());
        logMapper.insert(log);
    }

    public List<ToolCallLog> listRecent() {
        return logMapper.selectList(Wrappers.<ToolCallLog>lambdaQuery()
                .orderByDesc(ToolCallLog::getCreatedAt)
                .last("LIMIT 50"));
    }

    public List<ToolCallLog> listRecentBySession(String sessionId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return logMapper.selectList(Wrappers.<ToolCallLog>lambdaQuery()
                .eq(ToolCallLog::getSessionId, safe(sessionId, 64))
                .orderByDesc(ToolCallLog::getCreatedAt)
                .last("LIMIT " + safeLimit));
    }

    private String safe(String text, int limit) {
        if (text == null) {
            return "";
        }
        String sanitized = text.replaceAll("[\\r\\n]+", " ").trim();
        return sanitized.length() <= limit ? sanitized : sanitized.substring(0, limit);
    }
}
