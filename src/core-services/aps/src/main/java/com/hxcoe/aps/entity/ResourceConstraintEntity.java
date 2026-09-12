package com.hxcoe.aps.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aps_resource_constraint", uniqueConstraints = @UniqueConstraint(name = "uk_aps_res_constraint_rid", columnNames = "resource_id"))
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceConstraintEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "resource_id", nullable = false)
    private Long resourceId;
    
    @Column(name = "resource_name", nullable = false, length = 100)
    private String resourceName;
    
    @Column(name = "constraint_type", nullable = false, length = 20)
    private String constraintType;
    
    @Column(name = "capacity", nullable = false)
    private Integer capacity;
    
    @Column(name = "available_capacity", nullable = false)
    private Integer availableCapacity;
    
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
