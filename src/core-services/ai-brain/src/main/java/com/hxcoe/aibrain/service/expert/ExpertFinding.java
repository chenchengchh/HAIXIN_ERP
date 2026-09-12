package com.hxcoe.aibrain.service.expert;

import java.util.HashMap;
import java.util.Map;

/**
 * 领域专家会诊结论（P4-6 多 Agent 编排标准载体）
 * 各 Specialist Expert 只读查询本领域数据，输出统一结构的发现：
 * 状态分级 + 一句话结论 + 关键指标 + 可选 CONFIRM 级补充动作草稿
 */
public class ExpertFinding {

    /** 领域：EQUIPMENT/INVENTORY/PROCUREMENT/PRODUCTION */
    private String domain;

    /** 状态：OK（无异常）/WARNING/CRITICAL/UNKNOWN（查询失败，不阻塞会诊） */
    private String status;

    /** 一句话结论（面向决策者的自然语言摘要） */
    private String summary;

    /** 关键指标（结构化度量，供前端展示与交叉验证） */
    private Map<String, Object> metrics = new HashMap<>();

    /** 补充动作草稿（可空；契约同 action_draft：endpoint/method/payload/description/confirmText） */
    private Map<String, Object> actionDraft;

    public ExpertFinding() { }

    public ExpertFinding(String domain, String status, String summary) {
        this.domain = domain;
        this.status = status;
        this.summary = summary;
    }

    /** 异常兜底构造：专家自身查询失败时返回 UNKNOWN，保证会诊不中断 */
    public static ExpertFinding unknown(String domain, String reason) {
        return new ExpertFinding(domain, "UNKNOWN", "本领域查询失败：" + reason);
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public void setMetrics(Map<String, Object> metrics) {
        this.metrics = metrics;
    }

    public Map<String, Object> getActionDraft() {
        return actionDraft;
    }

    public void setActionDraft(Map<String, Object> actionDraft) {
        this.actionDraft = actionDraft;
    }
}
