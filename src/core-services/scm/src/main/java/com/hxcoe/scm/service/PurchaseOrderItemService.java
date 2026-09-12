package com.hxcoe.scm.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.PurchaseOrderItemEntity;
import org.springframework.data.domain.Pageable;

public interface PurchaseOrderItemService {
    Result<PurchaseOrderItemEntity> createOrderItem(PurchaseOrderItemEntity item);
    Result<PurchaseOrderItemEntity> updateOrderItem(Long id, PurchaseOrderItemEntity item);
    Result<Void> deleteOrderItem(Long id);
    Result<PurchaseOrderItemEntity> getOrderItemById(Long id);
    Result<java.util.List<PurchaseOrderItemEntity>> getOrderItemsByOrderId(Long orderId);
    Result<PageResult<PurchaseOrderItemEntity>> getOrderItemsByPage(Pageable pageable);
}