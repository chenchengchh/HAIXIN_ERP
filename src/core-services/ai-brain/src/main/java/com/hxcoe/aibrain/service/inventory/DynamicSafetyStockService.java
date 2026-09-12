package com.hxcoe.aibrain.service.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.service.SuggestionService;
import com.hxcoe.aibrain.service.ontology.OntologyQueryService;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 动态安全库存（S03，规则版）
 * 算法：近90天已批准领用消耗速率 → 建议安全库存 = 周均消耗 × 2周覆盖 × 1.25 波动系数
 * 与现值偏差 >20% 时生成调整建议（CONFIRM 级草稿）
 */
@Slf4j
@Service
public class DynamicSafetyStockService {

    private static final double DEVIATION_THRESHOLD = 0.20;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OntologyQueryService ontology;

    /**
     * 计算全部备件的建议安全库存并生成调整建议
     * P4-4 本体层：备件对象经本体查询（含 trusted 闸门）；消耗聚合表名取自注册表，无物理表硬编码
     *
     * @return 新增建议数
     */
    public int scan() {
        // 本体可信度闸门：spare_part 被 S12 哨兵标记 trusted=0 时自动跳过，数据修复前不对 AI 开放
        OntologyQueryService.OntologyQuery spareQuery = ontology.findObjects("spare_part");
        if (spareQuery.blocked()) {
            log.warn("动态安全库存跳过：{}", spareQuery.blockReason());
            return 0;
        }
        String issueTable = ontology.sourceTableOf("spare_issue");
        if (issueTable == null) {
            log.warn("动态安全库存跳过：本体对象未注册 spare_issue");
            return 0;
        }

        // 备件主数据走本体层对象查询
        List<Map<String, Object>> spares = spareQuery.list();
        // 近90天已批准领用消耗按备件聚合（内存 JOIN，避免 N+1）
        List<Map<String, Object>> consumption = jdbcTemplate.queryForList(
                "SELECT spare_id, COALESCE(SUM(quantity), 0) AS consumed_90d FROM " + issueTable
                        + " WHERE status = 'approved' AND application_date >= DATE_SUB(NOW(), INTERVAL 90 DAY) GROUP BY spare_id",
                new MapSqlParameterSource());
        Map<Long, Double> consumedMap = new HashMap<>();
        for (Map<String, Object> c : consumption) {
            consumedMap.put(((Number) c.get("spare_id")).longValue(), ((Number) c.get("consumed_90d")).doubleValue());
        }

        int created = 0;
        for (Map<String, Object> row : spares) {
            try {
                row.put("consumed_90d", consumedMap.getOrDefault(((Number) row.get("id")).longValue(), 0.0));
                AiSuggestionEntity s = evaluate(row);
                if (s != null && suggestionService.publish(s)) {
                    created++;
                }
            } catch (Exception e) {
                log.warn("动态安全库存评估失败 spare={}: {}", row.get("code"), e.getMessage());
            }
        }
        log.info("动态安全库存: 评估备件{}种, 新增调整建议{}条", spares.size(), created);
        return created;
    }

    /**
     * 评估单备件安全库存合理性
     */
    private AiSuggestionEntity evaluate(Map<String, Object> row) throws Exception {
        double consumed90d = ((Number) row.get("consumed_90d")).doubleValue();
        int currentSafety = ((Number) row.get("safety_stock")).intValue();
        int currentStock = ((Number) row.get("current_stock")).intValue();
        if (consumed90d <= 0) {
            return null;
        }
        // 算法冲突防护：缺货中（库存<安全库存）由 STOCK_SHORTAGE 闭环负责补货，不再建议调低安全库存
        boolean inShortage = currentStock < currentSafety;
        double weeklyConsumption = consumed90d / 13.0;
        int suggested = (int) Math.ceil(weeklyConsumption * 2 * 1.25);
        if (suggested <= 0 || currentSafety <= 0) {
            return null;
        }
        if (inShortage && suggested < currentSafety) {
            return null;
        }
        double deviation = Math.abs(suggested - currentSafety) * 1.0 / currentSafety;
        if (deviation < DEVIATION_THRESHOLD) {
            return null;
        }

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("spareCode", row.get("code"));
        metrics.put("consumed90d", consumed90d);
        metrics.put("weeklyConsumption", Math.round(weeklyConsumption * 100.0) / 100.0);
        metrics.put("currentSafetyStock", currentSafety);
        metrics.put("suggestedSafetyStock", suggested);
        metrics.put("currentStock", row.get("current_stock"));

        AiSuggestionEntity s = new AiSuggestionEntity();
        s.setSuggestionType("SAFETY_STOCK");
        s.setModule("EAM");
        s.setSeverity("INFO");
        s.setTitle(String.format("安全库存建议：%s（%s）周均消耗 %.1f，建议安全库存 %d → %d",
                row.get("name"), row.get("code"), weeklyConsumption, currentSafety, suggested));
        s.setAutomationLevel("CONFIRM");
        s.setEventId("SAFETY-STOCK-" + row.get("id") + "-" + LocalDate.now().withDayOfMonth(1));

        Map<String, Object> action = new HashMap<>();
        action.put("type", "ADJUST_SAFETY_STOCK");
        // 端点对齐 EAM SparePartController 实际路径（PUT /api/v1/eam/spares/{id}，全量更新实体）
        action.put("endpoint", "/api/v1/eam/spares/" + row.get("id"));
        action.put("method", "PUT");
        // payload 携带完整实体字段，防止 PUT 全量保存时将其他字段置空，仅 safetyStock 为调整值
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", row.get("name"));
        payload.put("code", row.get("code"));
        payload.put("model", row.get("model"));
        payload.put("category", row.get("category"));
        payload.put("supplier", row.get("supplier"));
        payload.put("unit", row.get("unit"));
        payload.put("currentStock", currentStock);
        payload.put("safetyStock", suggested);
        action.put("payload", payload);
        action.put("description", "调整安全库存（人工确认后生效，执行成功可在EAM备件列表查看）");
        action.put("confirmText", "确认将 " + row.get("name") + " 安全库存 " + currentSafety + " 调整为 " + suggested + "？");
        action.put("rollbackHint", "调整后如需还原，可在 EAM 备件列表手动编辑安全库存");
        s.setActionDraft(objectMapper.writeValueAsString(action));

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("algorithm", "周均消耗×2周覆盖×1.25波动系数");
        analysis.put("evidence", Map.of("sources",
                List.of("eam_db.eam_spare_part", "eam_db.eam_spare_issue"), "metrics", metrics));
        s.setAnalysis(objectMapper.writeValueAsString(analysis));
        return s;
    }
}
