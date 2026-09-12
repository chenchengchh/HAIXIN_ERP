package com.hxcoe.agv.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "agv_device")
@Data
public class AgvDeviceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agv_code", nullable = false, unique = true, length = 64)
    private String code;

    @Column(name = "agv_name", length = 128)
    private String name;

    @Column(name = "agv_type", length = 64)
    private String type;

    @Column(name = "model", length = 64)
    private String model;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "voltage", precision = 10, scale = 2)
    private BigDecimal voltage;

    @Column(name = "temperature", precision = 10, scale = 2)
    private BigDecimal temperature;

    @Column(name = "speed", precision = 10, scale = 2)
    private BigDecimal speed;

    @Column(name = "direction", length = 32)
    private String direction;

    @Column(name = "position", length = 128)
    private String position;

    @Column(name = "current_task_id", length = 64)
    private String currentTaskId;

    @Column(name = "load_status", length = 32)
    private String loadStatus;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @Column(name = "remark", length = 512)
    private String remark;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdTime == null) createdTime = now;
        updatedTime = now;
        if (lastUpdate == null) lastUpdate = now;
        if (status == null || status.isBlank()) status = "idle";
        if (batteryLevel == null) batteryLevel = 100;
        if (voltage == null) voltage = BigDecimal.ZERO;
        if (temperature == null) temperature = BigDecimal.ZERO;
        if (speed == null) speed = BigDecimal.ZERO;
        if (direction == null) direction = "";
        if (position == null) position = "";
        if (currentTaskId == null) currentTaskId = "";
        if (loadStatus == null || loadStatus.isBlank()) loadStatus = "empty";
        if (remark == null) remark = "";
    }

    @PreUpdate
    protected void onUpdate() {
        LocalDateTime now = LocalDateTime.now();
        updatedTime = now;
        lastUpdate = now;
    }
}
