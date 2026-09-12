package com.hxcoe.eam.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "eam_maintenance_record")
public class MaintenanceRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(name = "equipment_name")
    private String equipmentName;

    private String type;
    private String content;
    
    @Column(name = "maintenance_time")
    private LocalDateTime maintenanceTime;
    
    private String maintainer;
    private BigDecimal cost;
    
    @Column(name = "workorder_id")
    private Long workorderId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
