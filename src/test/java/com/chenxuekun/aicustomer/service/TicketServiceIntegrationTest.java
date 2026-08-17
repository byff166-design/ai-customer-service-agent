package com.chenxuekun.aicustomer.service;

import com.chenxuekun.aicustomer.entity.SupportTicket;
import com.chenxuekun.aicustomer.exception.BusinessException;
import com.chenxuekun.aicustomer.exception.ResourceNotFoundException;
import com.chenxuekun.aicustomer.mapper.SupportTicketMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class TicketServiceIntegrationTest {
    @Autowired
    private TicketService ticketService;
    @Autowired
    private SupportTicketMapper ticketMapper;

    @BeforeEach
    void cleanTickets() {
        ticketMapper.delete(null);
    }

    @Test
    void shouldCreateAndQueryTicket() {
        SupportTicket created = ticketService.create("ord1001", "包装破损", "high");

        SupportTicket loaded = ticketService.getByTicketNo(created.getTicketNo());
        assertThat(loaded.getOrderNo()).isEqualTo("ORD1001");
        assertThat(loaded.getPriority()).isEqualTo("HIGH");
        assertThat(loaded.getStatus()).isEqualTo("OPEN");
    }

    @Test
    void shouldPreventDuplicateOpenTicket() {
        ticketService.create("ORD1001", "包装破损", "NORMAL");

        assertThatThrownBy(() -> ticketService.create("ORD1001", "再次提交", "NORMAL"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已有处理中工单");
    }

    @Test
    void shouldRejectUnknownOrder() {
        assertThatThrownBy(() -> ticketService.create("ORD9999", "商品损坏", "NORMAL"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThatThrownBy(() -> ticketService.create("ORD1001", " ", "NORMAL"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不能为空");
    }
}
