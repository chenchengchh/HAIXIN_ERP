package com.hxcoe.ems.repository;

import com.hxcoe.ems.entity.EffectEvaluationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 节能效果评估仓库接口
 * 用于节能效果评估数据的CRUD操作
 *
 * @author author
 * @date 2026-01-01
 */
@Repository
public interface EffectEvaluationRepository extends JpaRepository<EffectEvaluationEntity, Long> {

    /**
     * 根据优化方案ID查询评估记录
     *
     * @param planId 优化方案ID
     * @return 评估记录列表
     */
    List<EffectEvaluationEntity> findByPlanId(Long planId);
    Page<EffectEvaluationEntity> findByPlanId(Long planId, Pageable pageable);

    /**
     * 根据目标区域查询评估记录
     *
     * @param targetArea 目标区域
     * @return 评估记录列表
     */
    List<EffectEvaluationEntity> findByTargetArea(String targetArea);
    Page<EffectEvaluationEntity> findByTargetArea(String targetArea, Pageable pageable);

    Page<EffectEvaluationEntity> findByTargetAreaAndPlanNameContaining(String targetArea, String planName, Pageable pageable);

    /**
     * 根据能源类型查询评估记录
     *
     * @param energyType 能源类型
     * @return 评估记录列表
     */
    List<EffectEvaluationEntity> findByEnergyType(String energyType);
    Page<EffectEvaluationEntity> findByEnergyType(String energyType, Pageable pageable);

    /**
     * 根据评估周期查询评估记录
     *
     * @param evaluationPeriod 评估周期
     * @return 评估记录列表
     */
    List<EffectEvaluationEntity> findByEvaluationPeriod(String evaluationPeriod);
    Page<EffectEvaluationEntity> findByEvaluationPeriod(String evaluationPeriod, Pageable pageable);

    Page<EffectEvaluationEntity> findByPlanNameContaining(String planName, Pageable pageable);

    Page<EffectEvaluationEntity> findByEvaluationDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<EffectEvaluationEntity> findByTargetAreaAndEvaluationDateBetween(String targetArea, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<EffectEvaluationEntity> findByPlanNameContainingAndEvaluationDateBetween(String planName, LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<EffectEvaluationEntity> findByTargetAreaAndPlanNameContainingAndEvaluationDateBetween(String targetArea, String planName, LocalDateTime start, LocalDateTime end, Pageable pageable);
}
