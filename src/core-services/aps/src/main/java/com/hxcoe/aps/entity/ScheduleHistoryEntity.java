package com.hxcoe.aps.entity;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "aps_schedule_history", indexes = {
    @Index(name = "idx_plan_id", columnList = "plan_id")
})
public class ScheduleHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "plan_id", nullable = false)
    private Long planId;
    
    @Column(name = "schedule_no", nullable = false, length = 50)
    private String scheduleNo;
    
    @Column(name = "algorithm", nullable = false, length = 50)
    private String algorithm;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status;
    
    @Column(name = "execution_time", nullable = false)
    private Double executionTime;
    
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    // 构造方法
    public ScheduleHistoryEntity() {
        // 默认构造方法
    }
}
