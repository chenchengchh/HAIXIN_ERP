package com.hxcoe.les.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.les.entity.LogisticsOrderEntity;
import org.springframework.data.domain.Pageable;

public interface LogisticsOrderService {
    Result<LogisticsOrderEntity> createOrder(LogisticsOrderEntity order);
    Result<LogisticsOrderEntity> updateOrder(Long id, LogisticsOrderEntity order);
    Result<Void> deleteOrder(Long id);
    Result<LogisticsOrderEntity> getOrderById(Long id);
    Result<PageResult<LogisticsOrderEntity>> getOrdersByPage(Pageable pageable);
}