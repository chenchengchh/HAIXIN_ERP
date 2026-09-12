package com.hxcoe.aibrain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * AI 技能参数注册表（P4-2 反馈学习环载体）
 * 技能阈值从硬编码外置到本表，FeedbackTuner 依据人工处置采纳率自动调优，
 * 实现"越用越准"：误报多（拒绝率高）→ 阈值放宽；采纳率高 → 阈值收紧
 */
@Data
@Entity
@Table(name = "ai_skill_param", uniqueConstraints = {
        @UniqueConstraint(name = "uk_skill_scope_key", columnNames = {"skill_code", "scope", "param_key"})
})
public class AiSkillParamEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 技能编码（FAULT_PREDICTION/DELIVERY_RISK/ENERGY_LOAD 等） */
    @Column(name = "skill_code", nullable = false, length = 64)
    private String skillCode;

    /** 参数键（drift_threshold/overdue_rate_threshold/peak_load_ratio） */
    @Column(name = "param_key", nullable = false, length = 64)
    private String paramKey;

    /** 当前值 */
    @Column(name = "param_value", nullable = false, length = 128)
    private String paramValue;

    /** 出厂默认值（人工重置用） */
    @Column(name = "default_value", nullable = false, length = 128)
    private String defaultValue;

    /** 作用域：GLOBAL 或设备/供应商级（最小启动包仅用 GLOBAL） */
    @Column(name = "scope", length = 128)
    private String scope = "GLOBAL";

    /** 是否允许自动调优 */
    @Column(name = "auto_tune")
    private Boolean autoTune = true;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /** 最后修改者：AUTO_TUNE 或人工账号 */
    @Column(name = "updated_by", length = 32)
    private String updatedBy;
}
