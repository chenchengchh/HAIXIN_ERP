package com.hxcoe.ems.repository;

import com.hxcoe.ems.entity.OptimizationPlanEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 优化方案执行仓库接口
 * 用于优化方案执行数据的CRUD操作
 *
 * @author author
 * @date 2026-01-01
 */
@Repository
public interface OptimizationPlanRepository extends JpaRepository<OptimizationPlanEntity, Long> {

    /**
     * 根据状态查询优化方案
     *
     * @param status 状态（1:建议中 2:执行中 3:回访验证 4:已结案）
     * @return 优化方案列表
     */
    List<OptimizationPlanEntity> findByStatus(Integer status);
    Page<OptimizationPlanEntity> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据目标区域查询优化方案
     *
     * @param targetArea 目标区域
     * @return 优化方案列表
     */
    List<OptimizationPlanEntity> findByTargetArea(String targetArea);
    Page<OptimizationPlanEntity> findByTargetArea(String targetArea, Pageable pageable);

    Page<OptimizationPlanEntity> findByStatusAndTargetArea(Integer status, String targetArea, Pageable pageable);
}
