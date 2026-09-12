package com.hxcoe.scm.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scm_mrp_result")
public class MrpResultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long planId;
    
    // PURCHASE, PRODUCTION
    private String type;
    
    private String materialCode;
    private String materialName;
    
    private BigDecimal quantity;
    private LocalDate suggestDate;
    private LocalDate requiredDate;
    
    private String sourceId; // 来源需求ID
    
    private String status;

    private String externalRefType;

    private String externalRefNo;

    private String confirmedBy;

    private LocalDateTime confirmedTime;

    @Column(columnDefinition = "TEXT")
    private String rejectedReason;

    private LocalDateTime releasedTime;
}
