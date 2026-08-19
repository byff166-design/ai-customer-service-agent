# 架构与关键设计

## 1. 为什么不让大模型直接查询数据库

大模型只负责理解自然语言和选择工具，不持有 SQL 权限。工具层负责参数校验、业务规则和数据访问，避免模型绕过订单校验或编造结果。

## 2. 双模式设计

### mock 模式

使用确定性规则路由，适合：

- 无 API Key 的本地演示
- CI 自动测试
- 对业务服务和异常兜底做稳定回归

### ai 模式

使用 LangChain4j `AiServices` 连接通义千问，由模型根据工具描述自主选择 `@Tool` 方法。两种模式共享同一套业务工具和数据库，因此 mock 模式不是另一套假业务。

## 3. 工具调用链路

```text
ChatController
  -> TraceFilter.begin(traceId) + MDC
  -> ChatService.begin(sessionId, mode)
  -> CustomerServiceAgent.chat()
  -> CustomerServiceTools
  -> Order/Logistics/FAQ/Ticket Service
  -> MyBatis-Plus + H2
  -> ToolCallLog(traceId, costMs, success)
  -> ChatResponse
```

`TraceFilter` 接收合法的上游 `X-Trace-Id` 或生成新值，并在响应头和响应体回传。
`ToolInvocationContext` 使用 ThreadLocal 保存当前同步请求的会话、模式和工具名；工具执行结果同时写入审计表。

## 4. 结构化日志与指标

控制台日志统一输出 JSON。业务事件通过 MDC 携带 `traceId`、`sessionId`、`mode`、
`event` 和 `costMs`，可以离线统计模型耗时、工具成功率和转人工率。工具失败属于可预期
业务结果，只记录结构化失败事件；未捕获的 Agent 异常才记录堆栈。

## 5. 会话记忆

AI 模式使用 `SummarizingChatMemory`：短期消息保留在窗口中，达到阈值后调用模型压缩旧消息，
摘要写入 H2 的 `conversation_summary`。每轮对话还会汇总同一 `sessionId` 的近期工具记录，作为
受限的系统上下文注入。mock 模式不调用模型摘要。

这里刻意称为“session 行为画像”，因为当前 API 没有认证后的 `customerId`，不能把浏览器会话
冒充真实用户身份。生产化时应增加认证与客户主键，再建立跨设备长期画像。

## 6. 失败策略

| 场景 | 策略 |
|---|---|
| 缺少订单号 | 追问用户，不猜测 |
| 订单/物流不存在 | 返回明确的工具失败信息 |
| FAQ 无依据 | 明确未找到依据并建议人工 |
| 重复创建工单 | 业务层拒绝并返回已有工单号 |
| 模型/API 异常 | ChatService 降级为转人工响应 |
| 无关问题 | 拒绝回答并收敛到客服范围 |

## 7. 后续可扩展方向

1. PostgreSQL 替换 H2，并通过 Flyway 管理数据库版本。
2. Redis 存储分布式会话记忆和限流计数。
3. RabbitMQ 异步分发高优先级工单。
4. 接入真实订单/物流沙箱 API。
5. 增加客服后台、工单分配与处理时效统计。
6. 引入 OpenTelemetry，将 Trace 跨服务上报到可观测平台。
