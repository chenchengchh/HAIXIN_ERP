package com.hxcoe.ems.repository;

import com.hxcoe.ems.entity.OptimizationSuggestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 优化建议Repository
 * 用于访问和操作优化建议表
 *
 * @author author
 * @date 2026-01-01
 */
@Repository
public interface OptimizationSuggestionRepository extends JpaRepository<OptimizationSuggestionEntity, Long> {

    /**
     * 根据状态查询优化建议
     *
     * @param status 状态
     * @return 优化建议列表
     */
    List<OptimizationSuggestionEntity> findByStatus(String status);
    Page<OptimizationSuggestionEntity> findByStatus(String status, Pageable pageable);

    /**
     * 根据目标区域查询优化建议
     *
     * @param targetArea 目标区域
     * @return 优化建议列表
     */
    List<OptimizationSuggestionEntity> findByTargetArea(String targetArea);
    Page<OptimizationSuggestionEntity> findByTargetArea(String targetArea, Pageable pageable);

    /**
     * 根据状态和目标区域查询优化建议
     *
     * @param status 状态
     * @param targetArea 目标区域
     * @return 优化建议列表
     */
    List<OptimizationSuggestionEntity> findByStatusAndTargetArea(String status, String targetArea);
    Page<OptimizationSuggestionEntity> findByStatusAndTargetArea(String status, String targetArea, Pageable pageable);
}
