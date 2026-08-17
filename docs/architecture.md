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
  -> ChatService.begin(sessionId)
  -> CustomerServiceAgent.chat()
  -> CustomerServiceTools
  -> Order/Logistics/FAQ/Ticket Service
  -> MyBatis-Plus + H2
  -> ToolCallLog
  -> ChatResponse
```

`ToolInvocationContext` 使用 ThreadLocal 保存当前同步请求的会话和工具名，响应可以显示本轮调用了哪些工具；工具执行结果同时写入审计表。

## 4. 失败策略

| 场景 | 策略 |
|---|---|
| 缺少订单号 | 追问用户，不猜测 |
| 订单/物流不存在 | 返回明确的工具失败信息 |
| FAQ 无依据 | 明确未找到依据并建议人工 |
| 重复创建工单 | 业务层拒绝并返回已有工单号 |
| 模型/API 异常 | ChatService 降级为转人工响应 |
| 无关问题 | 拒绝回答并收敛到客服范围 |

## 5. 后续可扩展方向

1. PostgreSQL 替换 H2，并通过 Flyway 管理数据库版本。
2. Redis 存储分布式会话记忆和限流计数。
3. RabbitMQ 异步分发高优先级工单。
4. 接入真实订单/物流沙箱 API。
5. 增加客服后台、工单分配与处理时效统计。
