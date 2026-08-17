package com.chenxuekun.aicustomer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiProperties {
    private String mode = "mock";
    private String apiKey = "";
    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    private String modelName = "qwen-plus";
    private int timeoutSeconds = 45;

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}
