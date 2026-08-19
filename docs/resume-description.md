# 简历项目描述

## 项目名称

基于 Function Calling 的智能客服与工单自动化系统

## 技术栈

Java 17、Spring Boot 3.5、LangChain4j、MyBatis-Plus、H2、SLF4J MDC、JUnit 5、MockMvc、GitHub Actions

## 建议写法

- **Agent 工具编排：** 基于 LangChain4j AI Services 与 Function Calling 封装订单、物流、FAQ、工单及转人工等 6 类业务工具，实现自然语言意图到 Java 业务操作的闭环。
- **可靠性设计：** 对订单号缺失、工具执行失败、FAQ 无依据及重复工单等场景设计追问、拒答和人工兜底策略，避免模型编造业务数据。
- **可观测性与审计：** 基于 Filter、ThreadLocal 与 MDC 实现 TraceId 全链路传递，输出 JSON 结构化事件日志，并在工具审计表记录调用耗时、成功状态及 TraceId，实现接口、日志和数据记录对账。
- **会话记忆：** 为 AI 模式实现滚动摘要压缩，将长对话摘要持久化至 H2，并按同一 session 的近期工具记录生成受限行为画像；mock 模式保持零模型依赖。
- **测试与交付：** 设计 mock/ai 双运行模式，编写 40 项单元及接口集成测试和 30 条固定评测样例，并通过 Maven Wrapper、Swagger 和 GitHub Actions 完成可复现交付。

注意：当前画像边界是 `sessionId`，不能写成“跨设备用户画像”；只有使用新 API Key 再次完成真实 AI 验收后，才能写“接入通义千问完成摘要压缩与自主工具调用”。
