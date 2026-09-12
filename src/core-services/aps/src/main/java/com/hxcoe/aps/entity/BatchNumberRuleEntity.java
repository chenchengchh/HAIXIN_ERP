package com.hxcoe.aps.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "aps_batch_number_rule", uniqueConstraints = @UniqueConstraint(name = "uk_aps_batch_rule_prefix", columnNames = "prefix"))
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchNumberRuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "prefix", nullable = false, length = 20)
    private String prefix;
    
    @Column(name = "date_format", nullable = false, length = 20)
    private String dateFormat;
    
    @Column(name = "serial_length", nullable = false)
    private Integer serialLength;
    
    @Column(name = "sep", nullable = false, length = 5)
    private String separator;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
    
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