package com.chenxuekun.aicustomer.service;

import com.chenxuekun.aicustomer.entity.ConversationSummary;
import com.chenxuekun.aicustomer.mapper.ConversationSummaryMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ConversationMemoryService {
    private static final int SUMMARY_LIMIT = 4000;
    private final ConversationSummaryMapper mapper;

    public ConversationMemoryService(ConversationSummaryMapper mapper) {
        this.mapper = mapper;
    }

    public Optional<ConversationSummary> find(String sessionId) {
        return Optional.ofNullable(mapper.selectById(sessionId));
    }

    public void save(String sessionId, String summary, long summarizedMessageCount) {
        ConversationSummary entity = new ConversationSummary();
        entity.setSessionId(sessionId);
        entity.setSummary(limit(summary));
        entity.setSummarizedMessageCount(Math.max(0, summarizedMessageCount));
        entity.setUpdatedAt(LocalDateTime.now());
        if (mapper.selectById(sessionId) == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
    }

    public void delete(String sessionId) {
        mapper.deleteById(sessionId);
    }

    private String limit(String summary) {
        String value = summary == null ? "" : summary.trim();
        return value.length() <= SUMMARY_LIMIT ? value : value.substring(0, SUMMARY_LIMIT);
    }
}
