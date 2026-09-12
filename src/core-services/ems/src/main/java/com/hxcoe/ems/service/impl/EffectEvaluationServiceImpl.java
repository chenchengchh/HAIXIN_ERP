package com.hxcoe.ems.service.impl;

import com.hxcoe.ems.entity.EffectEvaluationEntity;
import com.hxcoe.ems.repository.EffectEvaluationRepository;
import com.hxcoe.ems.service.EffectEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 节能效果评估服务实现类
 * 实现节能效果评估相关的业务逻辑
 *
 * @author author
 * @date 2026-01-01
 */
@Service
public class EffectEvaluationServiceImpl implements EffectEvaluationService {

    private final EffectEvaluationRepository effectEvaluationRepository;

    @Autowired
    public EffectEvaluationServiceImpl(EffectEvaluationRepository effectEvaluationRepository) {
        this.effectEvaluationRepository = effectEvaluationRepository;
    }

    @Override
    public List<EffectEvaluationEntity> getAllEffectEvaluations() {
        return effectEvaluationRepository.findAll();
    }

    @Override
    public Optional<EffectEvaluationEntity> getEffectEvaluationById(Long id) {
        return effectEvaluationRepository.findById(id);
    }

    @Override
    public EffectEvaluationEntity saveEffectEvaluation(EffectEvaluationEntity effectEvaluation) {
        // 计算实现率
        Double achievementRate = calculateAchievementRate(
                effectEvaluation.getActualSaving(),
                effectEvaluation.getPredictedSaving());
        effectEvaluation.setAchievementRate(achievementRate);
        
        return effectEvaluationRepository.save(effectEvaluation);
    }

    @Override
    public Optional<EffectEvaluationEntity> updateEffectEvaluation(Long id, EffectEvaluationEntity effectEvaluation) {
        return effectEvaluationRepository.findById(id)
                .map(existingEvaluation -> {
                    // 更新评估信息
                    existingEvaluation.setPlanName(effectEvaluation.getPlanName());
                    existingEvaluation.setTargetArea(effectEvaluation.getTargetArea());
                    existingEvaluation.setPredictedSaving(effectEvaluation.getPredictedSaving());
                    existingEvaluation.setActualSaving(effectEvaluation.getActualSaving());
                    existingEvaluation.setEvaluationPeriod(effectEvaluation.getEvaluationPeriod());
                    existingEvaluation.setEnergyType(effectEvaluation.getEnergyType());
                    
                    // 重新计算实现率
                    Double achievementRate = calculateAchievementRate(
                            effectEvaluation.getActualSaving(),
                            effectEvaluation.getPredictedSaving());
                    existingEvaluation.setAchievementRate(achievementRate);
                    
                    existingEvaluation.setEvaluationDate(effectEvaluation.getEvaluationDate());
                    existingEvaluation.setEvaluationContent(effectEvaluation.getEvaluationContent());
                    
                    return effectEvaluationRepository.save(existingEvaluation);
                });
    }

    @Override
    public void deleteEffectEvaluation(Long id) {
        effectEvaluationRepository.deleteById(id);
    }

    @Override
    public List<EffectEvaluationEntity> getEffectEvaluationsByPlanId(Long planId) {
        return effectEvaluationRepository.findByPlanId(planId);
    }

    @Override
    public List<EffectEvaluationEntity> getEffectEvaluationsByTargetArea(String targetArea) {
        return effectEvaluationRepository.findByTargetArea(targetArea);
    }

    @Override
    public List<EffectEvaluationEntity> getEffectEvaluationsByEnergyType(String energyType) {
        return effectEvaluationRepository.findByEnergyType(energyType);
    }

    @Override
    public List<EffectEvaluationEntity> getEffectEvaluationsByEvaluationPeriod(String evaluationPeriod) {
        return effectEvaluationRepository.findByEvaluationPeriod(evaluationPeriod);
    }

    @Override
    public Page<EffectEvaluationEntity> getEffectEvaluationsPage(
            Long planId,
            String targetArea,
            String planName,
            String energyType,
            String evaluationPeriod,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    ) {
        if (planId != null) {
            return effectEvaluationRepository.findByPlanId(planId, pageable);
        }
        if (energyType != null && !energyType.isBlank()) {
            return effectEvaluationRepository.findByEnergyType(energyType, pageable);
        }
        if (evaluationPeriod != null && !evaluationPeriod.isBlank()) {
            return effectEvaluationRepository.findByEvaluationPeriod(evaluationPeriod, pageable);
        }

        String a = targetArea == null || targetArea.isBlank() ? null : targetArea;
        String n = planName == null || planName.isBlank() ? null : planName;
        boolean hasRange = start != null && end != null;

        if (hasRange) {
            if (a != null && n != null) {
                return effectEvaluationRepository.findByTargetAreaAndPlanNameContainingAndEvaluationDateBetween(a, n, start, end, pageable);
            }
            if (a != null) {
                return effectEvaluationRepository.findByTargetAreaAndEvaluationDateBetween(a, start, end, pageable);
            }
            if (n != null) {
                return effectEvaluationRepository.findByPlanNameContainingAndEvaluationDateBetween(n, start, end, pageable);
            }
            return effectEvaluationRepository.findByEvaluationDateBetween(start, end, pageable);
        }

        if (a != null && n != null) {
            return effectEvaluationRepository.findByTargetAreaAndPlanNameContaining(a, n, pageable);
        }
        if (a != null) {
            return effectEvaluationRepository.findByTargetArea(a, pageable);
        }
        if (n != null) {
            return effectEvaluationRepository.findByPlanNameContaining(n, pageable);
        }
        return effectEvaluationRepository.findAll(pageable);
    }

    @Override
    public Double calculateAchievementRate(Double actualSaving, Double predictedSaving) {
        // 计算实现率：(实际节能量 / 预计节能量) * 100%
        // 防止除以0的情况
        if (predictedSaving == null || predictedSaving == 0) {
            return 0.0;
        }
        if (actualSaving == null) {
            return 0.0;
        }
        
        // 计算实现率，保留两位小数
        Double rate = (actualSaving / predictedSaving) * 100;
        return Math.round(rate * 100.0) / 100.0;
    }
}
