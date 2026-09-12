package com.hxcoe.srm.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "srm_supplier_qualification")
public class SupplierQualificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long supplierId;
    
    // ISO9001, ISO14001, BUSINESS_LICENSE
    private String qualificationType;
    
    private String certificateName;
    private String certificateNo;
    
    private LocalDate issueDate;
    private LocalDate expiryDate;
    
    private String attachmentUrl;
    
    // PENDING, APPROVED, REJECTED, EXPIRED
    private String status;

    @Column(columnDefinition = "TEXT")
    private String auditOpinion;

    private LocalDateTime auditedTime;

    private LocalDateTime createdTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }
}
