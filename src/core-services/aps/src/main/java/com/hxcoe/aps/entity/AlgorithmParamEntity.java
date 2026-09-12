package com.hxcoe.aps.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aps_algorithm_param", uniqueConstraints = @UniqueConstraint(name = "uk_aps_algo_param_name", columnNames = {"algorithm_name", "param_name"}))
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlgorithmParamEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "algorithm_name", nullable = false, length = 50)
    private String algorithmName;
    
    @Column(name = "param_name", nullable = false, length = 50)
    private String paramName;
    
    @Column(name = "param_value", nullable = false, length = 100)
    private String paramValue;
    
    @Column(name = "param_type", nullable = false, length = 20)
    private String paramType;
    
    @Column(name = "description", length = 200)
    private String description;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;
    
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