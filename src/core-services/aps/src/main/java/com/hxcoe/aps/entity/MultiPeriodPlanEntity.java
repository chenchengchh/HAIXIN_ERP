package com.hxcoe.aps.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 多周期计划主表实体：由生产计划按周期类型拆分为多个周期片段
 */
@Data
@Entity
@Table(name = "aps_multi_period_plan")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiPeriodPlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_no", nullable = false, unique = true, length = 50)
    private String planNo;

    @Column(name = "source_plan_id", nullable = false)
    private Long sourcePlanId;

    @Column(name = "source_plan_no", length = 50)
    private String sourcePlanNo;

    @Column(name = "plan_name", nullable = false, length = 100)
    private String planName;

    /**
     * 周期类型：daily/weekly/monthly/quarterly
     */
    @Column(name = "period_type", nullable = false, length = 20)
    private String periodType;

    @Column(name = "period_count", nullable = false)
    private Integer periodCount;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "algorithm", length = 50)
    private String algorithm;

    /**
     * 状态：已生成/执行中/已完成/已取消
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(name = "created_time", nullable = false, updatable = false)
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
