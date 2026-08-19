package com.chenxuekun.aicustomer.security;

import com.chenxuekun.aicustomer.config.CustomerAuthProperties;
import com.chenxuekun.aicustomer.exception.AuthenticationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
public class CustomerAuthService {
    private final CustomerAuthProperties properties;

    public CustomerAuthService(CustomerAuthProperties properties) {
        this.properties = properties;
    }

    public void verify(String mode, String customerId, String presentedApiKey) {
        if (!properties.isEnabled() || !"ai".equals(mode)) {
            return;
        }
        String expected = properties.getCustomerApiKeys().get(customerId);
        if (expected == null || presentedApiKey == null || !constantTimeEquals(expected, presentedApiKey)) {
            throw new AuthenticationException("客户身份验证失败");
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}
