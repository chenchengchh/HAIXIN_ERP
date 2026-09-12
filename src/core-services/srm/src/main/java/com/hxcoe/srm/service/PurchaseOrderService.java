package com.hxcoe.srm.service;

import com.hxcoe.srm.entity.PurchaseOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface PurchaseOrderService {
    PurchaseOrderEntity createPurchaseOrder(PurchaseOrderEntity purchaseOrder);
    Page<PurchaseOrderEntity> getPurchaseOrders(Pageable pageable);
    Optional<PurchaseOrderEntity> getPurchaseOrderById(Long id);
    PurchaseOrderEntity updatePurchaseOrder(Long id, PurchaseOrderEntity purchaseOrder);
    void deletePurchaseOrder(Long id);
}
