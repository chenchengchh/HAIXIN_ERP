package com.hxcoe.erp.repository;

import com.hxcoe.erp.entity.CostCalculationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 成本核算Repository接口
 */
@Repository
public interface CostCalculationRepository extends JpaRepository<CostCalculationEntity, Long>, JpaSpecificationExecutor<CostCalculationEntity> {

    /**
     * 根据核算日期范围查询
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 成本核算实体列表
     */
    List<CostCalculationEntity> findByCalculationDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 根据核算状态查询
     *
     * @param status 状态
     * @return 成本核算实体列表
     */
    List<CostCalculationEntity> findByStatus(String status);
}
