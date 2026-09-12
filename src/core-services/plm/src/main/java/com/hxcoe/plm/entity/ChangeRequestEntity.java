package com.hxcoe.plm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "plm_change_request")
@Data
public class ChangeRequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "change_code", length = 64, unique = true)
    private String changeCode;

    @Column(name = "title", nullable = false, length = 256)
    private String title;

    @Column(name = "change_type", length = 64)
    private String changeType;

    @Column(name = "process_code", length = 64)
    private String processCode;

    @Column(name = "process_name", length = 128)
    private String processName;

    @Lob
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Lob
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "apply_user", length = 64)
    private String applyUser;

    @Column(name = "apply_time")
    private LocalDate applyTime;

    @Column(name = "approve_user", length = 64)
    private String approveUser;

    @Column(name = "approve_time")
    private LocalDate approveTime;

    @Column(name = "implement_time")
    private LocalDate implementTime;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
