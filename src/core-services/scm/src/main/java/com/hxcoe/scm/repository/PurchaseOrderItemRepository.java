package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.PurchaseOrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItemEntity, Long>, JpaSpecificationExecutor<PurchaseOrderItemEntity> {
    List<PurchaseOrderItemEntity> findByOrderId(Long orderId);
    List<PurchaseOrderItemEntity> findByOrderNo(String orderNo);
    void deleteByOrderNo(String orderNo);
    List<PurchaseOrderItemEntity> findByOrderNoAndMaterialCode(String orderNo, String materialCode);
    List<PurchaseOrderItemEntity> findByOrderNoIn(List<String> orderNos);
}
