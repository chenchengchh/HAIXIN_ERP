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

@Entity
@Table(name = "aps_resource_capability", indexes = {
    @Index(name = "idx_resource_id", columnList = "resource_id"),
    @Index(name = "idx_capability_type", columnList = "capability_type")
})
public class ResourceCapabilityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;
    
    @Column(name = "resource_name", nullable = false, length = 100)
    private String resourceName;
    
    @Column(name = "capability_type", nullable = false, length = 50)
    private String capabilityType;
    
    @Column(name = "capability_value", nullable = false)
    private Double capabilityValue;
    
    @Column(name = "unit", nullable = false, length = 20)
    private String unit;
    
    @Column(name = "efficiency", nullable = false)
    private Double efficiency;
    
    @Column(name = "operation_type", nullable = false, length = 255)
    private String operationType;
    
    @Column(name = "priority", nullable = false)
    private Integer priority;
    
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
    public ResourceCapabilityEntity() {
        // 默认构造方法
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }
    
    public String getResourceName() {
        return resourceName;
    }
    
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }
    
    public String getCapabilityType() {
        return capabilityType;
    }
    
    public void setCapabilityType(String capabilityType) {
        this.capabilityType = capabilityType;
    }
    
    public Double getCapabilityValue() {
        return capabilityValue;
    }
    
    public void setCapabilityValue(Double capabilityValue) {
        this.capabilityValue = capabilityValue;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public Double getEfficiency() {
        return efficiency;
    }
    
    public void setEfficiency(Double efficiency) {
        this.efficiency = efficiency;
    }
    
    public String getOperationType() {
        return operationType;
    }
    
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
    
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
    
    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }
    
    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }
}
