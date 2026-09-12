package com.hxcoe.srm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "srm_integration_inbox",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_srm_inbox_event_key", columnNames = {"event_key"}),
                @UniqueConstraint(name = "uk_srm_inbox_event_id", columnNames = {"event_id"})
        })
public class IntegrationInboxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_key", nullable = false, length = 200)
    private String eventKey;

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

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "received_time", nullable = false)
    private LocalDateTime receivedTime;

    @PrePersist
    protected void onCreate() {
        receivedTime = LocalDateTime.now();
    }
}
