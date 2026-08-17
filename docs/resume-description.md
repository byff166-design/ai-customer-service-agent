# 简历项目描述

## 项目名称

基于 Function Calling 的智能客服与工单自动化系统

## 技术栈

Java 17、Spring Boot 3.5、LangChain4j、MyBatis-Plus、H2、JUnit 5、MockMvc、GitHub Actions

## 建议写法

- **Agent 工具编排：** 基于 LangChain4j AI Services 与 Function Calling 封装订单、物流、FAQ、工单及转人工等 6 类业务工具，实现自然语言意图到 Java 业务操作的闭环。
- **可靠性设计：** 对订单号缺失、工具执行失败、FAQ 无依据及重复工单等场景设计追问、拒答和人工兜底策略，避免模型编造业务数据。
- **业务与数据层：** 使用 Spring Boot、MyBatis-Plus 与 H2 实现订单、物流、售后工单和工具审计模块，记录会话维度的工具参数、结果及成功状态。
- **测试与交付：** 设计 mock/ai 双运行模式，编写 33 项单元及接口集成测试和 30 条固定评测样例，并通过 Maven Wrapper、Swagger 和 GitHub Actions 完成可复现交付。

注意：只有真实 AI 模式验证通过后，简历中才能写“接入通义千问完成自主工具调用”。
