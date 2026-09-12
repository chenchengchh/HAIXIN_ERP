package com.hxcoe.bom.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 替代规则配置实体类
 * 管理替代料的全局/分类级策略规则，如优先级策略、比例策略、生效条件等
 */
@Entity
@Table(name = "bom_substitute_rule")
@Data
public class SubstituteRuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 规则名称 */
    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    /** 规则类型：1-优先级策略 2-比例策略 3-生效条件 */
    @Column(name = "rule_type", nullable = false)
    private Integer ruleType;

    /** 适用分类ID，null表示全局 */
    @Column(name = "target_category_id")
    private Long targetCategoryId;

    /** 策略配置JSON，如 {"preferLowCost":true,"maxAlternatives":3} */
    @Column(name = "strategy_config", length = 1000)
    private String strategyConfig;

    /** 规则优先级（数字越小越优先） */
    @Column(name = "priority")
    private Integer priority;

    /** 生效日期 */
    @Column(name = "effective_date")
    private LocalDateTime effectiveDate;

    /** 失效日期 */
    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    /** 状态：0-禁用 1-启用 */
    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "remark", length = 500)
    private String remark;

    /** 适用分类名称（非持久化，查询时按targetCategoryId回填） */
    @Transient
    private String targetCategoryName;
}
