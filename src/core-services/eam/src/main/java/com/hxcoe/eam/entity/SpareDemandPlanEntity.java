package com.hxcoe.eam.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "eam_spare_demand_plan")
public class SpareDemandPlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "spare_id")
    private Long spareId;
    
    @Column(name = "spare_name")
    private String spareName;
    
    @Column(name = "spare_code")
    private String spareCode;
    
    @Transient
    private Integer currentStock;
    
    @Transient
    private String maintenancePlanName;
    
    @Transient
    private Integer gap;
    
    @Column(name = "required_qty")
    private Integer requiredQty;
    
    @Column(name = "suggested_date")
    private LocalDate suggestedDate;
    
    @Column(name = "maintenance_plan_id")
    private Long maintenancePlanId;
    
    private String status; // pending, ordered, fulfilled

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
