package com.hxcoe.wms.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wms_wave")
public class WaveEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wave_no", unique = true, nullable = false)
    private String waveNo;

    // CREATED, RELEASED, PICKING, COMPLETED
    private String status;

    @Column(length = 512)
    private String remark;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "assigned_to", length = 64)
    private String assignedTo;

    private LocalDateTime assignedTime;
    private LocalDateTime releasedTime;
    private LocalDateTime completedTime;

    @Column(precision = 19, scale = 4)
    private BigDecimal totalQuantity;

    private Integer orderCount;

    @Column(name = "order_type", length = 32)
    private String orderType;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (status == null || status.isBlank()) {
            status = "CREATED";
        }
        if (totalQuantity == null) {
            totalQuantity = BigDecimal.ZERO;
        }
        if (orderCount == null) {
            orderCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
