package com.hxcoe.scada.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "scada_alarm_history")
@Data
public class ScadaAlarmHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_code", length = 50)
    private String tagCode;

    @Column(name = "alarm_type", length = 50)
    private String alarmType;

    @Column(name = "severity")
    private Integer severity;

    @Column(name = "trigger_time")
    private LocalDateTime triggerTime;

    @Column(name = "confirm_time")
    private LocalDateTime confirmTime;

    @Column(name = "recovery_time")
    private LocalDateTime recoveryTime;

    @Column(name = "handler_id")
    private Long handlerId;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;
}

