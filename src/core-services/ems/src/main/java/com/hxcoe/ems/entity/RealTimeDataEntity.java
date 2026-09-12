package com.hxcoe.ems.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实时数据采集实体类
 * 用于存储能源实时采集数据
 *
 * @author author
 * @date 2026-01-01
 */
@Data
@Entity
@Table(name = "ems_real_time_data")
public class RealTimeDataEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 能源类型（电力、水、燃气、热能）
     */
    @Column(name = "energy_type", nullable = false, length = 50)
    private String energyType;

    /**
     * 采集区域
     */
    @Column(name = "area", nullable = false, length = 100)
    private String area;

    /**
     * 采集值
     */
    @Column(name = "actual_value", nullable = false)
    private Double actualValue;

    /**
     * 单位
     */
    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    /**
     * 采集时间
     */
    @Column(name = "collection_time", nullable = false)
    private LocalDateTime collectionTime;

    /**
     * 状态（normal: 正常, abnormal: 异常）
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * 设备ID
     */
    @Column(name = "device_id")
    private Long deviceId;

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
