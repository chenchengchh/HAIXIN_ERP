package com.hxcoe.mes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mes_manual_reporting")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManualReportingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id", length = 64)
    private String operatorId;

    @Column(name = "operator_name", length = 64)
    private String operatorName;

    @Column(name = "work_order_no", length = 64)
    private String workOrderNo;

    @Column(name = "step_id", length = 64)
    private String stepId;

    @Column(name = "step_name", length = 128)
    private String stepName;

    @Column(name = "workstation_id", length = 64)
    private String workstationId;

    @Column(name = "workstation_name", length = 128)
    private String workstationName;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "good_qty")
    private Integer goodQty;

    @Column(name = "scrap_qty")
    private Integer scrapQty;

    @Column(name = "rework_qty")
    private Integer reworkQty;

    @Column(name = "working_hours")
    private Integer workingHours;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
