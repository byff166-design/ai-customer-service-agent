package com.chenxuekun.aicustomer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.auth")
public class CustomerAuthProperties {
    private boolean enabled;
    private Map<String, String> customerApiKeys = new LinkedHashMap<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Map<String, String> getCustomerApiKeys() { return customerApiKeys; }
    public void setCustomerApiKeys(Map<String, String> customerApiKeys) {
        this.customerApiKeys = customerApiKeys == null ? new LinkedHashMap<>() : customerApiKeys;
    }
}
