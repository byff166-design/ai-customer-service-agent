# P1 单机生产化升级变更说明

## P1 新增

- 引入 `customerId`，对话摘要以 `customerId + sessionId` 唯一约束实现客户间隔离。
- AI 模式可选开启 `X-Api-Key` 鉴权；mock 模式保持零配置演示。
- 新增每客户每分钟的单机固定窗口限流，超限返回 HTTP 429 / `RATE_LIMITED`。
- 新增 ID 白名单校验、文本归一化和常见提示词注入拦截。
- 统一错误响应增加 `traceId`，补充 `AUTH_FAILED`、`RATE_LIMITED`、`PARAM_INVALID` 等错误码。
- 长对话摘要改为独立线程池异步执行，请求线程不等待摘要模型。
- 新增身份、限流、输入防护、客户隔离和异步摘要测试，测试总数 52。

## P1 边界

当前鉴权和限流适合单机演示，不是完整的集群方案。多实例环境应使用统一身份服务和 Redis 等共享限流存储。

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
