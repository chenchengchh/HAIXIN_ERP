package com.hxcoe.scm.service.impl;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.PurchaseOrderEntity;
import com.hxcoe.scm.repository.PurchaseOrderRepository;
import com.hxcoe.scm.service.PurchaseOrderEventOutboxService;
import com.hxcoe.scm.service.PurchaseOrderService;
import com.hxcoe.scm.service.ScmPoAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.time.LocalDateTime;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository orderRepository;

    @Autowired
    private PurchaseOrderEventOutboxService purchaseOrderEventOutboxService;

    @Autowired
    private ScmPoAuditService scmPoAuditService;

    @Override
    public Result<PurchaseOrderEntity> createOrder(PurchaseOrderEntity order) {
        if (order.getPurchaseType() == null) {
            order.setPurchaseType(0);
        }
        if (order.getOrderStatus() == null) {
            order.setOrderStatus(10);
        }
        if (order.getSupplierCode() == null || order.getSupplierCode().isBlank()) {
            order.setSupplierCode("UNKNOWN");
        }
        order.setCreatedTime(LocalDateTime.now());
        order.setUpdatedTime(LocalDateTime.now());
        PurchaseOrderEntity savedOrder = orderRepository.save(order);
        purchaseOrderEventOutboxService.enqueuePoEvent(savedOrder.getOrderNo(), "PO_CREATED");
        scmPoAuditService.recordStatusChange(savedOrder.getOrderNo(), null, savedOrder.getOrderStatus(), "PO_CREATED", savedOrder.getCreatedBy(), Map.of(
                "source", "scm",
                "action", "CREATE"
        ));
        return Result.success(savedOrder);
    }

    @Override
    public Result<PurchaseOrderEntity> updateOrder(Long id, PurchaseOrderEntity order) {
        PurchaseOrderEntity existingOrder = orderRepository.findById(id).orElse(null);
        if (existingOrder == null) {
            return Result.error("采购订单不存在");
        }
        Integer beforeStatus = existingOrder.getOrderStatus();
        existingOrder.setOrderNo(order.getOrderNo() != null ? order.getOrderNo() : existingOrder.getOrderNo());
        existingOrder.setSupplierId(order.getSupplierId() != null ? order.getSupplierId() : existingOrder.getSupplierId());
        existingOrder.setSupplierCode(order.getSupplierCode() != null ? order.getSupplierCode() : existingOrder.getSupplierCode());
        existingOrder.setSupplierName(order.getSupplierName() != null ? order.getSupplierName() : existingOrder.getSupplierName());
        existingOrder.setOrderAmount(order.getOrderAmount() != null ? order.getOrderAmount() : existingOrder.getOrderAmount());
        existingOrder.setExpectedDeliveryDate(order.getExpectedDeliveryDate() != null ? order.getExpectedDeliveryDate() : existingOrder.getExpectedDeliveryDate());
        existingOrder.setActualDeliveryDate(order.getActualDeliveryDate() != null ? order.getActualDeliveryDate() : existingOrder.getActualDeliveryDate());
        existingOrder.setOrderStatus(order.getOrderStatus() != null ? order.getOrderStatus() : existingOrder.getOrderStatus());
        existingOrder.setPurchaseType(order.getPurchaseType() != null ? order.getPurchaseType() : existingOrder.getPurchaseType());
        existingOrder.setRemark(order.getRemark() != null ? order.getRemark() : existingOrder.getRemark());
        existingOrder.setUpdatedTime(LocalDateTime.now());
        PurchaseOrderEntity updatedOrder = orderRepository.save(existingOrder);
        String eventType = "PO_UPDATED";
        if (updatedOrder.getOrderStatus() != null) {
            if (updatedOrder.getOrderStatus() == 30 && (beforeStatus == null || beforeStatus != 30)) {
                eventType = "PO_APPROVED";
            } else if (updatedOrder.getOrderStatus() == 70 && (beforeStatus == null || beforeStatus != 70)) {
                eventType = "PO_CLOSED";
            } else if (updatedOrder.getOrderStatus() == 90 && (beforeStatus == null || beforeStatus != 90)) {
                eventType = "PO_CANCELLED";
            }
        }
        purchaseOrderEventOutboxService.enqueuePoEvent(updatedOrder.getOrderNo(), eventType);
        Integer afterStatus = updatedOrder.getOrderStatus();
        if (afterStatus != null && (beforeStatus == null || !afterStatus.equals(beforeStatus))) {
            scmPoAuditService.recordStatusChange(updatedOrder.getOrderNo(), beforeStatus, afterStatus, eventType, updatedOrder.getUpdatedBy(), Map.of(
                    "source", "scm",
                    "action", "STATUS_CHANGE"
            ));
        }
        return Result.success(updatedOrder);
    }

    @Override
    public Result<Void> deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            return Result.error("采购订单不存在");
        }
        orderRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<PurchaseOrderEntity> getOrderById(Long id) {
        PurchaseOrderEntity order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            return Result.error("采购订单不存在");
        }
        return Result.success(order);
    }

    @Override
    public Result<PurchaseOrderEntity> getOrderByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            return Result.error("orderNo不能为空");
        }
        PurchaseOrderEntity order = orderRepository.findByOrderNo(orderNo).orElse(null);
        if (order == null) {
            return Result.error("采购订单不存在");
        }
        return Result.success(order);
    }

    @Override
    public Result<PageResult<PurchaseOrderEntity>> getOrdersByPage(Pageable pageable) {
        Page<PurchaseOrderEntity> page = orderRepository.findAll(pageable);
        PageResult<PurchaseOrderEntity> pageResult = PageResult.build(
            page.getTotalElements(), 
            (int) page.getSize(), 
            (int) (page.getNumber() + 1), 
            page.getContent()
        );
        return Result.success(pageResult);
    }
}
