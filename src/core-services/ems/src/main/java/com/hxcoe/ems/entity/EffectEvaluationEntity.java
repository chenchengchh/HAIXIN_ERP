package com.hxcoe.ems.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 节能效果评估实体类
 * 用于存储能源优化方案的效果评估数据
 *
 * @author author
 * @date 2026-01-01
 */
@Data
@Entity
@Table(name = "ems_effect_evaluation")
public class EffectEvaluationEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 关联的优化方案ID
     */
    private Long planId;

    /**
     * 方案名称
     */
    private String planName;

    /**
     * 目标区域
     */
    private String targetArea;

    /**
     * 预计节能量(%)
     */
    private Double predictedSaving;

    /**
     * 实际节能量(%)
     */
    private Double actualSaving;

    /**
     * 评估周期
     */
    private String evaluationPeriod;

    /**
     * 能源类型
     */
    private String energyType;

    /**
     * 实现率(%)
     */
    private Double achievementRate;

    /**
     * 评估日期
     */
    private LocalDateTime evaluationDate;

    /**
     * 评估内容
     */
    private String evaluationContent;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 自动设置创建和更新时间
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 自动更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}