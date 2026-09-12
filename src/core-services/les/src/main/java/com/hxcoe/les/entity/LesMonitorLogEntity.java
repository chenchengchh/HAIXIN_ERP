package com.hxcoe.les.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "les_monitor_log")
@Data
public class LesMonitorLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "current_status", length = 50)
    private String currentStatus;

    @Column(name = "is_anomaly")
    private Boolean isAnomaly;

    @Column(name = "record_time")
    private LocalDateTime recordTime;
}
