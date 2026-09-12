package com.hxcoe.srm.service;

import com.hxcoe.srm.entity.PurchaseOrderEntity;
import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import org.springframework.data.domain.Pageable;

public interface PurchaseService {
    Result<PurchaseOrderEntity> createPurchaseOrder(PurchaseOrderEntity order);
    Result<PurchaseOrderEntity> updatePurchaseOrder(Long id, PurchaseOrderEntity order);
    Result<Void> deletePurchaseOrder(Long id);
    Result<PurchaseOrderEntity> getPurchaseOrderById(Long id);
    Result<PageResult<PurchaseOrderEntity>> getPurchaseOrdersByPage(Pageable pageable);
}
