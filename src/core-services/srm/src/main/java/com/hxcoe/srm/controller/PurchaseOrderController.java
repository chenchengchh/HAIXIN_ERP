package com.hxcoe.srm.controller;

import com.hxcoe.srm.entity.PurchaseOrderEntity;
import com.hxcoe.srm.service.PurchaseOrderService;
import com.hxcoe.common.result.Result;
import com.hxcoe.srm.client.ScmPurchaseOrderClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/srm")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private ScmPurchaseOrderClient scmPurchaseOrderClient;

    @Value("${srm.procurement.po-write-enabled:false}")
    private boolean poWriteEnabled;

    @PostMapping("/purchase-orders")
    public Result<PurchaseOrderEntity> createPurchaseOrder(@RequestBody PurchaseOrderEntity purchaseOrder) {
        if (!poWriteEnabled) {
            return Result.error("采购订单已由SCM管理，请使用 /api/v1/scm/purchase-orders");
        }
        PurchaseOrderEntity result = purchaseOrderService.createPurchaseOrder(purchaseOrder);
        return Result.success("采购订单创建成功", result);
    }

    @GetMapping("/purchase-orders")
    public Result<Object> getPurchaseOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        int scmPage = page + 1;
        return scmPurchaseOrderClient.getOrders(scmPage, size);
    }

    @GetMapping("/purchase-orders/{id}")
    public Result<Object> getPurchaseOrderById(@PathVariable Long id) {
        Optional<PurchaseOrderEntity> local = purchaseOrderService.getPurchaseOrderById(id);
        if (local.isEmpty() || local.get().getOrderNo() == null || local.get().getOrderNo().isBlank()) {
            return Result.fail("采购订单不存在");
        }
        return scmPurchaseOrderClient.getByOrderNo(local.get().getOrderNo());
    }

    @PostMapping("/purchase-orders/{id}/confirm")
    public Result<Object> supplierConfirm(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Optional<PurchaseOrderEntity> local = purchaseOrderService.getPurchaseOrderById(id);
        if (local.isEmpty() || local.get().getOrderNo() == null || local.get().getOrderNo().isBlank()) {
            return Result.fail("采购订单不存在");
        }
        return scmPurchaseOrderClient.supplierConfirm(local.get().getOrderNo(), body);
    }

    @PutMapping("/purchase-orders/{id}")
    public Result<PurchaseOrderEntity> updatePurchaseOrder(@PathVariable Long id, @RequestBody PurchaseOrderEntity purchaseOrder) {
        if (!poWriteEnabled) {
            return Result.error("采购订单已由SCM管理，请使用 /api/v1/scm/purchase-orders");
        }
        PurchaseOrderEntity result = purchaseOrderService.updatePurchaseOrder(id, purchaseOrder);
        if (result != null) {
            return Result.success("采购订单更新成功", result);
        } else {
            return Result.fail("采购订单不存在");
        }
    }

    @DeleteMapping("/purchase-orders/{id}")
    public Result<String> deletePurchaseOrder(@PathVariable Long id) {
        if (!poWriteEnabled) {
            return Result.error("采购订单已由SCM管理，请使用 /api/v1/scm/purchase-orders");
        }
        purchaseOrderService.deletePurchaseOrder(id);
        return Result.success("采购订单删除成功");
    }
}
