package com.hxcoe.eam.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.result.Result;
import com.hxcoe.eam.client.AiBrainLlmClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 维修 Copilot（P4-7 LLM 语义增强版，AI决策系统 P0-3 起源）
 * 基于历史故障记录 + 维修记录 + 设备文档的相似故障检索，为维修工提供可参考的处置方案。
 * 检索策略：SQL 规则召回候选 → 规则评分排序取 top20 候选池 → ai-brain 本地 LLM 语义重排序
 * （识别同义/近义症状表述，突破 LIKE 关键词天花板）→ 按重排顺序取 top N 构建完整建议。
 * 降级铁律：LLM 不可用/重排输出解析失败时静默保持规则排序（evidence.mode=RULE_BASED）。
 */
@Service
public class MaintenanceCopilotService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceCopilotService.class);

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** LLM 重排序候选池大小（控制 prompt 长度与推理耗时） */
    private static final int RERANK_POOL_SIZE = 20;

    /** LLM 输出中的 faultId JSON 数组提取（容忍 ```json 包裹与前后多余文字） */
    private static final Pattern ID_ARRAY = Pattern.compile("\\[[0-9,\\s]*\\]");

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AiBrainLlmClient aiBrainLlmClient;

    /**
     * 检索相似故障的维修建议
     *
     * @param equipmentId 设备ID（可选，限定同设备历史）
     * @param symptom 症状关键词（可选，匹配故障类型/描述）
     * @param limit 返回条数（默认5，上限20）
     * @return 统一信封 {success, evidence, advices}
     */
    public Map<String, Object> faultAdvice(Long equipmentId, String symptom, Integer limit) {
        int safeLimit = limit == null || limit < 1 ? 5 : Math.min(limit, 20);
        String keyword = symptom == null ? "" : symptom.trim();

        // 1. 候选故障记录：同设备 或 关键词命中（type/description）
        List<Map<String, Object>> faults = queryFaultCandidates(equipmentId, keyword);

        // 2. 规则评分排序，取 top20 候选池
        List<Map<String, Object>> pool = new ArrayList<>(faults);
        pool.sort(Comparator.comparingDouble(f -> -scoreFault(f, equipmentId, keyword)));
        if (pool.size() > RERANK_POOL_SIZE) {
            pool = new ArrayList<>(pool.subList(0, RERANK_POOL_SIZE));
        }

        // 3. LLM 语义重排序（仅有关键词且候选≥2时有意义；失败静默保持规则序）
        boolean llmReranked = false;
        if (!keyword.isEmpty() && pool.size() >= 2) {
            llmReranked = tryLlmRerank(keyword, pool);
        }

        // 4. 按最终顺序取 top N 构建完整建议（维修记录 + 设备文档关联）
        List<Map<String, Object>> advices = new ArrayList<>();
        for (Map<String, Object> fault : pool) {
            if (advices.size() >= safeLimit) {
                break;
            }
            advices.add(buildAdvice(fault, equipmentId, keyword));
        }

        // 5. 统一信封：evidence 证据链（来源表 + 查询时间 + 候选数 + 排序模式）
        Map<String, Object> evidence = new HashMap<>();
        evidence.put("sources", List.of("eam_fault_record", "eam_maintenance_record", "eam_asset_document"));
        evidence.put("queryTime", LocalDateTime.now().format(DT_FMT));
        evidence.put("candidateCount", faults.size());
        evidence.put("mode", llmReranked ? "LLM_RERANKED" : "RULE_BASED");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("evidence", evidence);
        result.put("advices", advices);
        return result;
    }

    /**
     * 构建单条完整建议：基础故障信息 + 最近维修方案 + 关联设备文档
     */
    private Map<String, Object> buildAdvice(Map<String, Object> fault, Long equipmentId, String keyword) {
        double score = scoreFault(fault, equipmentId, keyword);
        Long faultEquipmentId = toLong(fault.get("equipment_id"));

        Map<String, Object> advice = new HashMap<>();
        advice.put("score", Math.round(score * 100.0) / 100.0);
        advice.put("faultId", fault.get("id"));
        advice.put("faultType", fault.get("type"));
        advice.put("equipmentId", faultEquipmentId);
        advice.put("equipmentName", fault.get("equipment_name"));
        advice.put("symptom", fault.get("description"));
        advice.put("reportTime", formatTime(fault.get("report_time")));
        advice.put("faultStatus", fault.get("status"));

        // 匹配同设备时间最接近的维修记录作为参考方案
        Map<String, Object> repair = findNearestRepair(faultEquipmentId, fault.get("report_time"));
        if (repair != null) {
            advice.put("solution", repair.get("content"));
            advice.put("maintainer", repair.get("maintainer"));
            advice.put("resolvedTime", formatTime(repair.get("maintenance_time")));
            advice.put("cost", repair.get("cost"));
        }

        // 关联该设备的有效文档（说明书/图纸等，维修时可查阅）
        advice.put("documents", listAssetDocuments(faultEquipmentId));
        return advice;
    }

    /**
     * LLM 语义重排序候选池（原地重排 pool）
     * 流程：候选池序列化为轻量 JSON（faultId/type/description）→ ai-brain skill-chat 端点
     * → 解析 faultId 数组按序重排（LLM 遗漏的 id 按原顺序追加末尾，保证候选不丢失）
     * 降级：Feign 异常/信封失败/degraded/输出无 JSON 数组/解析异常均返回 false（保持规则序）
     *
     * @param keyword 症状关键词
     * @param pool    候选池（成功时原地按 LLM 相关性重排）
     * @return true=LLM 重排已生效；false=保持规则排序
     */
    private boolean tryLlmRerank(String keyword, List<Map<String, Object>> pool) {
        try {
            List<Map<String, Object>> candidates = new ArrayList<>();
            for (Map<String, Object> f : pool) {
                Map<String, Object> c = new LinkedHashMap<>();
                c.put("faultId", f.get("id"));
                c.put("type", f.get("type"));
                c.put("description", f.get("description"));
                candidates.add(c);
            }
            String userPrompt = "症状描述：" + keyword + "\n候选历史故障记录：\n"
                    + objectMapper.writeValueAsString(candidates);

            Result<Map<String, Object>> resp = aiBrainLlmClient.skillChat(Map.of(
                    "skillCode", "MAINTENANCE_COPILOT_RERANK",
                    "userPrompt", userPrompt));
            if (resp == null || resp.getData() == null
                    || !(Integer.valueOf(0).equals(resp.getCode()) || Integer.valueOf(200).equals(resp.getCode()))) {
                return false;
            }
            Object degraded = resp.getData().get("degraded");
            Object content = resp.getData().get("content");
            if (Boolean.TRUE.equals(degraded) || content == null) {
                return false;
            }

            Matcher m = ID_ARRAY.matcher(String.valueOf(content));
            if (!m.find()) {
                log.warn("Copilot LLM 重排输出未含 id 数组，保持规则序: {}", abbreviate(String.valueOf(content)));
                return false;
            }
            JsonNode array = objectMapper.readTree(m.group());
            if (!array.isArray() || array.isEmpty()) {
                return false;
            }

            // 按 LLM 给出的 faultId 顺序重排候选池
            Map<Long, Map<String, Object>> byId = new LinkedHashMap<>();
            for (Map<String, Object> f : pool) {
                byId.put(toLong(f.get("id")), f);
            }
            List<Map<String, Object>> reordered = new ArrayList<>();
            for (JsonNode idNode : array) {
                Long id = idNode.isNumber() ? idNode.asLong() : toLong(idNode.asText());
                Map<String, Object> hit = byId.remove(id);
                if (hit != null) {
                    reordered.add(hit);
                }
            }
            // LLM 遗漏的候选按原相对顺序追加末尾（不丢弃任何召回结果）
            reordered.addAll(byId.values());
            if (reordered.size() != pool.size()) {
                return false;
            }
            pool.clear();
            pool.addAll(reordered);
            return true;
        } catch (Exception e) {
            log.warn("Copilot LLM 重排失败（保持规则序）: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 日志截断辅助（LLM 原始输出可能较长）
     */
    private String abbreviate(String text) {
        return text == null ? "null" : (text.length() <= 120 ? text : text.substring(0, 120) + "…");
    }

    /**
     * 查询候选故障记录（同设备优先，关键词次之；两者都为空时返回最近故障）
     */
    private List<Map<String, Object>> queryFaultCandidates(Long equipmentId, String keyword) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, equipment_id, equipment_name, type, description, status, report_time "
                        + "FROM eam_fault_record WHERE 1=1 ");
        MapSqlParameterSource params = new MapSqlParameterSource();
        List<String> conditions = new ArrayList<>();
        if (equipmentId != null) {
            conditions.add("equipment_id = :equipmentId");
            params.addValue("equipmentId", equipmentId);
        }
        if (!keyword.isEmpty()) {
            conditions.add("(type LIKE :kw OR description LIKE :kw)");
            params.addValue("kw", "%" + keyword + "%");
        }
        if (!conditions.isEmpty()) {
            sql.append("AND (").append(String.join(" OR ", conditions)).append(") ");
        }
        sql.append("ORDER BY report_time DESC LIMIT 50");
        return jdbcTemplate.queryForList(sql.toString(), params);
    }

    /**
     * 相似度评分：同设备 +0.5，关键词命中 type +0.2 / description +0.2，近90天 +0.1（上限1.0）
     */
    private double scoreFault(Map<String, Object> fault, Long equipmentId, String keyword) {
        double score = 0.0;
        if (equipmentId != null && equipmentId.equals(toLong(fault.get("equipment_id")))) {
            score += 0.5;
        }
        if (!keyword.isEmpty()) {
            String type = fault.get("type") == null ? "" : String.valueOf(fault.get("type"));
            String desc = fault.get("description") == null ? "" : String.valueOf(fault.get("description"));
            if (type.contains(keyword)) {
                score += 0.2;
            }
            if (desc.contains(keyword)) {
                score += 0.2;
            }
        }
        Object reportTime = fault.get("report_time");
        if (reportTime instanceof LocalDateTime
                && ((LocalDateTime) reportTime).isAfter(LocalDateTime.now().minusDays(90))) {
            score += 0.1;
        }
        return Math.min(1.0, score);
    }

    /**
     * 查找同设备与故障时间最接近的维修记录（优先故障之后48小时内的）
     */
    private Map<String, Object> findNearestRepair(Long equipmentId, Object reportTime) {
        if (equipmentId == null) {
            return null;
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("equipmentId", equipmentId);
        String timeCondition = "";
        if (reportTime instanceof LocalDateTime) {
            timeCondition = "AND maintenance_time >= :startTime AND maintenance_time <= :endTime ";
            params.addValue("startTime", ((LocalDateTime) reportTime).minusHours(1));
            params.addValue("endTime", ((LocalDateTime) reportTime).plusDays(14));
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, content, maintainer, maintenance_time, cost FROM eam_maintenance_record "
                        + "WHERE equipment_id = :equipmentId " + timeCondition
                        + "ORDER BY maintenance_time ASC LIMIT 1",
                params
        );
        // 时间窗内无匹配则回退取该设备最近一次维修
        if (rows.isEmpty() && !timeCondition.isEmpty()) {
            MapSqlParameterSource fallback = new MapSqlParameterSource();
            fallback.addValue("equipmentId", equipmentId);
            rows = jdbcTemplate.queryForList(
                    "SELECT id, content, maintainer, maintenance_time, cost FROM eam_maintenance_record "
                            + "WHERE equipment_id = :equipmentId ORDER BY maintenance_time DESC LIMIT 1",
                    fallback
            );
        }
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * 列出设备的有效文档（排除已归档）
     */
    private List<Map<String, Object>> listAssetDocuments(Long assetId) {
        if (assetId == null) {
            return List.of();
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("assetId", assetId);
        return jdbcTemplate.queryForList(
                "SELECT id, name, type, file_name, status FROM eam_asset_document "
                        + "WHERE asset_id = :assetId AND (status IS NULL OR status <> 'archived') "
                        + "ORDER BY id DESC LIMIT 10",
                params
        );
    }

    private Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String formatTime(Object time) {
        if (time instanceof LocalDateTime) {
            return ((LocalDateTime) time).format(DT_FMT);
        }
        return time == null ? null : String.valueOf(time);
    }
}
