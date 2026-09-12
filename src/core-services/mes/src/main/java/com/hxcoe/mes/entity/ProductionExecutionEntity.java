package com.hxcoe.mes.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 生产执行实体类
 */
@Entity
@Table(name = "mes_production_execution")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionExecutionEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 生产执行单号
     */
    @Column(name = "execution_no", unique = true, nullable = false, length = 32)
    private String executionNo;

    /**
     * 生产订单号
     */
    @Column(name = "production_order_no", nullable = false, length = 32)
    private String productionOrderNo;

    /**
     * ERP 生产单号（跨服务业务键）。
     *
     * <p>用于生产执行记录与 ERP 生产单的稳定关联，与 mes_work_order.erp_production_no 保持一致。
     * 由 ERP 创建生产单后通过 Outbox 事件传入，供前端 F4（MES工单ERP生产单号列）展示。
     */
    @Column(name = "erp_production_no", length = 32)
    private String erpProductionNo;

    /**
     * 产品编码
     */
    @Column(name = "product_code", nullable = false, length = 32)
    private String productCode;

    /**
     * 产品名称
     */
    @Column(name = "product_name", nullable = false, length = 128)
    private String productName;

    /**
     * 计划生产数量
     */
    @Column(name = "plan_quantity", nullable = false)
    private Integer planQuantity;

    /**
     * 实际生产数量
     */
    @Column(name = "actual_quantity", nullable = false)
    private Integer actualQuantity;

    /**
     * 合格数量
     */
    @Column(name = "qualified_quantity", nullable = false)
    private Integer qualifiedQuantity;

    /**
     * 不合格数量
     */
    @Column(name = "unqualified_quantity", nullable = false)
    private Integer unqualifiedQuantity;

    /**
     * 生产状态：1-待执行，2-执行中，3-已完成，4-已暂停，5-已取消
     */
    @Column(name = "execution_status", nullable = false)
    private Integer executionStatus;

    /**
     * 生产车间
     */
    @Column(name = "workshop", length = 32)
    private String workshop;

    /**
     * 生产线
     */
    @Column(name = "production_line", length = 32)
    private String productionLine;

    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    /**
     * 更新时间
     */
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    /**
     * 逻辑删除标识：0-未删除，1-已删除
     */
    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted;
}
