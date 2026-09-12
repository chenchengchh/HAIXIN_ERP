package com.hxcoe.les.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * LES 集成 Outbox 实体（表 les_integration_outbox）。
 * <p>用于 LES 签收完成事件向 ERP/CRM 异步可靠投递，通过 eventType 区分下游。</p>
 */
@Data
@Entity
@Table(name = "les_integration_outbox",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_les_outbox_event_ref", columnNames = {"event_type", "ref_no"}),
                @UniqueConstraint(name = "uk_les_outbox_event_id", columnNames = {"event_id"})
        })
public class IntegrationOutboxEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 事件类型（LES_SIGN_COMPLETED_ERP / LES_SIGN_COMPLETED_CRM） */
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    /** 业务引用号（签收凭证 ID 字符串） */
    @Column(name = "ref_no", nullable = false, length = 100)
    private String refNo;

    /** 关联实体 ID（签收凭证 ID） */
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

    /** 投递状态（PENDING/SENT/FAILED） */
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
