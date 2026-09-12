package com.hxcoe.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "wms_picking_task")
public class PickingTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_no", unique = true, nullable = false, length = 64)
    private String taskNo;

    private Long waveId;
    private String waveNo;

    @Column(name = "outbound_order_id")
    private Long outboundOrderId;

    @Column(name = "order_no", length = 64)
    private String orderNo;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "operator_id", length = 64)
    private String operatorId;

    @Column(name = "operator_name", length = 64)
    private String operatorName;

    private LocalDateTime assignedTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PickingTaskItemEntity> items;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (status == null || status.isBlank()) {
            status = "pending";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}

