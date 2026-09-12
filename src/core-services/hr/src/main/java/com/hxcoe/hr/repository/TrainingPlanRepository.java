package com.hxcoe.hr.repository;

import com.hxcoe.hr.entity.TrainingPlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * 培训计划Repository
 */
public interface TrainingPlanRepository extends JpaRepository<TrainingPlanEntity, Long>, JpaSpecificationExecutor<TrainingPlanEntity> {

    /**
     * 根据状态查询培训计划
     * @param status 状态：0计划中 1进行中 2已完成 3已取消
     * @return 培训计划列表
     */
    List<TrainingPlanEntity> findByStatus(Integer status);
}
