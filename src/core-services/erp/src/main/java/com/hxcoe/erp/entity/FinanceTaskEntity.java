package com.hxcoe.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "erp_finance_task",
        uniqueConstraints = @UniqueConstraint(name = "uk_erp_finance_task_idempotency_key", columnNames = {"idempotency_key"}))
public class FinanceTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fact_type", nullable = false, length = 32)
    private String factType;

    @Column(name = "source_type", nullable = false, length = 16)
    private String sourceType;

    @Column(name = "source_no", nullable = false, length = 64)
    private String sourceNo;

    @Column(name = "ref_no", length = 64)
    private String refNo;

    @Column(name = "result", length = 32)
    private String result;

    @Column(name = "payload_json", columnDefinition = "json")
    private String payloadJson;

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

    @Column(name = "idempotency_key", nullable = false, length = 200)
    private String idempotencyKey;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "voucher_id")
    private Long voucherId;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "processed_time")
    private LocalDateTime processedTime;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (retryCount == null) {
            retryCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
