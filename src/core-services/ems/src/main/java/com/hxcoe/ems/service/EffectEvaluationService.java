package com.hxcoe.ems.service;

import com.hxcoe.ems.entity.EffectEvaluationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 节能效果评估服务接口
 * 提供节能效果评估相关的业务逻辑
 *
 * @author author
 * @date 2026-01-01
 */
public interface EffectEvaluationService {

    /**
     * 获取所有节能效果评估记录
     *
     * @return 评估记录列表
     */
    List<EffectEvaluationEntity> getAllEffectEvaluations();

    /**
     * 根据ID获取节能效果评估记录
     *
     * @param id 评估ID
     * @return 评估记录
     */
    Optional<EffectEvaluationEntity> getEffectEvaluationById(Long id);

    /**
     * 保存节能效果评估记录
     *
     * @param effectEvaluation 评估记录
     * @return 保存后的评估记录
     */
    EffectEvaluationEntity saveEffectEvaluation(EffectEvaluationEntity effectEvaluation);

    /**
     * 更新节能效果评估记录
     *
     * @param id               评估ID
     * @param effectEvaluation 更新的评估信息
     * @return 更新后的评估记录
     */
    Optional<EffectEvaluationEntity> updateEffectEvaluation(Long id, EffectEvaluationEntity effectEvaluation);

    /**
     * 删除节能效果评估记录
     *
     * @param id 评估ID
     */
    void deleteEffectEvaluation(Long id);

    /**
     * 根据优化方案ID查询评估记录
     *
     * @param planId 优化方案ID
     * @return 评估记录列表
     */
    List<EffectEvaluationEntity> getEffectEvaluationsByPlanId(Long planId);

    /**
     * 根据目标区域查询评估记录
     *
     * @param targetArea 目标区域
     * @return 评估记录列表
     */
    List<EffectEvaluationEntity> getEffectEvaluationsByTargetArea(String targetArea);

    /**
     * 根据能源类型查询评估记录
     *
     * @param energyType 能源类型
     * @return 评估记录列表
     */
    List<EffectEvaluationEntity> getEffectEvaluationsByEnergyType(String energyType);

    /**
     * 根据评估周期查询评估记录
     *
     * @param evaluationPeriod 评估周期
     * @return 评估记录列表
     */
    List<EffectEvaluationEntity> getEffectEvaluationsByEvaluationPeriod(String evaluationPeriod);

    Page<EffectEvaluationEntity> getEffectEvaluationsPage(
            Long planId,
            String targetArea,
            String planName,
            String energyType,
            String evaluationPeriod,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    /**
     * 计算节能效果实现率
     *
     * @param actualSaving   实际节能量
     * @param predictedSaving 预计节能量
     * @return 实现率(%)
     */
    Double calculateAchievementRate(Double actualSaving, Double predictedSaving);
}
