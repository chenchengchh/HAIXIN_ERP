package com.hxcoe.mes.repository;

import com.hxcoe.mes.entity.ProductionReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * 生产报工数据访问接口
 */
@Repository
public interface ProductionReportRepository extends JpaRepository<ProductionReportEntity, Long>, JpaSpecificationExecutor<ProductionReportEntity> {
    
    // 根据报工单号查询报工记录
    ProductionReportEntity findByReportNo(String reportNo);
    
    // 根据工单号查询报工记录列表
    java.util.List<ProductionReportEntity> findByWorkOrderNo(String workOrderNo);
    
    // 根据操作员姓名查询报工记录列表
    java.util.List<ProductionReportEntity> findByOperatorName(String operatorName);
    
    // 根据状态查询报工记录列表
    java.util.List<ProductionReportEntity> findByStatus(String status);
}
