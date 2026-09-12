package com.hxcoe.ems.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 能耗异常实体类
 * 用于存储能耗异常检测数据
 *
 * @author author
 * @date 2026-01-01
 */
@Data
@Entity
@Table(name = "ems_energy_anomaly")
public class EnergyAnomalyEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 能源类型
     */
    @Column(name = "energy_type", nullable = false, length = 50)
    private String energyType;

    /**
     * 异常区域
     */
    @Column(name = "area", nullable = false, length = 100)
    private String area;

    /**
     * 异常类型
     */
    @Column(name = "anomaly_type", nullable = false, length = 100)
    private String anomalyType;

    /**
     * 实际值
     */
    @Column(name = "actual_value", nullable = false)
    private Double actualValue;

    /**
     * 预期值
     */
    @Column(name = "expected_value", nullable = false)
    private Double expectedValue;

    /**
     * 检测时间
     */
    @Column(name = "detection_time", nullable = false)
    private LocalDateTime detectionTime;

    /**
     * 状态（pending: 待处理, processed: 已处理）
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * 处理时间
     */
    @Column(name = "processed_time")
    private LocalDateTime processedTime;

    /**
     * 处理人
     */
    @Column(name = "processed_by", length = 50)
    private String processedBy;

    /**
     * 处理备注
     */
    @Column(name = "processed_remark", length = 500)
    private String processedRemark;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
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
