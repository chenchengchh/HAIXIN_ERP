package com.hxcoe.plm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plm_quality_issue")
@Data
public class QualityIssueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "issue_code", length = 64, unique = true)
    private String issueCode;

    @Column(name = "issue_title", nullable = false, length = 256)
    private String issueTitle;

    @Column(name = "severity", length = 16)
    private String severity;

    @Column(name = "status", length = 32)
    private String status;

    @Lob
    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "create_user", length = 64)
    private String createUser;

    @Column(name = "create_time")
    private LocalDate createTime;

    @Column(name = "resolve_user", length = 64)
    private String resolveUser;

    @Column(name = "resolve_time")
    private LocalDate resolveTime;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
