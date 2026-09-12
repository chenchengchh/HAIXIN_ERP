package com.hxcoe.scm.repository;

import com.hxcoe.scm.entity.PurchaseOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, Long>, JpaSpecificationExecutor<PurchaseOrderEntity> {
    Optional<PurchaseOrderEntity> findByOrderNo(String orderNo);
    Page<PurchaseOrderEntity> findByOrderStatus(Integer orderStatus, Pageable pageable);
    List<PurchaseOrderEntity> findByOrderNoIn(List<String> orderNos);
}
