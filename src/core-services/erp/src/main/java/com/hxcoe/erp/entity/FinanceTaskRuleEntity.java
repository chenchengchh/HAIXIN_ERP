package com.hxcoe.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "erp_finance_task_rule")
public class FinanceTaskRuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fact_type", nullable = false, length = 32)
    private String factType;

    @Column(name = "result", nullable = false, length = 32)
    private String result;

    @Column(name = "action_type", nullable = false, length = 32)
    private String actionType;

    @Column(name = "voucher_type", length = 16)
    private String voucherType;

    @Column(name = "enabled", nullable = false)
    private Integer enabled;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
        if (enabled == null) {
            enabled = 1;
        }
        if (result == null || result.isBlank()) {
            result = "*";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
