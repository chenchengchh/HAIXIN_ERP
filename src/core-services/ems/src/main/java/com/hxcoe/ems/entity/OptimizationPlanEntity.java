package com.hxcoe.ems.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 优化方案执行实体类
 * 用于存储能源优化方案的执行数据
 *
 * @author author
 * @date 2026-01-01
 */
@Data
@Entity
@Table(name = "ems_optimization_plan")
public class OptimizationPlanEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
     * 状态（1:建议中 2:执行中 3:回访验证 4:已结案）
     */
    private Integer status;

    /**
     * 执行内容
     */
    private String execContent;

    /**
     * 报告URL
     */
    private String reportUrl;

    /**
     * 开始日期
     */
    private LocalDateTime startDate;

    /**
     * 结束日期
     */
    private LocalDateTime endDate;

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