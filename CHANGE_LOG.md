# P0 企业级升级变更说明

## 本次升级

- 新增请求级 TraceId：支持 `X-Trace-Id` 透传或自动生成，响应头和聊天响应体均可查看。
- 新增 JSON 结构化日志：覆盖 `agentRequest`、`llmCall`、`toolCall`、`agentFallback`、`agentResponse`。
- 工具审计表新增 `trace_id`、`cost_ms`，可关联 API、日志与数据库记录。
- AI 模式新增摘要压缩：长对话达到阈值后压缩旧消息，摘要持久化到 H2。
- AI 模式新增 session 行为画像：读取摘要和近期工具记录后注入系统上下文。
- 前端消息下方增加 TraceId 展示。

## 主要新增类

- `observability/TraceContext`、`TraceFilter`、`StructuredEventLogger`
- `memory/SummarizingChatMemory`、`PersistentChatMemoryProvider`
- `memory/ConversationSummarizer`、`LlmConversationSummarizer`
- `entity/ConversationSummary`、`mapper/ConversationSummaryMapper`
- `service/ConversationMemoryService`、`SessionContextService`

## 设计边界

当前长期上下文以 `sessionId` 为边界，仅代表同一会话，不代表经过认证的真实用户。若用于生产，
需要增加登录鉴权和 `customerId`，再把记忆存储迁移到可共享的数据库或 Redis。

## 验证方式

```powershell
.\mvnw.cmd clean verify
```

启动后连续请求 `POST /api/v1/chat`，确认每次响应的 `traceId` 不同，且控制台 JSON 日志中可以
按该值关联请求、工具调用和响应事件。
