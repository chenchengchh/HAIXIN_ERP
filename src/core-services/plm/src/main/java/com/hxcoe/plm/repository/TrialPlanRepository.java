package com.hxcoe.plm.repository;

import com.hxcoe.plm.entity.TrialPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TrialPlanRepository extends JpaRepository<TrialPlanEntity, Long>, JpaSpecificationExecutor<TrialPlanEntity> {

    /**
     * 按计划编码查询试产计划。
     */
    Optional<TrialPlanEntity> findByPlanCode(String planCode);
}

