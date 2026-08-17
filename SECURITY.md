# Security Policy

本项目只使用模拟业务数据，请勿提交真实客户、订单或联系方式。

## 密钥管理

- 使用 `DASHSCOPE_API_KEY` 环境变量。
- 不要把密钥写入 `application.yml`、启动脚本、README、Issue 或日志。
- 如果密钥曾经被提交，即使之后删除文件，也应立即废弃旧密钥并清理 Git 历史。

## 报告问题

请通过 GitHub Security Advisory 私下报告安全问题，不要在公开 Issue 中粘贴密钥或漏洞利用数据。
