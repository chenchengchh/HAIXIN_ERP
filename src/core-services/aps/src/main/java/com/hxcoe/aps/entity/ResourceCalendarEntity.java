package com.hxcoe.aps.entity;
import java.time.LocalDate;
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
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(name = "aps_resource_calendar", 
       uniqueConstraints = {@UniqueConstraint(columnNames = {"resource_id", "date", "shift"})},
       indexes = {@Index(name = "idx_resource_date", columnList = "resource_id, date")})
public class ResourceCalendarEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;
    
    @Column(name = "resource_name", nullable = false, length = 100)
    private String resourceName;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "shift", nullable = false, length = 20)
    private String shift;
    
    @Column(name = "available", nullable = false)
    private Boolean available;
    
    @Column(name = "available_hours", nullable = false)
    private Double availableHours;
    
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
    public ResourceCalendarEntity() {
        // 默认构造方法
    }
}
