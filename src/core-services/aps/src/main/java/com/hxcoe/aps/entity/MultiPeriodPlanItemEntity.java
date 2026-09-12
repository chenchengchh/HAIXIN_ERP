package com.hxcoe.aps.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 多周期计划明细实体：主计划拆分后的单个周期片段
 */
@Data
@Entity
@Table(name = "aps_multi_period_plan_item")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiPeriodPlanItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所属多周期计划ID（关联aps_multi_period_plan.id）
     */
    @Column(name = "plan_id", nullable = false)
    private Long planId;

    /**
     * 周期序号（从1开始）
     */
    @Column(name = "period_index", nullable = false)
    private Integer periodIndex;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    /**
     * 本周期分配的生产数量（总量按周期均分，余数归入最后一个周期）
     */
    @Column(name = "quantity")
    private BigDecimal quantity;

    /**
     * 状态：已生成/执行中/已完成
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }
}
