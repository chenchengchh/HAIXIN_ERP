package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.PerformanceBonusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 绩效奖金Repository
 */
public interface PerformanceBonusRepository extends JpaRepository<PerformanceBonusEntity, Long>, JpaSpecificationExecutor<PerformanceBonusEntity> {

    /**
     * 根据考核周期查询绩效奖金
     * @param appraisalPeriod 考核周期
     * @return 绩效奖金列表
     */
    List<PerformanceBonusEntity> findByAppraisalPeriod(String appraisalPeriod);

    /**
     * 根据状态查询绩效奖金
     * @param status 状态（0待审批 1已批准 2已发放）
     * @return 绩效奖金列表
     */
    List<PerformanceBonusEntity> findByStatus(Integer status);
}
