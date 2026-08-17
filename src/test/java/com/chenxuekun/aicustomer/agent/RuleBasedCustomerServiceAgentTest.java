package com.chenxuekun.aicustomer.agent;

import com.chenxuekun.aicustomer.agent.tool.CustomerServiceTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RuleBasedCustomerServiceAgentTest {
    private CustomerServiceTools tools;
    private RuleBasedCustomerServiceAgent agent;

    @BeforeEach
    void setUp() {
        tools = mock(CustomerServiceTools.class);
        agent = new RuleBasedCustomerServiceAgent(tools);
    }

    @Test
    void shouldCallLogisticsToolWhenOrderNumberExists() {
        when(tools.getLogistics("ORD1001")).thenReturn("运输中");

        String answer = agent.chat("S1", "帮我查一下 ORD1001 的物流");

        assertThat(answer).isEqualTo("运输中");
        verify(tools).getLogistics("ORD1001");
    }

    @Test
    void shouldAskForOrderNumberInsteadOfGuessing() {
        String answer = agent.chat("S1", "帮我查一下物流");

        assertThat(answer).contains("请提供").contains("订单号");
    }

    @Test
    void shouldTransferToHumanOnExplicitRequest() {
        when(tools.transferToHuman("用户主动要求人工客服")).thenReturn("已转人工");

        assertThat(agent.chat("S1", "我要找人工客服")).isEqualTo("已转人工");
        verify(tools).transferToHuman("用户主动要求人工客服");
    }

    @Test
    void shouldRejectUnrelatedQuestions() {
        assertThat(agent.chat("S1", "帮我写一首诗")).contains("只处理订单、物流和售后问题");
    }
}
