package com.chenxuekun.aicustomer.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chenxuekun.aicustomer.entity.CustomerOrder;
import com.chenxuekun.aicustomer.exception.ResourceNotFoundException;
import com.chenxuekun.aicustomer.mapper.CustomerOrderMapper;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final CustomerOrderMapper orderMapper;

    public OrderService(CustomerOrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }

    public CustomerOrder getByOrderNo(String orderNo) {
        CustomerOrder order = orderMapper.selectOne(Wrappers.<CustomerOrder>lambdaQuery()
                .eq(CustomerOrder::getOrderNo, normalize(orderNo)));
        if (order == null) {
            throw new ResourceNotFoundException("未找到订单：" + orderNo);
        }
        return order;
    }

    private String normalize(String orderNo) {
        return orderNo == null ? "" : orderNo.trim().toUpperCase();
    }
}
