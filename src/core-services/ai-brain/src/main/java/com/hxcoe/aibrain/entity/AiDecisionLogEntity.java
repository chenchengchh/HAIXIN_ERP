package com.hxcoe.aibrain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * AI 决策执行日志（P4-8 决策可观测性载体）
 * 每次技能执行（定时/事件/手动）留痕：扫描数、命中数、耗时、结果，
 * 供技能健康看板聚合分析与连续失败告警使用
 */
@Data
@Entity
@Table(name = "ai_decision_log", indexes = {
        @Index(name = "idx_skill_time", columnList = "skill_code, created_time")
})
public class AiDecisionLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联网关 traceId（预留，P4-7 LLM 接入后串联调用链） */
    @Column(name = "trace_id", length = 64)
    private String traceId;

    /** 技能编码：FAULT_PREDICTION/DELIVERY_RISK/ENERGY_LOAD/PREAUDIT/SAFETY_STOCK/STOCK_SHORTAGE/DQ_SENTINEL/MORNING_BRIEFING */
    @Column(name = "skill_code", nullable = false, length = 64)
    private String skillCode;

    /** 触发方式：SCHEDULED/EVENT/MANUAL */
    @Column(name = "trigger_type", nullable = false, length = 16)
    private String triggerType;

    /** 扫描记录数（技能自检对象总数） */
    @Column(name = "scanned_count")
    private Integer scannedCount = 0;

    /** 命中生成建议数 */
    @Column(name = "matched_count")
    private Integer matchedCount = 0;

    /** 跳过原因（无数据/未达阈值/幂等） */
    @Column(name = "skipped_reason")
    private String skippedReason;

    /** 执行耗时（毫秒） */
    @Column(name = "duration_ms")
    private Integer durationMs;

    /** LLM 消耗 token 数（P4-7 接入后回填） */
    @Column(name = "llm_tokens")
    private Integer llmTokens = 0;

    /** 执行结果：SUCCESS/ERROR */
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    /** 失败原因（截断 500 字符） */
    @Column(name = "error_msg", length = 500)
    private String errorMsg;

    @Column(name = "created_time")
    private LocalDateTime createdTime;
}
