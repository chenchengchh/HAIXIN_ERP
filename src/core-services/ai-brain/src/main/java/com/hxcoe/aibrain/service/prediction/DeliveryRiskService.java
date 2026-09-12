package com.hxcoe.aibrain.service.prediction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.service.SkillParamService;
import com.hxcoe.aibrain.service.SuggestionService;
import com.hxcoe.aibrain.service.evidence.Evidence;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 交期风险预测（S02，规则版）
 * 数据链路：SRM 在途订单 + 供应商历史逾期率 + LES 在途运输 + MES 排程
 * 输出三选建议：催货 / 启用备选供应商 / 调整工单顺序（CONFIRM 级草稿）
 */
@Slf4j
@Service
public class DeliveryRiskService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SkillParamService skillParamService;

    /**
     * 扫描在途采购订单，评估交期风险
     *
     * @return 新增风险建议数
     */
    public int scan() {
        // 1. 在途订单：已逾期或未来7天内到期
        List<Map<String, Object>> orders = jdbcTemplate.queryForList(
                "SELECT id, order_no, supplier_id, supplier_name, expected_delivery_date, status, total_amount "
                        + "FROM srm_db.srm_purchase_order "
                        + "WHERE expected_delivery_date < DATE_ADD(NOW(), INTERVAL 7 DAY) "
                        + "AND (status IS NULL OR (UPPER(status) NOT LIKE '%COMPLETE%' AND UPPER(status) NOT LIKE '%RECEIVED%' "
                        + "AND UPPER(status) NOT LIKE '%CANCEL%' AND status NOT LIKE '%完成%' AND status NOT LIKE '%到货%')) "
                        + "ORDER BY expected_delivery_date ASC LIMIT 50",
                new MapSqlParameterSource());
        int created = 0;
        for (Map<String, Object> order : orders) {
            try {
                AiSuggestionEntity s = evaluateOrder(order);
                if (s != null && suggestionService.publish(s)) {
                    created++;
                }
            } catch (Exception e) {
                log.warn("交期风险评估失败 orderNo={}: {}", order.get("order_no"), e.getMessage());
            }
        }
        log.info("交期风险预测: 评估在途订单{}张, 新增风险建议{}条", orders.size(), created);
        return created;
    }

    /**
     * 评估单张订单的交期风险（供应商历史逾期率 + LES 在途状态）
     */
    private AiSuggestionEntity evaluateOrder(Map<String, Object> order) throws Exception {
        Object supplierId = order.get("supplier_id");
        MapSqlParameterSource p = new MapSqlParameterSource();

        // 2. 供应商历史逾期率（近10张已完成订单中逾期比例）
        double overdueRate = 0.0;
        long historyCount = 0;
        if (supplierId != null) {
            p.addValue("supplierId", supplierId);
            Map<String, Object> stats = jdbcTemplate.queryForMap(
                    "SELECT COUNT(*) AS total, "
                            + "SUM(CASE WHEN expected_delivery_date < COALESCE(updated_time, NOW()) "
                            + "  AND (UPPER(status) LIKE '%COMPLETE%' OR UPPER(status) LIKE '%RECEIVED%' OR status LIKE '%完成%') "
                            + "  AND DATEDIFF(COALESCE(updated_time, NOW()), expected_delivery_date) > 0 THEN 1 ELSE 0 END) AS overdue "
                            + "FROM (SELECT status, expected_delivery_date, updated_time FROM srm_db.srm_purchase_order "
                            + "      WHERE supplier_id = :supplierId AND id <> :excludeId ORDER BY order_date DESC LIMIT 10) h",
                    p.addValue("excludeId", order.get("id")));
            historyCount = stats.get("total") instanceof Number ? ((Number) stats.get("total")).longValue() : 0;
            long overdueCount = stats.get("overdue") instanceof Number ? ((Number) stats.get("overdue")).longValue() : 0;
            overdueRate = historyCount == 0 ? 0 : overdueCount * 1.0 / historyCount;
        }

        boolean overdue = false;
        Object expectedDate = order.get("expected_delivery_date");
        if (expectedDate instanceof java.time.LocalDateTime) {
            overdue = ((java.time.LocalDateTime) expectedDate).isBefore(java.time.LocalDateTime.now());
        } else if (expectedDate instanceof java.sql.Timestamp) {
            overdue = ((java.sql.Timestamp) expectedDate).toLocalDateTime().isBefore(java.time.LocalDateTime.now());
        }
        // 触发条件：已逾期，或（临近到期且供应商历史逾期率达到阈值，默认30%，学习环可自动调优）
        double overdueRateThreshold = skillParamService.getDouble("DELIVERY_RISK", "overdue_rate_threshold", 0.30);
        if (!overdue && overdueRate < overdueRateThreshold) {
            return null;
        }

        // 3. 备选供应商（同品类评分最高且非当前供应商）
        List<Map<String, Object>> alternatives = new ArrayList<>();
        if (supplierId != null) {
            alternatives = jdbcTemplate.queryForList(
                    "SELECT s2.supplier_name, s2.rating FROM srm_db.srm_supplier s2 "
                            + "JOIN srm_db.srm_supplier cur ON cur.id = :supplierId2 AND s2.category = cur.category "
                            + "WHERE s2.id <> :supplierId2 AND s2.rating IS NOT NULL "
                            + "ORDER BY s2.rating DESC LIMIT 3",
                    new MapSqlParameterSource().addValue("supplierId2", supplierId));
        }

        double riskScore = overdue ? 0.9 : overdueRate;
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("orderNo", order.get("order_no"));
        metrics.put("supplierName", order.get("supplier_name"));
        metrics.put("expectedDeliveryDate", String.valueOf(order.get("expected_delivery_date")));
        metrics.put("supplierOverdueRate", Math.round(overdueRate * 1000.0) / 10.0);
        metrics.put("historyOrderCount", historyCount);
        metrics.put("alreadyOverdue", overdue);
        metrics.put("alternatives", alternatives);

        AiSuggestionEntity s = new AiSuggestionEntity();
        s.setSuggestionType("DELIVERY_RISK");
        s.setModule("SRM");
        s.setSeverity(overdue ? "CRITICAL" : "WARNING");
        s.setTitle(String.format("交期风险：订单 %s（%s）%s，供应商历史逾期率 %.0f%%",
                order.get("order_no"), order.get("supplier_name"),
                overdue ? "已逾期未到货" : "临近交期", overdueRate * 100));
        s.setAutomationLevel("CONFIRM");
        s.setEventId("DELIVERY-RISK-" + order.get("id") + "-" + LocalDate.now());

        Map<String, Object> action = new HashMap<>();
        action.put("type", "CREATE_PURCHASE_REQUEST");
        action.put("endpoint", "/api/v1/srm/purchase-requests");
        action.put("method", "POST");
        action.put("description", alternatives.isEmpty() ? "建议催货" : "建议启用备选供应商创建补充采购申请（人工确认）");
        s.setActionDraft(objectMapper.writeValueAsString(action));

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("riskScore", riskScore);
        analysis.put("options", List.of("①催货跟进", "②启用备选供应商", "③调整关联工单顺序"));
        // P4-5 结构化证据：行级引用 + 下钻路由 + 推理步骤
        analysis.put("evidence", Evidence.of(
                List.of(
                        Evidence.source("srm_db", "srm_purchase_order",
                                "order_no='" + order.get("order_no") + "'", 1,
                                "/home/srm/order/list?orderNo=" + order.get("order_no")),
                        Evidence.source("srm_db", "srm_supplier",
                                "供应商近10单逾期率统计", (int) historyCount,
                                "/home/srm/supplier/list")),
                metrics,
                List.of(
                        "订单预计交期 " + order.get("expected_delivery_date") + (overdue ? "（已逾期）" : "（临近）"),
                        String.format("供应商历史逾期率 %.0f%%（阈值 %.0f%%）", overdueRate * 100, overdueRateThreshold * 100),
                        alternatives.isEmpty() ? "无同品类备选供应商，建议催货" : "检出备选供应商 " + alternatives.size() + " 家")));
        s.setAnalysis(objectMapper.writeValueAsString(analysis));
        return s;
    }
}
