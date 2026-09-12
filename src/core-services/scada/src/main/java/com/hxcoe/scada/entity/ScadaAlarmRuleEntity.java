package com.hxcoe.scada.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "scada_alarm_rule")
@Data
public class ScadaAlarmRuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_code", nullable = false, length = 50)
    private String tagCode;

    @Column(name = "alarm_name", length = 100)
    private String alarmName;

    @Column(name = "severity")
    private Integer severity;

    @Column(name = "enabled")
    private Boolean enabled;

    @Column(name = "high_high")
    private Double highHigh;

    @Column(name = "high")
    private Double high;

    @Column(name = "low")
    private Double low;

    @Column(name = "low_low")
    private Double lowLow;

    @Column(name = "deadband")
    private Double deadband;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}

