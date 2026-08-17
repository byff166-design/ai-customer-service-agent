package com.chenxuekun.aicustomer.agent.tool;

import com.chenxuekun.aicustomer.agent.ToolInvocationContext;
import com.chenxuekun.aicustomer.entity.CustomerOrder;
import com.chenxuekun.aicustomer.entity.LogisticsInfo;
import com.chenxuekun.aicustomer.entity.SupportTicket;
import com.chenxuekun.aicustomer.service.FaqService;
import com.chenxuekun.aicustomer.service.LogisticsService;
import com.chenxuekun.aicustomer.service.OrderService;
import com.chenxuekun.aicustomer.service.TicketService;
import com.chenxuekun.aicustomer.service.ToolCallLogService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class CustomerServiceTools {
    private final OrderService orderService;
    private final LogisticsService logisticsService;
    private final TicketService ticketService;
    private final FaqService faqService;
    private final ToolCallLogService logService;
    private final ToolInvocationContext context;

    public CustomerServiceTools(OrderService orderService,
                                LogisticsService logisticsService,
                                TicketService ticketService,
                                FaqService faqService,
                                ToolCallLogService logService,
                                ToolInvocationContext context) {
        this.orderService = orderService;
        this.logisticsService = logisticsService;
        this.ticketService = ticketService;
        this.faqService = faqService;
        this.logService = logService;
        this.context = context;
    }

    @Tool("根据订单号查询订单详情。用户未提供订单号时不要调用。")
    public String getOrderDetail(@P("订单号，例如 ORD1001") String orderNo) {
        return execute("getOrderDetail", orderNo, () -> {
            CustomerOrder order = orderService.getByOrderNo(orderNo);
            return "订单号=" + order.getOrderNo() + "，客户=" + order.getCustomerName()
                    + "，状态=" + order.getStatus() + "，金额=" + order.getAmount() + "元";
        });
    }

    @Tool("根据订单号查询物流公司、运单号和最新物流状态。")
    public String getLogistics(@P("订单号，例如 ORD1001") String orderNo) {
        return execute("getLogistics", orderNo, () -> {
            LogisticsInfo info = logisticsService.getByOrderNo(orderNo);
            return "物流公司=" + info.getCompany() + "，运单号=" + info.getTrackingNo()
                    + "，最新状态=" + info.getStatus() + "，更新时间=" + info.getUpdatedAt();
        });
    }

    @Tool("查询退款、退货、发货、运费、发票、修改地址等售后政策。")
    public String searchFaq(@P("需要查询的政策关键词") String keyword) {
        return execute("searchFaq", keyword, () -> faqService.search(keyword)
                .orElse("未找到相关政策依据，请明确告知用户不知道并建议转人工。"));
    }

    @Tool("为存在的订单创建售后工单。同一订单已有处理中工单时不能重复创建。")
    public String createTicket(@P("订单号") String orderNo,
                               @P("用户问题的完整描述") String problemDescription,
                               @P("优先级：LOW、NORMAL 或 HIGH") String priority) {
        return execute("createTicket", orderNo + ":" + problemDescription, () -> {
            SupportTicket ticket = ticketService.create(orderNo, problemDescription, priority);
            return "工单创建成功，工单号=" + ticket.getTicketNo() + "，状态=" + ticket.getStatus()
                    + "，优先级=" + ticket.getPriority();
        });
    }

    @Tool("根据工单号查询售后工单状态。")
    public String getTicketStatus(@P("工单号") String ticketNo) {
        return execute("getTicketStatus", ticketNo, () -> {
            SupportTicket ticket = ticketService.getByTicketNo(ticketNo);
            return "工单号=" + ticket.getTicketNo() + "，订单号=" + ticket.getOrderNo()
                    + "，状态=" + ticket.getStatus() + "，问题=" + ticket.getProblemDescription();
        });
    }

    @Tool("当用户明确要求人工，或者工具失败、资料不足无法可靠回答时，登记转人工。")
    public String transferToHuman(@P("转人工原因") String reason) {
        return execute("transferToHuman", reason,
                () -> "已登记转人工客服，原因=" + reason + "。人工客服会尽快处理。 ");
    }

    private String execute(String toolName, String request, Supplier<String> action) {
        context.record(toolName);
        try {
            String result = action.get();
            logService.record(context.sessionId(), toolName, request, result, true);
            return result;
        } catch (RuntimeException exception) {
            context.markFailed();
            String result = "工具执行失败：" + exception.getMessage();
            logService.record(context.sessionId(), toolName, request, result, false);
            return result;
        }
    }
}
