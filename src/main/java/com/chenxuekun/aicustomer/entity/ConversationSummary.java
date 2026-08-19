package com.chenxuekun.aicustomer.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("conversation_summary")
public class ConversationSummary {
    @TableId(type = IdType.INPUT)
    private String sessionId;
    private String summary;
    private Long summarizedMessageCount;
    private LocalDateTime updatedAt;

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public Long getSummarizedMessageCount() { return summarizedMessageCount; }
    public void setSummarizedMessageCount(Long summarizedMessageCount) { this.summarizedMessageCount = summarizedMessageCount; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
