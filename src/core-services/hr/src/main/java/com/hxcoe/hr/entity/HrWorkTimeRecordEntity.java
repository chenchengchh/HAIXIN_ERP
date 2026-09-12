package com.hxcoe.hr.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MES报工工时记录实体
 *
 * <p>接收MES推送的生产报工工时数据并落库，形成 MES→HR 报工工时闭环。
 * 按 eventId 唯一约束实现幂等防重。
 */
@Data
@Entity
@Table(name = "hr_work_time_record")
public class HrWorkTimeRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 员工编号
     */
    @Column(name = "employee_no", length = 64)
    private String employeeNo;

    /**
     * 员工姓名
     */
    @Column(name = "employee_name", length = 64)
    private String employeeName;

    /**
     * 工单号
     */
    @Column(name = "work_order_no", length = 64)
    private String workOrderNo;

    /**
     * 报工工时（小时）
     */
    @Column(name = "work_hours")
    private Double workHours;

    /**
     * 产出数量（合格品数量）
     */
    @Column(name = "output_quantity")
    private Integer outputQuantity;

    /**
     * 报工日期
     */
    @Column(name = "report_date")
    private LocalDate reportDate;

    /**
     * 工位名称
     */
    @Column(name = "workstation", length = 128)
    private String workstation;

    /**
     * 集成事件ID（唯一约束，幂等判重键）
     */
    @Column(name = "event_id", nullable = false, unique = true, length = 64)
    private String eventId;

    /**
     * 记录创建时间
     */
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
    }
}
