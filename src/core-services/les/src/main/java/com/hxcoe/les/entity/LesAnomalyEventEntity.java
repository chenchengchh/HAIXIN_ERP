package com.hxcoe.les.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "les_anomaly_event")
@Data
public class LesAnomalyEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "event_no", length = 50)
    private String eventNo;

    @Column(name = "event_type", length = 50)
    private String eventType;

    @Column(name = "event_desc", length = 500)
    private String eventDesc;

    @Column(name = "event_description", length = 500)
    private String eventDescription;

    @Column(name = "event_time")
    private LocalDateTime eventTime;

    @Column(name = "handling_status", length = 20)
    private String handlingStatus;

    @Column(name = "handling_result", length = 500)
    private String handlingResult;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}

