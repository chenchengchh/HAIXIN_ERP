package com.hxcoe.qms.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "quality_inspection")
@Data
public class QualityInspectionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_code", nullable = false, unique = true, length = 50)
    private String inspectionCode;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "inspection_type", length = 20)
    private String inspectionType;

    @Column(name = "inspection_standard", length = 200)
    private String inspectionStandard;

    @Column(name = "inspection_result", length = 20)
    private String inspectionResult;

    /**
     * 来源类型 (PRODUCTION-生产, PURCHASE-采购)
     */
    @Column(name = "source_type")
    private String sourceType;

    /**
     * 来源单据号 (工单号/采购单号)
     */
    @Column(name = "source_no")
    private String sourceNo;

    /**
     * 送检数量
     */
    @Column(name = "quantity")
    private java.math.BigDecimal quantity;

    /**
     * 检验状态 (PENDING, COMPLETED)
     */
    @Column(name = "status")
    private String status;

    /**
     * 检验员
     */
    @Column(name = "inspector", length = 50)
    private String inspector;

    @Column(name = "inspection_date")
    private LocalDateTime inspectionDate;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}