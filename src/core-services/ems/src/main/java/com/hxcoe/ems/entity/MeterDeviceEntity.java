package com.hxcoe.ems.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 采集设备实体类
 * 用于存储采集设备信息
 *
 * @author author
 * @date 2026-01-01
 */
@Data
@Entity
@Table(name = "ems_meter_device")
public class MeterDeviceEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 设备名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 设备类型
     */
    @Column(name = "type", nullable = false, length = 50)
    private String type;

    /**
     * IP地址
     */
    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    /**
     * 状态（online: 在线, offline: 离线）
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * 最后更新时间
     */
    @Column(name = "last_update", nullable = false)
    private LocalDateTime lastUpdate;

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
        lastUpdate = LocalDateTime.now();
    }

    /**
     * 自动更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        lastUpdate = LocalDateTime.now();
    }
}