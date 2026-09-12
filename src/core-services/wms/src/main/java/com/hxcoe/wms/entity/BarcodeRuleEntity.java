package com.hxcoe.wms.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wms_barcode_rule")
public class BarcodeRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String ruleCode;

    @Column(length = 128)
    private String ruleName;

    @Column(length = 32)
    private String ruleType;

    @Column(length = 256)
    private String ruleFormat;

    @Column(length = 512)
    private String description;

    @Column(length = 8)
    private String status;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdTime = now;
        updatedTime = now;
        if (status == null || status.isBlank()) {
            status = "1";
        }
        if (ruleType == null || ruleType.isBlank()) {
            ruleType = "material";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}

