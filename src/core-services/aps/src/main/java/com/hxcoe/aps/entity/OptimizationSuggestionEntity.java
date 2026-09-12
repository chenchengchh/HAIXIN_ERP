package com.hxcoe.aps.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aps_optimization_suggestion")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptimizationSuggestionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "schedule_result_id", nullable = false)
    private Long scheduleResultId;
    
    @Column(name = "type", nullable = false, length = 50)
    private String type;
    
    @Column(name = "description", nullable = false, length = 500)
    private String description;
    
    @Column(name = "suggestion", nullable = false, length = 500)
    private String suggestion;
    
    @Column(name = "priority", nullable = false, length = 20)
    private String priority;
    
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
}