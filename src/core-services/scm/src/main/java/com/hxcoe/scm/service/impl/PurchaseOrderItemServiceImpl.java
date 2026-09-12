package com.hxcoe.scm.service.impl;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.PurchaseOrderItemEntity;
import com.hxcoe.scm.repository.PurchaseOrderItemRepository;
import com.hxcoe.scm.service.PurchaseOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseOrderItemServiceImpl implements PurchaseOrderItemService {

    @Autowired
    private PurchaseOrderItemRepository orderItemRepository;

    @Override
    public Result<PurchaseOrderItemEntity> createOrderItem(PurchaseOrderItemEntity item) {
        LocalDateTime now = LocalDateTime.now();
        item.setCreatedTime(now);
        item.setUpdatedTime(now);
        PurchaseOrderItemEntity savedItem = orderItemRepository.save(item);
        return Result.success(savedItem);
    }

    @Override
    public Result<PurchaseOrderItemEntity> updateOrderItem(Long id, PurchaseOrderItemEntity item) {
        PurchaseOrderItemEntity existingItem = orderItemRepository.findById(id).orElse(null);
        if (existingItem == null) {
            return Result.error("采购订单项不存在");
        }
        item.setId(id);
        item.setUpdatedTime(LocalDateTime.now());
        PurchaseOrderItemEntity updatedItem = orderItemRepository.save(item);
        return Result.success(updatedItem);
    }

    @Override
    public Result<Void> deleteOrderItem(Long id) {
        if (!orderItemRepository.existsById(id)) {
            return Result.error("采购订单项不存在");
        }
        orderItemRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<PurchaseOrderItemEntity> getOrderItemById(Long id) {
        PurchaseOrderItemEntity item = orderItemRepository.findById(id).orElse(null);
        if (item == null) {
            return Result.error("采购订单项不存在");
        }
        return Result.success(item);
    }

    @Override
    public Result<List<PurchaseOrderItemEntity>> getOrderItemsByOrderId(Long orderId) {
        List<PurchaseOrderItemEntity> items = orderItemRepository.findAll().stream()
            .filter(item -> item.getOrderId().equals(orderId))
            .toList();
        return Result.success(items);
    }

    @Override
    public Result<PageResult<PurchaseOrderItemEntity>> getOrderItemsByPage(Pageable pageable) {
        Page<PurchaseOrderItemEntity> page = orderItemRepository.findAll(pageable);
        PageResult<PurchaseOrderItemEntity> pageResult = PageResult.build(
            page.getTotalElements(), 
            (int) page.getSize(), 
            (int) (page.getNumber() + 1), 
            page.getContent()
        );
        return Result.success(pageResult);
    }
}
