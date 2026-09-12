package com.hxcoe.eam.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "eam_workorder")
public class WorkOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // preventive, repair, emergency
    
    @Column(name = "equipment_id")
    private Long equipmentId;
    
    @Column(name = "equipment_name")
    private String equipmentName;
    
    private String priority; // high, medium, low
    private String assignee;
    private String description;
    private String status; // pending, processing, completed, cancelled
    
    @Column(name = "plan_id")
    private Long planId;

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
