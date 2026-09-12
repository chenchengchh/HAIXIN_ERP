package com.hxcoe.aibrain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * AI 建议与人工反馈（S13 反馈学习环载体）
 * 所有决策引擎产出的建议统一落库：晨会简报/故障预测/风险预警/预审意见等
 * 人工处置（采纳/拒绝/修改）回写本表，定期分析采纳率形成学习环
 */
@Data
@Entity
@Table(name = "ai_suggestion")
public class AiSuggestionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 建议类型：MORNING_BRIEFING/FAULT_PREDICTION/DELIVERY_RISK/ENERGY_LOAD/PREAUDIT/ROOT_CAUSE/STOCK_SHORTAGE/SAFETY_STOCK/EIGHT_D */
    @Column(name = "suggestion_type", nullable = false, length = 64)
    private String suggestionType;

    /** 来源模块：EAM/SRM/MES/EMS/OA/QMS/WMS/LES/CROSS */
    @Column(name = "module", nullable = false, length = 32)
    private String module;

    /** 建议标题（简报列表直接展示） */
    @Column(name = "title", nullable = false)
    private String title;

    /** 严重级别：INFO/WARNING/CRITICAL */
    @Column(name = "severity", nullable = false, length = 16)
    private String severity = "INFO";

    /** AI 分析过程与证据链（JSON：sources/指标值/推演步骤） */
    @Column(name = "analysis", columnDefinition = "TEXT")
    private String analysis;

    /** 建议动作草稿（CONFIRM级，JSON：endpoint/method/payload） */
    @Column(name = "action_draft", columnDefinition = "TEXT")
    private String actionDraft;

    /** 自动化级别：AUTO/CONFIRM（FORBIDDEN 永不落库） */
    @Column(name = "automation_level", nullable = false, length = 16)
    private String automationLevel = "AUTO";

    /** 状态：PENDING/ACCEPTED/REJECTED/MODIFIED/EXECUTED */
    @Column(name = "status", nullable = false, length = 16)
    private String status = "PENDING";

    /** 人工处置说明 */
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    /** 幂等键（建议类型+业务键，防重复生成） */
    @Column(name = "event_id", unique = true, length = 128)
    private String eventId;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Column(name = "resolved_time")
    private LocalDateTime resolvedTime;
}
