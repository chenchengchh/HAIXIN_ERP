package com.hxcoe.eam.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "eam_spare_issue")
public class SpareIssueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spare_id")
    private Long spareId;
    
    @Column(name = "spare_name")
    private String spareName;
    
    @Column(name = "spare_code")
    private String spareCode;
    
    private Integer quantity;
    private String applicant;
    
    @Column(name = "application_date")
    private LocalDateTime applicationDate;
    
    private String status; // pending, approved, rejected
    
    @Column(name = "return_status")
    private String returnStatus; // not_returned, returned, consumed
    
    private String remark;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
