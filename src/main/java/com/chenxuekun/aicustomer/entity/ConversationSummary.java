package com.chenxuekun.aicustomer.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;

@TableName("conversation_memory")
public class ConversationSummary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String customerId;
    private String sessionId;
    private String summary;
    private Long summarizedMessageCount;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Long getSummarizedMessageCount() { return summarizedMessageCount; }
    public void setSummarizedMessageCount(Long summarizedMessageCount) { this.summarizedMessageCount = summarizedMessageCount; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
