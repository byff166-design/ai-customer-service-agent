package com.chenxuekun.aicustomer.service;

import com.chenxuekun.aicustomer.entity.ConversationSummary;
import com.chenxuekun.aicustomer.mapper.ConversationSummaryMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
        return find(sessionId, sessionId);
    }

    public void save(String sessionId, String summary, long summarizedMessageCount) {
        save(sessionId, sessionId, summary, summarizedMessageCount);
    }

    public Optional<ConversationSummary> find(String customerId, String sessionId) {
        return Optional.ofNullable(mapper.selectOne(Wrappers.<ConversationSummary>lambdaQuery()
                .eq(ConversationSummary::getCustomerId, customerId)
                .eq(ConversationSummary::getSessionId, sessionId)));
    }

    public void save(String customerId, String sessionId, String summary, long summarizedMessageCount) {
        ConversationSummary entity = new ConversationSummary();
        entity.setCustomerId(customerId);
        entity.setSessionId(sessionId);
        entity.setSummary(limit(summary));
        entity.setSummarizedMessageCount(Math.max(0, summarizedMessageCount));
        entity.setUpdatedAt(LocalDateTime.now());
        if (find(customerId, sessionId).isEmpty()) {
            mapper.insert(entity);
        } else {
            mapper.update(entity, Wrappers.<ConversationSummary>lambdaUpdate()
                    .eq(ConversationSummary::getCustomerId, customerId)
                    .eq(ConversationSummary::getSessionId, sessionId));
        }
    }

    public void delete(String sessionId) {
        delete(sessionId, sessionId);
    }

    public void delete(String customerId, String sessionId) {
        mapper.delete(Wrappers.<ConversationSummary>lambdaQuery()
                .eq(ConversationSummary::getCustomerId, customerId)
                .eq(ConversationSummary::getSessionId, sessionId));
    }

    private String limit(String summary) {
        String value = summary == null ? "" : summary.trim();
        return value.length() <= SUMMARY_LIMIT ? value : value.substring(0, SUMMARY_LIMIT);
    }
}
