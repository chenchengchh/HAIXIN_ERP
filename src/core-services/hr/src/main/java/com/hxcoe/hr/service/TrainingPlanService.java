package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.TrainingPlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 培训计划Service接口
 */
public interface TrainingPlanService {

    /**
     * 创建培训计划
     * @param trainingPlan 培训计划实体
     * @return 培训计划实体
     */
    TrainingPlanEntity createTrainingPlan(TrainingPlanEntity trainingPlan);

    /**
     * 根据ID查询培训计划
     * @param id 培训计划ID
     * @return 培训计划实体
     */
    TrainingPlanEntity getTrainingPlanById(Long id);

    /**
     * 更新培训计划
     * @param id 培训计划ID
     * @param trainingPlan 培训计划实体
     * @return 培训计划实体
     */
    TrainingPlanEntity updateTrainingPlan(Long id, TrainingPlanEntity trainingPlan);

    /**
     * 删除培训计划
     * @param id 培训计划ID
     */
    void deleteTrainingPlan(Long id);

    /**
     * 查询全部培训计划
     * @return 培训计划列表
     */
    List<TrainingPlanEntity> getAllTrainingPlans();

    /**
     * 分页查询培训计划
     * @param pageable 分页参数
     * @return 培训计划分页列表
     */
    Page<TrainingPlanEntity> getTrainingPlansByPage(Pageable pageable);
}
