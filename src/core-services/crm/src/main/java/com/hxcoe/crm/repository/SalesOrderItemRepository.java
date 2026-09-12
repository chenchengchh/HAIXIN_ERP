package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.SalesOrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 销售订单明细Repository接口
 */
@Repository
public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItemEntity, Long>, JpaSpecificationExecutor<SalesOrderItemEntity> {
}
