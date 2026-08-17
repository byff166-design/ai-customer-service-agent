package com.chenxuekun.aicustomer.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.chenxuekun.aicustomer.entity.LogisticsInfo;
import com.chenxuekun.aicustomer.exception.ResourceNotFoundException;
import com.chenxuekun.aicustomer.mapper.LogisticsInfoMapper;
import org.springframework.stereotype.Service;

@Service
public class LogisticsService {
    private final LogisticsInfoMapper logisticsMapper;

    public LogisticsService(LogisticsInfoMapper logisticsMapper) {
        this.logisticsMapper = logisticsMapper;
    }

    public LogisticsInfo getByOrderNo(String orderNo) {
        LogisticsInfo logistics = logisticsMapper.selectOne(Wrappers.<LogisticsInfo>lambdaQuery()
                .eq(LogisticsInfo::getOrderNo, normalize(orderNo)));
        if (logistics == null) {
            throw new ResourceNotFoundException("订单暂无物流信息：" + orderNo);
        }
        return logistics;
    }

    private String normalize(String orderNo) {
        return orderNo == null ? "" : orderNo.trim().toUpperCase();
    }
}
