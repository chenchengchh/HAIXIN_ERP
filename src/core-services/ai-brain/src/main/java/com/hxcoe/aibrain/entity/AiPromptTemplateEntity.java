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
 * AI 提示词模板资产表（P4-7 LLM 接入载体）
 * 提示词版本化管理：各技能的 system prompt 从硬编码外置到本表，
 * 支持版本迭代与回滚（对应 AIP Evals 的提示词治理能力）。
 * 模板内变量占位符格式：${varName}，由 PromptTemplateService 渲染替换。
 */
@Data
@Entity
@Table(name = "ai_prompt_template", uniqueConstraints = {
        @UniqueConstraint(name = "uk_skill_version", columnNames = {"skill_code", "version"})
})
public class AiPromptTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 技能编码（MAINTENANCE_COPILOT_RERANK/EIGHT_D_ROOTCAUSE/VISION_INVOICE_EXTRACT/VISION_GAUGE_EXTRACT） */
    @Column(name = "skill_code", nullable = false, length = 64)
    private String skillCode;

    /** 模板版本号（同技能多版本，取启用中最高版本） */
    @Column(name = "version", nullable = false)
    private Integer version;

    /** 模板用途说明 */
    @Column(name = "description", length = 255)
    private String description;

    /** 模板内容（system prompt，变量占位符 ${varName}） */
    @Column(name = "template", nullable = false, columnDefinition = "TEXT")
    private String template;

    /** 是否启用 */
    @Column(name = "enabled")
    private Boolean enabled = true;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /** 最后修改者：SEED 或人工账号 */
    @Column(name = "updated_by", length = 32)
    private String updatedBy;
}
