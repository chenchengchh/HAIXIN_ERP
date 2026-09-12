package com.hxcoe.aibrain.service.expert;

import com.hxcoe.aibrain.service.ontology.OntologyQueryService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 采购领域专家（P4-6 Specialist 工具化）
 * 封装在途采购评估能力：逾期未到货订单、供应保障风险
 * 主数据走本体层（purchase_order 对象），逾期判定含状态语义过滤
 */
@Slf4j
@Component
public class ProcurementExpert {

    @Autowired
    private OntologyQueryService ontology;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * 评估在途采购延迟（逾期未到货且状态非完成/取消的订单）
     *
     * @return 采购领域发现
     */
    public ExpertFinding assessOverdueOrders() {
        OntologyQueryService.OntologyQuery query = ontology.findObjects("purchase_order");
        if (query.blocked()) {
            return ExpertFinding.unknown("PROCUREMENT", query.blockReason());
        }
        // 逾期判定需 NOW() 比较与状态语义排除，走注册表表名的条件 SQL（表名来自本体，无硬编码）
        String table = ontology.sourceTableOf("purchase_order");
        List<Map<String, Object>> overdue = jdbcTemplate.queryForList(
                "SELECT order_no, supplier_name, expected_delivery_date FROM " + table
                        + " WHERE expected_delivery_date < NOW() "
                        + "AND (status IS NULL OR (UPPER(status) NOT LIKE '%COMPLETE%' AND UPPER(status) NOT LIKE '%RECEIVED%' "
                        + "AND UPPER(status) NOT LIKE '%CANCEL%')) ORDER BY expected_delivery_date ASC LIMIT 5",
                new MapSqlParameterSource());

        ExpertFinding finding = new ExpertFinding();
        finding.setDomain("PROCUREMENT");
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("overdueCount", overdue.size());
        metrics.put("overdueOrders", overdue);
        finding.setMetrics(metrics);

        if (overdue.isEmpty()) {
            finding.setStatus("OK");
            finding.setSummary("在途采购无逾期，供应保障正常");
        } else {
            finding.setStatus(overdue.size() >= 3 ? "CRITICAL" : "WARNING");
            finding.setSummary(String.format("%d 张采购订单逾期未到货（最早 %s，供应商 %s），备件补给受阻",
                    overdue.size(), overdue.get(0).get("expected_delivery_date"), overdue.get(0).get("supplier_name")));
        }
        return finding;
    }
}
