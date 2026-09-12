package com.hxcoe.aibrain.service.inventory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.service.SuggestionService;
import com.hxcoe.aibrain.service.evidence.Evidence;
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
 * 备件缺货决策闭环（Palantir 库存推演复刻，EAM→SRM→OA）
 * 触发：备件当前库存 < 安全库存
 * 推演：未来30天维保计划需求 + 缺口计算 + 采购申请草稿（CONFIRM 级，走 SRM/OA 现有端点）
 */
@Slf4j
@Service
public class StockShortageService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 扫描备件缺货并生成采购决策建议
     *
     * @return 新增建议数
     */
    public int scan() {
        List<Map<String, Object>> shortages = jdbcTemplate.queryForList(
                "SELECT id, name, code, current_stock, safety_stock, supplier, unit "
                        + "FROM eam_db.eam_spare_part WHERE current_stock < safety_stock",
                new MapSqlParameterSource());
        int created = 0;
        for (Map<String, Object> spare : shortages) {
            try {
                AiSuggestionEntity s = evaluate(spare);
                if (s != null && suggestionService.publish(s)) {
                    created++;
                }
            } catch (Exception e) {
                log.warn("备件缺货推演失败 spare={}: {}", spare.get("code"), e.getMessage());
            }
        }
        log.info("备件缺货决策: 检出缺货备件{}种, 新增决策建议{}条", shortages.size(), created);
        return created;
    }

    /**
     * 单备件缺货推演：缺口 = max(2倍安全库存 - 当前库存, 未来30天需求缺口)
     */
    private AiSuggestionEntity evaluate(Map<String, Object> spare) throws Exception {
        long spareId = ((Number) spare.get("id")).longValue();
        int currentStock = ((Number) spare.get("current_stock")).intValue();
        int safetyStock = ((Number) spare.get("safety_stock")).intValue();

        // 未来30天维保计划关联需求（eam_spare_demand_plan suggested_date 在30天内）
        Long plannedDemand = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(required_qty), 0) FROM eam_db.eam_spare_demand_plan "
                        + "WHERE spare_id = :spareId AND suggested_date <= DATE_ADD(NOW(), INTERVAL 30 DAY)",
                new MapSqlParameterSource().addValue("spareId", spareId), Long.class);
        plannedDemand = plannedDemand == null ? 0 : plannedDemand;

        int replenishTarget = safetyStock * 2;
        int gap = Math.max(replenishTarget - currentStock, (int) (plannedDemand - currentStock));
        if (gap <= 0) {
            gap = replenishTarget - currentStock;
        }

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("spareCode", spare.get("code"));
        metrics.put("currentStock", currentStock);
        metrics.put("safetyStock", safetyStock);
        metrics.put("plannedDemand30d", plannedDemand);
        metrics.put("suggestedQty", gap);
        metrics.put("preferredSupplier", spare.get("supplier"));

        AiSuggestionEntity s = new AiSuggestionEntity();
        s.setSuggestionType("STOCK_SHORTAGE");
        s.setModule("EAM");
        s.setSeverity(currentStock <= 0 ? "CRITICAL" : "WARNING");
        s.setTitle(String.format("备件缺货：%s（%s）库存 %d 低于安全库存 %d，建议补货 %d 件",
                spare.get("name"), spare.get("code"), currentStock, safetyStock, gap));
        s.setAutomationLevel("CONFIRM");
        s.setEventId("STOCK-SHORTAGE-" + spareId + "-" + LocalDate.now());

        Map<String, Object> action = new HashMap<>();
        action.put("type", "CREATE_PURCHASE_REQUEST");
        action.put("endpoint", "/api/v1/srm/purchase-requests");
        action.put("method", "POST");
        // payload 对齐 PurchaseRequestEntity 必填与核心字段（requestCode 必填且唯一，按备件+日期幂等）
        Map<String, Object> payload = new HashMap<>();
        payload.put("requestCode", "PR-AI-" + spareId + "-"
                + LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE));
        payload.put("applicant", "AI决策引擎");
        payload.put("department", "设备管理部");
        payload.put("status", "PENDING");
        payload.put("purchaseType", "备件补货");
        payload.put("materialCode", spare.get("code"));
        payload.put("materialName", spare.get("name"));
        payload.put("quantity", gap);
        payload.put("unit", spare.get("unit"));
        payload.put("description", "AI备件缺货补货建议（EAM触发），缺口=" + gap
                + "，优选供应商=" + (spare.get("supplier") == null ? "无" : spare.get("supplier")));
        action.put("payload", payload);
        action.put("description", "创建SRM采购申请（人工确认后执行，执行成功可在SRM采购申请列表查看）");
        action.put("confirmText", "确认创建 SRM 采购申请（" + spare.get("name") + " × " + gap + "）？");
        action.put("rollbackHint", "采购申请创建后如需撤销，可在 SRM 采购申请列表删除");
        s.setActionDraft(objectMapper.writeValueAsString(action));

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("algorithm", "缺口=max(2倍安全库存-当前库存, 30天计划需求-当前库存)");
        // P4-5 结构化证据：行级引用 + 下钻路由 + 推理步骤
        analysis.put("evidence", Evidence.of(
                List.of(
                        Evidence.source("eam_db", "eam_spare_part",
                                "code='" + spare.get("code") + "'", 1,
                                "/home/eam/spare-parts-management?keyword=" + spare.get("code")),
                        Evidence.source("eam_db", "eam_spare_demand_plan",
                                "spare_id=" + spareId + " AND 30天内计划", null,
                                "/home/eam/spare-parts-management")),
                metrics,
                List.of(
                        "当前库存 " + currentStock + " < 安全库存 " + safetyStock,
                        "未来30天计划需求 " + plannedDemand,
                        "补货目标 2×安全库存 = " + replenishTarget + "，缺口 = " + gap)));
        s.setAnalysis(objectMapper.writeValueAsString(analysis));
        return s;
    }
}
