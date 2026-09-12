package com.hxcoe.srm.service.impl;

import com.hxcoe.srm.entity.PurchaseOrderEntity;
import com.hxcoe.srm.repository.PurchaseOrderRepository;
import com.hxcoe.srm.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Value("${srm.procurement.po-write-enabled:false}")
    private boolean poWriteEnabled;

    @Override
    public PurchaseOrderEntity createPurchaseOrder(PurchaseOrderEntity purchaseOrder) {
        if (!poWriteEnabled) {
            throw new IllegalStateException("采购订单已由SCM管理");
        }
        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Override
    public Page<PurchaseOrderEntity> getPurchaseOrders(Pageable pageable) {
        return purchaseOrderRepository.findAll(pageable);
    }

    @Override
    public Optional<PurchaseOrderEntity> getPurchaseOrderById(Long id) {
        return purchaseOrderRepository.findById(id);
    }

    @Override
    public PurchaseOrderEntity updatePurchaseOrder(Long id, PurchaseOrderEntity purchaseOrder) {
        if (!poWriteEnabled) {
            throw new IllegalStateException("采购订单已由SCM管理");
        }
        if (purchaseOrderRepository.existsById(id)) {
            purchaseOrder.setId(id);
            return purchaseOrderRepository.save(purchaseOrder);
        }
        return null;
    }

    @Override
    public void deletePurchaseOrder(Long id) {
        if (!poWriteEnabled) {
            throw new IllegalStateException("采购订单已由SCM管理");
        }
        purchaseOrderRepository.deleteById(id);
    }
}
