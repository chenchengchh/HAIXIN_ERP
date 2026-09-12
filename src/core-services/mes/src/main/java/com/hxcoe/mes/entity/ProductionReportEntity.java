package com.hxcoe.mes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mes_production_report")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_no", unique = true, nullable = false, length = 64)
    private String reportNo;

    @Column(name = "work_order_no", nullable = false, length = 64)
    private String workOrderNo;

    @Column(name = "step_name", length = 128)
    private String stepName;

    @Column(name = "workstation_name", length = 128)
    private String workstationName;

    @Column(name = "operator_name", length = 64)
    private String operatorName;

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
    private Double workingHours;

    @Column(name = "machine_hours")
    private Double machineHours;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "remark", length = 1000)
    private String remark;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
