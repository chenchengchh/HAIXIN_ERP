package com.hxcoe.opportunity.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "opportunity_opportunity")
public class OpportunityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "opportunity_code", unique = true, nullable = false, length = 30)
    private String opportunityCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_name", nullable = false, length = 50)
    private String customerName;

    @Column(name = "estimated_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal estimatedAmount;

    @Column(name = "win_probability")
    private Integer winProbability;

    @Column(name = "stage", nullable = false, length = 20)
    private String stage;

    @Column(name = "expected_close_date")
    private LocalDateTime expectedCloseDate;

    @Column(name = "owner", nullable = false, length = 50)
    private String owner;

    @Column(name = "source", length = 50)
    private String source;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @PrePersist
    public void prePersist() {
        this.createdTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedTime = LocalDateTime.now();
    }
}
