package com.hxcoe.srm.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "srm_contract")
public class ContractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contract_no", unique = true, nullable = false)
    private String contractNo;

    private String title;
    
    private Long supplierId;
    private String supplierName;
    
    private BigDecimal totalAmount;
    
    // DRAFT, ACTIVE, EXPIRED, TERMINATED
    private String status;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    private String signedBy;
    private LocalDateTime signedDate;

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
