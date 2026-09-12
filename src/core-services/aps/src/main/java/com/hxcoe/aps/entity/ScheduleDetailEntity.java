package com.hxcoe.aps.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aps_schedule_detail")
@Data
public class ScheduleDetailEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    /**
     * 所属排程结果ID：区分同一计划的多次排程，避免重排后详情混杂
     */
    @Column(name = "schedule_result_id")
    private Long scheduleResultId;

    @Column(name = "resource_id", nullable = false)
    private Long resourceId;

    @Column(name = "resource_name")
    private String resourceName;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "status", length = 20)
    private String status; // SCHEDULED, RELEASED

    @Column(name = "created_time")
    private LocalDateTime createdTime;
}
