package com.hxcoe.aps.entity;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "aps_schedule_task", indexes = {
    @Index(name = "idx_schedule_result_id", columnList = "schedule_result_id"),
    @Index(name = "idx_product_id", columnList = "product_id"),
    @Index(name = "idx_process_id", columnList = "process_id"),
    @Index(name = "idx_resource_id", columnList = "resource_id")
})
public class ScheduleTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "schedule_result_id", nullable = false)
    private Long scheduleResultId;
    
    @Column(name = "task_no", nullable = false, length = 50)
    private String taskNo;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;
    
    @Column(name = "process_id", nullable = false)
    private Long processId;
    
    @Column(name = "process_name", nullable = false, length = 100)
    private String processName;
    
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;
    
    @Column(name = "resource_name", nullable = false, length = 100)
    private String resourceName;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;
    
    @Column(name = "duration", nullable = false)
    private Integer duration;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "sequence", nullable = false)
    private Integer sequence;
    
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }
}
