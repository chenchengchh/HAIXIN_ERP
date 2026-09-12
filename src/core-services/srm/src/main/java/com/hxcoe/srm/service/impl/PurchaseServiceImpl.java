package com.hxcoe.srm.service.impl;

import com.hxcoe.srm.entity.PurchaseOrderEntity;
import com.hxcoe.srm.entity.PurchaseOrderItemEntity;
import com.hxcoe.srm.repository.PurchaseOrderRepository;
import com.hxcoe.srm.service.PurchaseService;
import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Value("${srm.procurement.po-write-enabled:false}")
    private boolean poWriteEnabled;

    @Override
    @Transactional
    public Result<PurchaseOrderEntity> createPurchaseOrder(PurchaseOrderEntity order) {
        if (!poWriteEnabled) {
            return Result.error("采购订单已由SCM管理，请使用 /api/v1/scm/purchase-orders");
        }
        if (purchaseOrderRepository.findByOrderNo(order.getOrderNo()) != null) {
            return Result.error("订单号已存在");
        }
        
        // 关联子项
        if (order.getItems() != null) {
            for (PurchaseOrderItemEntity item : order.getItems()) {
                item.setPurchaseOrder(order);
            }
        }
        
        order.setCreatedTime(LocalDateTime.now());
        order.setUpdatedTime(LocalDateTime.now());
        order.setStatus("CREATED");
        PurchaseOrderEntity saved = purchaseOrderRepository.save(order);
        return Result.success(saved);
    }

    @Override
    @Transactional
    public Result<PurchaseOrderEntity> updatePurchaseOrder(Long id, PurchaseOrderEntity order) {
        if (!poWriteEnabled) {
            return Result.error("采购订单已由SCM管理，请使用 /api/v1/scm/purchase-orders");
        }
        PurchaseOrderEntity existing = purchaseOrderRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("订单不存在");
        }
        
        // 关联子项
        if (order.getItems() != null) {
            for (PurchaseOrderItemEntity item : order.getItems()) {
                item.setPurchaseOrder(order);
            }
        }
        
        order.setId(id);
        order.setCreatedTime(existing.getCreatedTime());
        order.setUpdatedTime(LocalDateTime.now());
        PurchaseOrderEntity updated = purchaseOrderRepository.save(order);
        return Result.success(updated);
    }

    @Override
    public Result<Void> deletePurchaseOrder(Long id) {
        if (!poWriteEnabled) {
            return Result.error("采购订单已由SCM管理，请使用 /api/v1/scm/purchase-orders");
        }
        if (!purchaseOrderRepository.existsById(id)) {
            return Result.error("订单不存在");
        }
        purchaseOrderRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<PurchaseOrderEntity> getPurchaseOrderById(Long id) {
        PurchaseOrderEntity order = purchaseOrderRepository.findById(id).orElse(null);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @Override
    public Result<PageResult<PurchaseOrderEntity>> getPurchaseOrdersByPage(Pageable pageable) {
        Page<PurchaseOrderEntity> page = purchaseOrderRepository.findAll(pageable);
        PageResult<PurchaseOrderEntity> pageResult = PageResult.build(
            page.getTotalElements(), 
            Math.toIntExact(page.getSize()), 
            page.getNumber() + 1, 
            page.getContent()
        );
        return Result.success(pageResult);
    }
}
