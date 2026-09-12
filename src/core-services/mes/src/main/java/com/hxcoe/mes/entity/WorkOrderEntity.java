package com.hxcoe.mes.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mes_work_order")
@Data
public class WorkOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_order_no", unique = true)
    private String workOrderNo;

    @Column(name = "source_id") // 对应 APS ScheduleDetail ID
    private String sourceId;

    /**
     * ERP 生产单号（跨服务业务键）。
     *
     * <p>用于 MES 工单与 ERP 生产单的稳定关联，避免使用易变的主键 id。
     * 由 ERP 创建生产单后通过 Outbox 事件传入。
     */
    @Column(name = "erp_production_no", length = 32)
    private String erpProductionNo;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "plan_quantity")
    private BigDecimal planQuantity;

    @Column(name = "actual_quantity")
    private BigDecimal actualQuantity;

    @Column(name = "resource_name") // 对应 APS Resource Name
    private String resourceName;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "status") // CREATED, STARTED, COMPLETED
    private String status;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
