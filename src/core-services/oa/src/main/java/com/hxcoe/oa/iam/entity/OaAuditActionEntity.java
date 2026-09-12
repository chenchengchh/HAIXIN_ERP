package com.hxcoe.oa.iam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "oa_audit_action")
public class OaAuditActionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "username", length = 64)
    private String username;

    @Column(name = "trace_id", length = 64)
    private String traceId;

    @Column(name = "http_method", length = 16)
    private String httpMethod;

    @Column(name = "path", length = 512)
    private String path;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "result", length = 16)
    private String result;

    @Column(name = "error_message", length = 512)
    private String errorMessage;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }
}

