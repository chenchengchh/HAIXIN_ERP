package com.hxcoe.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wms_integration_outbox",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wms_outbox_event_ref", columnNames = {"event_type", "ref_no"}),
                @UniqueConstraint(name = "uk_wms_outbox_event_id", columnNames = {"event_id"})
        })
public class IntegrationOutboxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "ref_no", nullable = false, length = 100)
    private String refNo;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "event_id", length = 64)
    private String eventId;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "producer", length = 64)
    private String producer;

    @Column(name = "event_version")
    private Integer eventVersion;

    @Column(name = "partition_key", length = 128)
    private String partitionKey;

    @Column(name = "idempotency_key", length = 200)
    private String idempotencyKey;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Column(name = "created_time", nullable = false)
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
