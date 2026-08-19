package com.chenxuekun.aicustomer.security;

import com.chenxuekun.aicustomer.config.CustomerAuthProperties;
import com.chenxuekun.aicustomer.exception.AuthenticationException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerAuthServiceTest {
    @Test
    void shouldEnforceConfiguredKeyOnlyInAiMode() {
        CustomerAuthProperties properties = new CustomerAuthProperties();
        properties.setEnabled(true);
        properties.setCustomerApiKeys(Map.of("CUSTOMER-1", "secret-key"));
        CustomerAuthService service = new CustomerAuthService(properties);

        assertThatCode(() -> service.verify("ai", "CUSTOMER-1", "secret-key")).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.verify("ai", "CUSTOMER-1", "wrong-key"))
                .isInstanceOf(AuthenticationException.class);
        assertThatCode(() -> service.verify("mock", "CUSTOMER-1", null)).doesNotThrowAnyException();
    }
}
