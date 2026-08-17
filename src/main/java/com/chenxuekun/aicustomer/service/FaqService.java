package com.chenxuekun.aicustomer.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FaqService {
    private final Map<String, String> policies = new LinkedHashMap<>();

    public FaqService() {
        policies.put("退款", "未发货订单可申请退款；已发货订单需先拒收或签收后发起退货申请。退款原路退回，通常需要1至5个工作日。");
        policies.put("运费", "因质量问题产生的退换货运费由平台承担；个人原因退货的运费由用户承担。");
        policies.put("退货", "商品签收后7天内、保持完好且不影响二次销售时，可申请无理由退货；定制和易耗类商品除外。");
        policies.put("发货", "现货商品通常在付款后48小时内发货；预售商品以商品页面承诺时间为准。");
        policies.put("发票", "订单完成后可在订单详情页申请电子发票，通常在24小时内发送至预留邮箱。");
        policies.put("修改地址", "订单未进入仓库打包前可联系客服尝试修改地址；已经发货的订单无法直接修改。");
    }

    public Optional<String> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Optional.empty();
        }
        String normalized = keyword.trim();
        return policies.entrySet().stream()
                .filter(entry -> normalized.contains(entry.getKey()) || entry.getKey().contains(normalized))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
