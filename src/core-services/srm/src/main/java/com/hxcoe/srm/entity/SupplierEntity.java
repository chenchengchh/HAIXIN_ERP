package com.hxcoe.srm.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "srm_supplier")
public class SupplierEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_code", unique = true, nullable = false)
    private String supplierCode;

    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    private String contactPerson;
    private String contactPhone;
    private String email;
    private String address;
    
    // A, B, C, D
    private String category;
    
    // ACTIVE, BLACKLIST, POTENTIAL
    private String status;
    
    // POTENTIAL, QUALIFIED, BLACKLISTED
    private String type;
    
    private Double rating;

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
