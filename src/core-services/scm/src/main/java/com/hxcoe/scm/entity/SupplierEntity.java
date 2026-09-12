package com.hxcoe.scm.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "scm_supplier")
@Data
public class SupplierEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "supplier_code", unique = true, nullable = false, length = 50)
    private String supplierCode;
    
    @Column(name = "supplier_name", nullable = false, length = 100)
    private String supplierName;
    
    @Column(name = "supplier_type", length = 20)
    private String supplierType;
    
    @Column(name = "supplier_level", length = 20)
    private String supplierLevel;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "contact_person", length = 50)
    private String contactPerson;
    
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
    
    @Column(name = "contact_email", length = 100)
    private String contactEmail;
    
    @Column(name = "address", length = 200)
    private String address;
    
    @Column(name = "business_license", length = 64)
    private String businessLicense;
    
    @Column(name = "credit_level", length = 20)
    private String creditLevel;
    
    @Column(name = "created_by", length = 50)
    private String createdBy;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_by", length = 50)
    private String updatedBy;
    
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;
    
    @Column(name = "remark", length = 500)
    private String remark;
}