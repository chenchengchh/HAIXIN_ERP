package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.ErpSalesOrderMirrorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * ERP销售订单镜像仓库
 */
@Repository
public interface ErpSalesOrderMirrorRepository extends JpaRepository<ErpSalesOrderMirrorEntity, Long> {

    /**
     * 按订单编号查询镜像记录（幂等查重）
     *
     * @param orderNo CRM订单编号
     * @return 镜像记录
     */
    Optional<ErpSalesOrderMirrorEntity> findByOrderNo(String orderNo);
}
