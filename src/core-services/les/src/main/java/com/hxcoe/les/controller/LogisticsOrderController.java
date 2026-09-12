package com.hxcoe.les.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.les.entity.LogisticsOrderEntity;
import com.hxcoe.les.service.LogisticsOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/les/orders", "/les/orders"})
public class LogisticsOrderController {

    @Autowired
    private LogisticsOrderService orderService;

    @PostMapping
    public Result<LogisticsOrderEntity> createOrder(@RequestBody LogisticsOrderEntity order) {
        return orderService.createOrder(order);
    }

    @PutMapping("/{id}")
    public Result<LogisticsOrderEntity> updateOrder(@PathVariable Long id, @RequestBody LogisticsOrderEntity order) {
        return orderService.updateOrder(id, order);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        return orderService.deleteOrder(id);
    }

    @GetMapping("/{id}")
    public Result<LogisticsOrderEntity> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/page")
    public Result<PageResult<LogisticsOrderEntity>> getOrdersByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderService.getOrdersByPage(pageable);
    }
}