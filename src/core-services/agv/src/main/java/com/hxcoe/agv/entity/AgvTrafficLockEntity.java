package com.hxcoe.agv.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "agv_traffic_lock")
public class AgvTrafficLockEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "node_code", nullable = false, length = 128)
    private String nodeCode;

    @Column(name = "agv_code", length = 64)
    private String agvCode;

    @Column(name = "lock_status", length = 32)
    private String lockStatus;

    @Column(name = "lock_time")
    private LocalDateTime lockTime;

    @Column(name = "release_time")
    private LocalDateTime releaseTime;

    @Column(name = "remark", length = 512)
    private String remark;

    @PrePersist
    protected void onCreate() {
        if (lockTime == null) lockTime = LocalDateTime.now();
        if (lockStatus == null || lockStatus.isBlank()) lockStatus = "locked";
        if (agvCode == null) agvCode = "";
        if (remark == null) remark = "";
    }
}

