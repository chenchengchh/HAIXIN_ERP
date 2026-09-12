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
@Table(name = "aps_process_operation", indexes = {
    @Index(name = "idx_route_id", columnList = "route_id"),
    @Index(name = "idx_process_id", columnList = "process_id")
})
public class ProcessOperationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "route_id", nullable = false)
    private Long routeId;
    
    @Column(name = "process_id", nullable = false)
    private Long processId;
    
    @Column(name = "process_name", nullable = false, length = 100)
    private String processName;
    
    @Column(name = "sequence", nullable = false)
    private Integer sequence;
    
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;
    
    @Column(name = "resource_name", nullable = false, length = 100)
    private String resourceName;
    
    @Column(name = "processing_time", nullable = false)
    private Integer processingTime;
    
    @Column(name = "setup_time", nullable = false)
    private Integer setupTime;
    
    @Column(name = "teardown_time", nullable = false)
    private Integer teardownTime;
    
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
    public ProcessOperationEntity() {
        // 默认构造方法
    }
}
