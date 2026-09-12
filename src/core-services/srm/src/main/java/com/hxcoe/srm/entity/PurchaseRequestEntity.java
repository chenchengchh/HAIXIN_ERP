package com.hxcoe.srm.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "srm_purchase_request")
public class PurchaseRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_code", unique = true, nullable = false)
    private String requestCode;

    private String applicant;
    private String department;
    private LocalDateTime applyDate;
    private String status;
    private String purchaseType;
    private String description;
    private LocalDateTime expectedDeliveryDate;
    
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private String unit;
    
    /** 审批人 */
    private String approver;
    /** 审批意见 */
    @Column(name = "approval_opinion", length = 500)
    private String approvalOpinion;
    /** 审批时间 */
    private LocalDateTime approvalTime;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
