package com.chenxuekun.aicustomer.agent;

import com.chenxuekun.aicustomer.agent.tool.CustomerServiceTools;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@ConditionalOnProperty(name = "app.ai.mode", havingValue = "mock", matchIfMissing = true)
public class RuleBasedCustomerServiceAgent implements CustomerServiceAgent {
    private static final Pattern ORDER_PATTERN = Pattern.compile("(?i)ORD\\d{4,}");
    private static final Pattern TICKET_PATTERN = Pattern.compile("(?i)TK[A-Z0-9]{8,}");
    private final CustomerServiceTools tools;

    public RuleBasedCustomerServiceAgent(CustomerServiceTools tools) {
        this.tools = tools;
    }

    @Override
    public String chat(String sessionId, String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        Optional<String> orderNo = extract(ORDER_PATTERN, message);
        Optional<String> ticketNo = extract(TICKET_PATTERN, message);

        if (containsAny(normalized, "人工", "真人客服", "转接客服")) {
            return tools.transferToHuman("用户主动要求人工客服");
        }

        if (ticketNo.isPresent() && containsAny(normalized, "工单", "进度", "状态")) {
            return tools.getTicketStatus(ticketNo.get());
        }

        if (containsAny(normalized, "物流", "快递", "到哪")) {
            return orderNo.map(tools::getLogistics)
                    .orElse("请提供需要查询的订单号，例如 ORD1001。");
        }

        if (containsAny(normalized, "创建工单", "提交工单", "商品坏", "商品损坏", "投诉", "售后处理")) {
            return orderNo.map(no -> tools.createTicket(no, message, inferPriority(message)))
                    .orElse("创建售后工单前，请提供订单号和具体问题。");
        }

        if (containsAny(normalized, "退款", "退货", "发货", "运费", "发票", "修改地址")) {
            return tools.searchFaq(message);
        }

        if (containsAny(normalized, "订单", "购买", "下单") || orderNo.isPresent()) {
            return orderNo.map(tools::getOrderDetail)
                    .orElse("请提供需要查询的订单号，例如 ORD1001。");
        }

        if (containsAny(normalized, "你好", "您好", "在吗")) {
            return "您好，我可以帮您查询订单、物流、售后政策和工单。您可以试试：查询 ORD1001 的物流。";
        }

        return "抱歉，我目前只处理订单、物流和售后问题。请补充订单号或说明具体诉求；需要人工服务也可以直接告诉我。";
    }

    @Override
    public String mode() {
        return "mock";
    }

    private Optional<String> extract(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? Optional.of(matcher.group().toUpperCase(Locale.ROOT)) : Optional.empty();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String inferPriority(String text) {
        return containsAny(text, "紧急", "严重", "投诉") ? "HIGH" : "NORMAL";
    }
}
