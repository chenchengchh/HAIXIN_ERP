package com.hxcoe.scm.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.PurchaseOrderEntity;
import org.springframework.data.domain.Pageable;

public interface PurchaseOrderService {
    Result<PurchaseOrderEntity> createOrder(PurchaseOrderEntity order);
    Result<PurchaseOrderEntity> updateOrder(Long id, PurchaseOrderEntity order);
    Result<Void> deleteOrder(Long id);
    Result<PurchaseOrderEntity> getOrderById(Long id);
    Result<PurchaseOrderEntity> getOrderByOrderNo(String orderNo);
    Result<PageResult<PurchaseOrderEntity>> getOrdersByPage(Pageable pageable);
}
