package com.hxcoe.scada.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "scada_alarm_active")
@Data
public class ScadaActiveAlarmEntity {
    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "tag_code", length = 50)
    private String tagCode;

    @Column(name = "alarm_name", length = 100)
    private String alarmName;

    @Column(name = "alarm_type", length = 50)
    private String alarmType;

    @Column(name = "severity")
    private Integer severity;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "trigger_time")
    private LocalDateTime triggerTime;

    @Column(name = "current_value")
    private Double currentValue;

    @Column(name = "threshold_value")
    private Double threshold;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "muted")
    private Boolean muted;
}

