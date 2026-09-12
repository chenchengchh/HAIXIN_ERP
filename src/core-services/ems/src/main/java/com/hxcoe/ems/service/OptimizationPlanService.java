package com.hxcoe.ems.service;

import com.hxcoe.ems.entity.OptimizationPlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 优化方案执行服务接口
 * 提供优化方案执行相关的业务逻辑
 *
 * @author author
 * @date 2026-01-01
 */
public interface OptimizationPlanService {

    /**
     * 获取所有优化方案
     *
     * @return 优化方案列表
     */
    List<OptimizationPlanEntity> getAllOptimizationPlans();

    /**
     * 根据ID获取优化方案
     *
     * @param id 方案ID
     * @return 优化方案
     */
    Optional<OptimizationPlanEntity> getOptimizationPlanById(Long id);

    /**
     * 保存优化方案
     *
     * @param optimizationPlan 优化方案
     * @return 保存后的优化方案
     */
    OptimizationPlanEntity saveOptimizationPlan(OptimizationPlanEntity optimizationPlan);

    /**
     * 更新优化方案
     *
     * @param id               方案ID
     * @param optimizationPlan 更新的方案信息
     * @return 更新后的优化方案
     */
    Optional<OptimizationPlanEntity> updateOptimizationPlan(Long id, OptimizationPlanEntity optimizationPlan);

    /**
     * 删除优化方案
     *
     * @param id 方案ID
     */
    void deleteOptimizationPlan(Long id);

    /**
     * 根据状态查询优化方案
     *
     * @param status 状态（1:建议中 2:执行中 3:回访验证 4:已结案）
     * @return 优化方案列表
     */
    List<OptimizationPlanEntity> getOptimizationPlansByStatus(Integer status);

    /**
     * 根据目标区域查询优化方案
     *
     * @param targetArea 目标区域
     * @return 优化方案列表
     */
    List<OptimizationPlanEntity> getOptimizationPlansByTargetArea(String targetArea);

    Page<OptimizationPlanEntity> getOptimizationPlansPage(Integer status, String targetArea, Pageable pageable);

    /**
     * 更新优化方案状态
     *
     * @param id     方案ID
     * @param status 新状态
     * @return 更新后的优化方案
     */
    Optional<OptimizationPlanEntity> updateOptimizationPlanStatus(Long id, Integer status);
}
