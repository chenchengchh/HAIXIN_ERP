package com.hxcoe.aibrain.service;

import com.hxcoe.aibrain.entity.AiDecisionLogEntity;
import com.hxcoe.aibrain.repository.AiDecisionLogRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI 决策日志服务（P4-8 决策可观测性核心）
 * 职责：技能执行统一留痕（耗时/命中/成败）、技能健康度聚合
 * 埋点方式：DecisionJob 定时任务与 DecisionController 手动触发统一经 record() 包装
 */
@Slf4j
@Service
public class DecisionLogService {

    @Autowired
    private AiDecisionLogRepository decisionLogRepository;

    /**
     * 包装技能执行并留痕：记录耗时、命中数（按返回值推断）、执行结果
     *
     * @param skillCode 技能编码
     * @param triggerType 触发方式 SCHEDULED/EVENT/MANUAL
     * @param action 技能执行体
     * @param <T> 返回值类型（Collection→size，Number→intValue，实体→1，null→0）
     * @return 技能原始返回值
     */
    public <T> T record(String skillCode, String triggerType, Supplier<T> action) {
        long start = System.currentTimeMillis();
        AiDecisionLogEntity entry = new AiDecisionLogEntity();
        entry.setSkillCode(skillCode);
        entry.setTriggerType(triggerType);
        try {
            T result = action.get();
            entry.setStatus("SUCCESS");
            entry.setMatchedCount(inferMatchedCount(result));
            if (entry.getMatchedCount() == 0) {
                entry.setSkippedReason("未达阈值或无新增");
            }
            return result;
        } catch (Exception e) {
            entry.setStatus("ERROR");
            entry.setErrorMsg(truncate(e.getMessage(), 500));
            throw e;
        } finally {
            entry.setDurationMs((int) (System.currentTimeMillis() - start));
            entry.setCreatedTime(LocalDateTime.now());
            try {
                decisionLogRepository.save(entry);
            } catch (Exception saveEx) {
                // 留痕失败不阻断技能主流程
                log.warn("决策日志落库失败 skill={}: {}", skillCode, saveEx.getMessage());
            }
        }
    }

    /**
     * 技能健康度聚合（健康看板数据源）
     * 统计各技能近 24h / 7d 的执行次数、成功率、平均耗时、建议产出数，并计算连续失败标红
     *
     * @return {skills: [{skillCode, runs24h, errors24h, successRate24h, avgDurationMs24h,
     *                    produced24h, runs7d, errors7d, successRate7d, avgDurationMs7d,
     *                    produced7d, consecutiveErrors, health, lastRunTime}]}
     */
    public Map<String, Object> skillHealth() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<AiDecisionLogEntity> logs = decisionLogRepository
                .findByCreatedTimeAfterOrderByCreatedTimeDesc(sevenDaysAgo);

        LocalDateTime oneDayAgo = LocalDateTime.now().minusHours(24);
        Map<String, List<AiDecisionLogEntity>> bySkill = new LinkedHashMap<>();
        for (AiDecisionLogEntity l : logs) {
            bySkill.computeIfAbsent(l.getSkillCode(), k -> new ArrayList<>()).add(l);
        }

        List<Map<String, Object>> skills = new ArrayList<>();
        for (Map.Entry<String, List<AiDecisionLogEntity>> e : bySkill.entrySet()) {
            List<AiDecisionLogEntity> all = e.getValue();
            List<AiDecisionLogEntity> day = all.stream()
                    .filter(l -> l.getCreatedTime() != null && l.getCreatedTime().isAfter(oneDayAgo)).toList();

            Map<String, Object> row = new HashMap<>();
            row.put("skillCode", e.getKey());
            putWindowStats(row, day, "24h");
            putWindowStats(row, all, "7d");

            // 连续失败次数（列表已按时间倒序，从头数连续 ERROR）
            int consecutive = 0;
            for (AiDecisionLogEntity l : all) {
                if ("ERROR".equals(l.getStatus())) {
                    consecutive++;
                } else {
                    break;
                }
            }
            row.put("consecutiveErrors", consecutive);
            row.put("health", consecutive >= 3 ? "RED" : "GREEN");
            row.put("lastRunTime", all.isEmpty() || all.get(0).getCreatedTime() == null
                    ? null : all.get(0).getCreatedTime().toString().replace('T', ' ').substring(0, 19));
            skills.add(row);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("skills", skills);
        result.put("totalCount", logs.size());
        return result;
    }

    /**
     * 聚合单个时间窗口的执行统计并写入结果行
     *
     * @param row 目标行
     * @param logs 窗口内日志
     * @param suffix 字段后缀（24h/7d）
     */
    private void putWindowStats(Map<String, Object> row, List<AiDecisionLogEntity> logs, String suffix) {
        int runs = logs.size();
        long errors = logs.stream().filter(l -> "ERROR".equals(l.getStatus())).count();
        int produced = logs.stream().mapToInt(l -> l.getMatchedCount() == null ? 0 : l.getMatchedCount()).sum();
        int avgDuration = runs == 0 ? 0
                : (int) logs.stream().mapToInt(l -> l.getDurationMs() == null ? 0 : l.getDurationMs()).average().orElse(0);
        row.put("runs" + suffix, runs);
        row.put("errors" + suffix, errors);
        row.put("successRate" + suffix, runs == 0 ? 0 : Math.round((runs - errors) * 1000.0 / runs) / 10.0);
        row.put("avgDurationMs" + suffix, avgDuration);
        row.put("produced" + suffix, produced);
    }

    /**
     * LLM 调用留痕（P4-7 token 观测）
     * 轻量直写（不经 record() 包装）：OllamaLlmService 每次真实调用记录 skill 归属、token 消耗、耗时与结果，
     * 技能健康看板据此聚合 LLM 调用维度的成功率与耗时分布
     *
     * @param skillCode  发起调用的技能编码（如 MAINTENANCE_COPILOT_RERANK/EIGHT_D_ROOTCAUSE）
     * @param tokens     总 token 消耗（prompt_eval_count + eval_count，未知传 0）
     * @param durationMs 调用耗时（毫秒）
     * @param error      失败原因（成功传 null）
     */
    public void logLlmCall(String skillCode, int tokens, int durationMs, String error) {
        try {
            AiDecisionLogEntity entry = new AiDecisionLogEntity();
            entry.setSkillCode("LLM:" + skillCode);
            entry.setTriggerType("LLM_CALL");
            entry.setLlmTokens(tokens);
            entry.setDurationMs(durationMs);
            entry.setStatus(error == null ? "SUCCESS" : "ERROR");
            entry.setErrorMsg(truncate(error, 500));
            entry.setCreatedTime(LocalDateTime.now());
            decisionLogRepository.save(entry);
        } catch (Exception e) {
            // 留痕失败不阻断 LLM 主流程
            log.warn("LLM调用留痕落库失败 skill={}: {}", skillCode, e.getMessage());
        }
    }

    /**
     * 按返回值类型推断命中建议数
     */
    private int inferMatchedCount(Object result) {
        if (result == null) {
            return 0;
        }
        if (result instanceof Collection<?> c) {
            return c.size();
        }
        if (result instanceof Number n) {
            return n.intValue();
        }
        return 1;
    }

    /**
     * 字符串截断（防 errorMsg 超列宽）
     */
    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
