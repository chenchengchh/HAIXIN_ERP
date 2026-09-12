package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.InspectionPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * 检验计划仓库
 */
public interface InspectionPlanRepository extends JpaRepository<InspectionPlanEntity, Long>, JpaSpecificationExecutor<InspectionPlanEntity> {

    /**
     * 根据计划编号查询
     *
     * @param planNo 计划编号
     * @return 计划
     */
    Optional<InspectionPlanEntity> findByPlanNo(String planNo);
}
