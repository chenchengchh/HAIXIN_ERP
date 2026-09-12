package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.TrainingPlanEntity;
import com.hxcoe.hr.repository.TrainingPlanRepository;
import com.hxcoe.hr.service.TrainingPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 培训计划Service实现类
 */
@Service
public class TrainingPlanServiceImpl implements TrainingPlanService {

    @Autowired
    private TrainingPlanRepository trainingPlanRepository;

    /**
     * 创建培训计划
     * @param trainingPlan 培训计划实体
     * @return 培训计划实体
     */
    @Override
    public TrainingPlanEntity createTrainingPlan(TrainingPlanEntity trainingPlan) {
        return trainingPlanRepository.save(trainingPlan);
    }

    /**
     * 根据ID查询培训计划
     * @param id 培训计划ID
     * @return 培训计划实体
     */
    @Override
    public TrainingPlanEntity getTrainingPlanById(Long id) {
        return trainingPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("记录不存在: id=" + id));
    }

    /**
     * 更新培训计划
     * @param id 培训计划ID
     * @param trainingPlan 培训计划实体
     * @return 培训计划实体
     */
    @Override
    public TrainingPlanEntity updateTrainingPlan(Long id, TrainingPlanEntity trainingPlan) {
        TrainingPlanEntity existingPlan = getTrainingPlanById(id);
        // 更新培训计划字段
        existingPlan.setTrainingName(trainingPlan.getTrainingName());
        existingPlan.setTrainer(trainingPlan.getTrainer());
        existingPlan.setStartDate(trainingPlan.getStartDate());
        existingPlan.setEndDate(trainingPlan.getEndDate());
        existingPlan.setLocation(trainingPlan.getLocation());
        existingPlan.setStatus(trainingPlan.getStatus());
        existingPlan.setDescription(trainingPlan.getDescription());
        existingPlan.setRemark(trainingPlan.getRemark());
        return trainingPlanRepository.save(existingPlan);
    }

    /**
     * 删除培训计划
     * @param id 培训计划ID
     */
    @Override
    public void deleteTrainingPlan(Long id) {
        TrainingPlanEntity existingPlan = getTrainingPlanById(id);
        trainingPlanRepository.delete(existingPlan);
    }

    /**
     * 查询全部培训计划
     * @return 培训计划列表
     */
    @Override
    public List<TrainingPlanEntity> getAllTrainingPlans() {
        return trainingPlanRepository.findAll();
    }

    /**
     * 分页查询培训计划
     * @param pageable 分页参数
     * @return 培训计划分页列表
     */
    @Override
    public Page<TrainingPlanEntity> getTrainingPlansByPage(Pageable pageable) {
        return trainingPlanRepository.findAll(pageable);
    }
}
