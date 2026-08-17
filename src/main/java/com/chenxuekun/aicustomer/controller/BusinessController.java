package com.chenxuekun.aicustomer.controller;

import com.chenxuekun.aicustomer.entity.CustomerOrder;
import com.chenxuekun.aicustomer.entity.SupportTicket;
import com.chenxuekun.aicustomer.entity.ToolCallLog;
import com.chenxuekun.aicustomer.service.OrderService;
import com.chenxuekun.aicustomer.service.TicketService;
import com.chenxuekun.aicustomer.service.ToolCallLogService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class BusinessController {
    private final OrderService orderService;
    private final TicketService ticketService;
    private final ToolCallLogService logService;

    public BusinessController(OrderService orderService,
                              TicketService ticketService,
                              ToolCallLogService logService) {
        this.orderService = orderService;
        this.ticketService = ticketService;
        this.logService = logService;
    }

    @Operation(summary = "查询订单")
    @GetMapping("/orders/{orderNo}")
    public CustomerOrder getOrder(@PathVariable String orderNo) {
        return orderService.getByOrderNo(orderNo);
    }

    @Operation(summary = "查询工单")
    @GetMapping("/tickets/{ticketNo}")
    public SupportTicket getTicket(@PathVariable String ticketNo) {
        return ticketService.getByTicketNo(ticketNo);
    }

    @Operation(summary = "查看最近工单")
    @GetMapping("/tickets")
    public List<SupportTicket> listTickets() {
        return ticketService.listRecent();
    }

    @Operation(summary = "查看最近工具调用日志")
    @GetMapping("/tool-calls")
    public List<ToolCallLog> listToolCalls() {
        return logService.listRecent();
    }
}
