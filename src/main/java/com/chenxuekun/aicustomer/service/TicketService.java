package com.chenxuekun.aicustomer.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chenxuekun.aicustomer.entity.SupportTicket;
import com.chenxuekun.aicustomer.exception.BusinessException;
import com.chenxuekun.aicustomer.exception.ResourceNotFoundException;
import com.chenxuekun.aicustomer.mapper.SupportTicketMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class TicketService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final SupportTicketMapper ticketMapper;
    private final OrderService orderService;

    public TicketService(SupportTicketMapper ticketMapper, OrderService orderService) {
        this.ticketMapper = ticketMapper;
        this.orderService = orderService;
    }

    @Transactional
    public SupportTicket create(String orderNo, String description, String priority) {
        String normalizedOrderNo = normalize(orderNo);
        orderService.getByOrderNo(normalizedOrderNo);
        if (description == null || description.isBlank()) {
            throw new BusinessException("问题描述不能为空");
        }

        SupportTicket duplicate = ticketMapper.selectOne(Wrappers.<SupportTicket>lambdaQuery()
                .eq(SupportTicket::getOrderNo, normalizedOrderNo)
                .eq(SupportTicket::getStatus, "OPEN")
                .last("LIMIT 1"));
        if (duplicate != null) {
            throw new BusinessException("该订单已有处理中工单：" + duplicate.getTicketNo());
        }

        LocalDateTime now = LocalDateTime.now();
        SupportTicket ticket = new SupportTicket();
        ticket.setTicketNo(generateTicketNo(now));
        ticket.setOrderNo(normalizedOrderNo);
        ticket.setProblemDescription(description.trim());
        ticket.setStatus("OPEN");
        ticket.setPriority(normalizePriority(priority));
        ticket.setCreatedAt(now);
        ticket.setUpdatedAt(now);
        ticketMapper.insert(ticket);
        return ticket;
    }

    public SupportTicket getByTicketNo(String ticketNo) {
        SupportTicket ticket = ticketMapper.selectOne(Wrappers.<SupportTicket>lambdaQuery()
                .eq(SupportTicket::getTicketNo, normalize(ticketNo)));
        if (ticket == null) {
            throw new ResourceNotFoundException("未找到工单：" + ticketNo);
        }
        return ticket;
    }

    public List<SupportTicket> listRecent() {
        return ticketMapper.selectList(Wrappers.<SupportTicket>lambdaQuery()
                .orderByDesc(SupportTicket::getCreatedAt)
                .last("LIMIT 20"));
    }

    private String generateTicketNo(LocalDateTime now) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "TK" + now.format(DATE_FORMAT) + suffix;
    }

    private String normalizePriority(String priority) {
        if (priority == null) {
            return "NORMAL";
        }
        return switch (priority.trim().toUpperCase()) {
            case "HIGH", "URGENT" -> "HIGH";
            case "LOW" -> "LOW";
            default -> "NORMAL";
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
