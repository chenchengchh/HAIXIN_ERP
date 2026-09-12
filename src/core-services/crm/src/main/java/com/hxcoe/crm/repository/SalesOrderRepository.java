package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.SalesOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 销售订单 Repository
 */
@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrderEntity, Long>, JpaSpecificationExecutor<SalesOrderEntity> {
    SalesOrderEntity findByOrderNo(String orderNo);
}
