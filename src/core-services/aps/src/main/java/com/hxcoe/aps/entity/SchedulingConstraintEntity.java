package com.hxcoe.aps.entity;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "aps_scheduling_constraint", indexes = {
    @Index(name = "idx_constraint_type", columnList = "constraint_type")
})
public class SchedulingConstraintEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "constraint_name", nullable = false, length = 100)
    private String constraintName;
    
    @Column(name = "constraint_type", nullable = false, length = 50)
    private String constraintType;
    
    @Column(name = "priority", nullable = false)
    private Integer priority;
    
    @Column(name = "value", nullable = false, length = 100)
    private String value;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time", nullable = false)
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
    
    // 构造方法
    public SchedulingConstraintEntity() {
        // 默认构造方法
    }
}
