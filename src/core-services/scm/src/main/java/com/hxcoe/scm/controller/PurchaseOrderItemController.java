package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResultAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.PurchaseOrderItemEntity;
import com.hxcoe.scm.service.PurchaseOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/scm/purchase-order-items", "/scm/purchase-order-items"})
public class PurchaseOrderItemController {

    @Autowired
    private PurchaseOrderItemService orderItemService;

    @PostMapping
    public ApiResponse<PurchaseOrderItemEntity> createOrderItem(@RequestBody PurchaseOrderItemEntity item) {
        return ResultAdapter.fromResult(orderItemService.createOrderItem(item));
    }

    @PutMapping("/{id}")
    public ApiResponse<PurchaseOrderItemEntity> updateOrderItem(@PathVariable Long id, @RequestBody PurchaseOrderItemEntity item) {
        return ResultAdapter.fromResult(orderItemService.updateOrderItem(id, item));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteOrderItem(@PathVariable Long id) {
        return ResultAdapter.fromResult(orderItemService.deleteOrderItem(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<PurchaseOrderItemEntity> getOrderItemById(@PathVariable Long id) {
        return ResultAdapter.fromResult(orderItemService.getOrderItemById(id));
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<java.util.List<PurchaseOrderItemEntity>> getOrderItemsByOrderId(@PathVariable Long orderId) {
        return ResultAdapter.fromResult(orderItemService.getOrderItemsByOrderId(orderId));
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<PurchaseOrderItemEntity>> getOrderItemsByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResultAdapter.fromResult(orderItemService.getOrderItemsByPage(pageable));
    }
}
