package com.hxcoe.aps.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aps_process", uniqueConstraints = @UniqueConstraint(name = "uk_aps_process_name_workshop", columnNames = {"process_name", "workshop"}))
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "process_name", nullable = false, length = 100)
    private String processName;
    
    @Column(name = "workshop", nullable = false, length = 50)
    private String workshop;
    
    @Column(name = "sequence", nullable = false)
    private Integer sequence;
    
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
}