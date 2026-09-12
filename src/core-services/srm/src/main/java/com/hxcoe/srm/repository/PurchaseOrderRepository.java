package com.hxcoe.srm.repository;

import com.hxcoe.srm.entity.PurchaseOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, Long> {
    PurchaseOrderEntity findByOrderNo(String orderNo);
    List<PurchaseOrderEntity> findBySupplierId(Long supplierId);
}
