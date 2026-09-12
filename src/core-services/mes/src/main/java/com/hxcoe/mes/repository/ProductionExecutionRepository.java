package com.hxcoe.mes.repository;

import com.hxcoe.mes.entity.ProductionExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 生产执行Repository接口
 */
@Repository
public interface ProductionExecutionRepository extends JpaRepository<ProductionExecutionEntity, Long>, JpaSpecificationExecutor<ProductionExecutionEntity> {

    /**
     * 根据生产执行单号查询
     *
     * @param executionNo 生产执行单号
     * @return 生产执行实体
     */
    ProductionExecutionEntity findByExecutionNo(String executionNo);

    /**
     * 根据生产订单号查询
     *
     * @param productionOrderNo 生产订单号
     * @return 生产执行实体列表
     */
    List<ProductionExecutionEntity> findByProductionOrderNo(String productionOrderNo);

    /**
     * 根据产品编码查询
     *
     * @param productCode 产品编码
     * @return 生产执行实体列表
     */
    List<ProductionExecutionEntity> findByProductCode(String productCode);

    /**
     * 根据生产状态查询
     *
     * @param executionStatus 生产状态
     * @return 生产执行实体列表
     */
    List<ProductionExecutionEntity> findByExecutionStatus(Integer executionStatus);

    /**
     * 根据生产线查询
     *
     * @param productionLine 生产线
     * @return 生产执行实体列表
     */
    List<ProductionExecutionEntity> findByProductionLine(String productionLine);

    /**
     * 根据开始时间范围查询
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 生产执行实体列表
     */
    List<ProductionExecutionEntity> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
}
