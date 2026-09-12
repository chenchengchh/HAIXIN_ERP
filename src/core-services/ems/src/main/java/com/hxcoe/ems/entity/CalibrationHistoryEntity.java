package com.hxcoe.ems.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 校准历史实体类
 * 用于存储数据校准历史记录
 *
 * @author author
 * @date 2026-01-01
 */
@Data
@Entity
@Table(name = "ems_calibration_history")
public class CalibrationHistoryEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 设备ID
     */
    @Column(name = "meter_id", nullable = false)
    private Long meterId;

    /**
     * 设备名称
     */
    @Column(name = "meter_name", nullable = false, length = 100)
    private String meterName;

    /**
     * 原始值
     */
    @Column(name = "raw_value", nullable = false)
    private Double rawValue;

    /**
     * 校准后的值
     */
    @Column(name = "calibrated_value", nullable = false)
    private Double calibratedValue;

    /**
     * 校准系数
     */
    @Column(name = "coefficient", nullable = false)
    private Double coefficient;

    /**
     * 校准原因
     */
    @Column(name = "reason", length = 500)
    private String reason;

    /**
     * 校准时间
     */
    @Column(name = "calibration_time", nullable = false)
    private LocalDateTime calibrationTime;

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
        calibrationTime = LocalDateTime.now();
    }

    /**
     * 自动更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}