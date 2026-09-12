package com.hxcoe.eam.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "eam_spare_inventory")
public class SpareInventoryEntity {
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
    private Integer safetyStock;
    
    private String location;
    private Integer quantity;
    private String status; // normal, low, excess

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
