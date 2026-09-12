package com.hxcoe.scm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SCM 接收 MES 完工事实实体（B6 闭环）。
 *
 * <p>对应表 scm_production_completion_fact（由 SQL 脚本 P0-02 创建）。
 * MES 工单完工后通过 Outbox 推送，SCM 落库后形成制造回流，闭环生产链。
 */
@Data
@Entity
@Table(name = "scm_production_completion_fact")
public class ScmProductionCompletionFactEntity {

    /** 主键ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 事件ID（幂等用） */
    @Column(name = "event_id", length = 64)
    private String eventId;

    /** 事件键 */
    @Column(name = "event_key", length = 128)
    private String eventKey;

    /** 幂等键 */
    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    /** ERP 生产单号（业务键） */
    @Column(name = "erp_production_no", nullable = false, length = 32)
    private String erpProductionNo;

    /** MES 工单号 */
    @Column(name = "work_order_no", nullable = false, length = 64)
    private String workOrderNo;

    /** 产品编码 */
    @Column(name = "product_code", length = 32)
    private String productCode;

    /** 产品名称 */
    @Column(name = "product_name", length = 128)
    private String productName;

    /** 计划数量 */
    @Column(name = "plan_quantity", precision = 18, scale = 4)
    private BigDecimal planQuantity;

    /** 实际完工数量 */
    @Column(name = "completed_quantity", precision = 18, scale = 4)
    private BigDecimal completedQuantity;

    /** 完工时间 */
    @Column(name = "completed_time")
    private LocalDateTime completedTime;

    /** SCM 侧订单状态投影 */
    @Column(name = "scm_order_status", length = 32)
    private String scmOrderStatus;

    /** 来源系统 */
    @Column(name = "source_system", length = 32)
    private String sourceSystem;

    /** 创建时间 */
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    /** 更新时间 */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 新增前设置时间戳。
     */
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (sourceSystem == null) {
            sourceSystem = "mes-service";
        }
    }

    /**
     * 更新前刷新时间戳。
     */
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
