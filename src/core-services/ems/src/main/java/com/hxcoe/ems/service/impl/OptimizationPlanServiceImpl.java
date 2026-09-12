package com.hxcoe.ems.service.impl;

import com.hxcoe.ems.entity.OptimizationPlanEntity;
import com.hxcoe.ems.repository.OptimizationPlanRepository;
import com.hxcoe.ems.service.OptimizationPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 优化方案执行服务实现类
 * 实现优化方案执行相关的业务逻辑
 *
 * @author author
 * @date 2026-01-01
 */
@Service
public class OptimizationPlanServiceImpl implements OptimizationPlanService {

    private final OptimizationPlanRepository optimizationPlanRepository;

    @Autowired
    public OptimizationPlanServiceImpl(OptimizationPlanRepository optimizationPlanRepository) {
        this.optimizationPlanRepository = optimizationPlanRepository;
    }

    @Override
    public List<OptimizationPlanEntity> getAllOptimizationPlans() {
        return optimizationPlanRepository.findAll();
    }

    @Override
    public Optional<OptimizationPlanEntity> getOptimizationPlanById(Long id) {
        return optimizationPlanRepository.findById(id);
    }

    @Override
    public OptimizationPlanEntity saveOptimizationPlan(OptimizationPlanEntity optimizationPlan) {
        return optimizationPlanRepository.save(optimizationPlan);
    }

    @Override
    public Optional<OptimizationPlanEntity> updateOptimizationPlan(Long id, OptimizationPlanEntity optimizationPlan) {
        return optimizationPlanRepository.findById(id)
                .map(existingPlan -> {
                    // 更新方案信息
                    existingPlan.setPlanName(optimizationPlan.getPlanName());
                    existingPlan.setTargetArea(optimizationPlan.getTargetArea());
                    existingPlan.setPredictedSaving(optimizationPlan.getPredictedSaving());
                    existingPlan.setActualSaving(optimizationPlan.getActualSaving());
                    existingPlan.setStatus(optimizationPlan.getStatus());
                    existingPlan.setExecContent(optimizationPlan.getExecContent());
                    existingPlan.setReportUrl(optimizationPlan.getReportUrl());
                    existingPlan.setStartDate(optimizationPlan.getStartDate());
                    existingPlan.setEndDate(optimizationPlan.getEndDate());
                    return optimizationPlanRepository.save(existingPlan);
                });
    }

    @Override
    public void deleteOptimizationPlan(Long id) {
        optimizationPlanRepository.deleteById(id);
    }

    @Override
    public List<OptimizationPlanEntity> getOptimizationPlansByStatus(Integer status) {
        return optimizationPlanRepository.findByStatus(status);
    }

    @Override
    public List<OptimizationPlanEntity> getOptimizationPlansByTargetArea(String targetArea) {
        return optimizationPlanRepository.findByTargetArea(targetArea);
    }

    @Override
    public Page<OptimizationPlanEntity> getOptimizationPlansPage(Integer status, String targetArea, Pageable pageable) {
        if (status != null && targetArea != null && !targetArea.isBlank()) {
            return optimizationPlanRepository.findByStatusAndTargetArea(status, targetArea, pageable);
        }
        if (status != null) {
            return optimizationPlanRepository.findByStatus(status, pageable);
        }
        if (targetArea != null && !targetArea.isBlank()) {
            return optimizationPlanRepository.findByTargetArea(targetArea, pageable);
        }
        return optimizationPlanRepository.findAll(pageable);
    }

    @Override
    public Optional<OptimizationPlanEntity> updateOptimizationPlanStatus(Long id, Integer status) {
        return optimizationPlanRepository.findById(id)
                .map(existingPlan -> {
                    existingPlan.setStatus(status);
                    return optimizationPlanRepository.save(existingPlan);
                });
    }
}
