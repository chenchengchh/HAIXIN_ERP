package com.hxcoe.les.service.impl;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.les.entity.LogisticsOrderEntity;
import com.hxcoe.les.repository.LogisticsOrderRepository;
import com.hxcoe.les.service.LogisticsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogisticsOrderServiceImpl implements LogisticsOrderService {

    @Autowired
    private LogisticsOrderRepository orderRepository;

    @Override
    public Result<LogisticsOrderEntity> createOrder(LogisticsOrderEntity order) {
        order.setCreatedTime(LocalDateTime.now());
        LogisticsOrderEntity savedOrder = orderRepository.save(order);
        return Result.success(savedOrder);
    }

    @Override
    public Result<LogisticsOrderEntity> updateOrder(Long id, LogisticsOrderEntity order) {
        LogisticsOrderEntity existingOrder = orderRepository.findById(id).orElse(null);
        if (existingOrder == null) {
            return Result.error("物流订单不存在");
        }
        order.setId(id);
        order.setUpdatedTime(LocalDateTime.now());
        LogisticsOrderEntity updatedOrder = orderRepository.save(order);
        return Result.success(updatedOrder);
    }

    @Override
    public Result<Void> deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            return Result.error("物流订单不存在");
        }
        orderRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<LogisticsOrderEntity> getOrderById(Long id) {
        LogisticsOrderEntity order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return Result.error("物流订单不存在");
        }
        return Result.success(order);
    }

    @Override
    public Result<PageResult<LogisticsOrderEntity>> getOrdersByPage(Pageable pageable) {
        Page<LogisticsOrderEntity> page = orderRepository.findAll(pageable);
        PageResult<LogisticsOrderEntity> pageResult = PageResult.build(
            page.getTotalElements(), 
            (int) page.getSize(), 
            (int) (page.getNumber() + 1), 
            page.getContent()
        );
        return Result.success(pageResult);
    }
}