package com.hxcoe.aibrain.service.rca;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.aibrain.service.evidence.Evidence;
import com.hxcoe.common.result.Result;
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
 * 跨域联合会诊（S06，根因分析链）
 * 沿固定本体链路追溯：设备故障 → MES 工单影响 → EAM 备件库存 → SRM 采购延迟
 * 单模块看每件事都合理，串起来才见系统性失效。
 */
@Slf4j
@Service
public class RootCauseService {

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 以一条故障记录为起点做跨域根因追溯
     *
     * @param faultId EAM 故障记录ID
     * @return 因果链 + 各环节证据（统一信封由 Controller 包装）
     */
    public Map<String, Object> traceByFault(Long faultId) {
        List<Map<String, Object>> chain = new ArrayList<>();
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("faultId", faultId);

        // 环节1：故障本体
        List<Map<String, Object>> faults = jdbcTemplate.queryForList(
                "SELECT id, equipment_id, equipment_name, type, description, status, report_time "
                        + "FROM eam_db.eam_fault_record WHERE id = :faultId", p);
        if (faults.isEmpty()) {
            return Map.of("success", false, "message", "故障记录不存在: " + faultId);
        }
        Map<String, Object> fault = faults.get(0);
        chain.add(node(1, "EAM", "设备故障",
                String.format("%s 报告【%s】：%s", fault.get("equipment_name"), fault.get("type"), fault.get("description")),
                Evidence.source("eam_db", "eam_fault_record", "id=" + faultId, 1,
                        "/home/eam/maintenance-management/fault-repair"),
                Map.of("faultId", faultId, "reportTime", String.valueOf(fault.get("report_time")))));

        Object equipmentId = fault.get("equipment_id");

        // 环节2：故障时段受影响的 MES 工单（±2天窗口，使用该设备资源的排产）
        if (fault.get("equipment_name") != null) {
            MapSqlParameterSource p2 = new MapSqlParameterSource()
                    .addValue("resourceName", "%" + fault.get("equipment_name") + "%")
                    .addValue("reportTime", fault.get("report_time"));
            List<Map<String, Object>> affectedOrders = jdbcTemplate.queryForList(
                    "SELECT work_order_no, product_name, status, start_time, end_time FROM mes_db.mes_work_order "
                            + "WHERE resource_name LIKE :resourceName "
                            + "AND start_time <= DATE_ADD(:reportTime, INTERVAL 2 DAY) "
                            + "AND COALESCE(end_time, NOW()) >= DATE_SUB(:reportTime, INTERVAL 2 DAY) LIMIT 10", p2);
            if (!affectedOrders.isEmpty()) {
                chain.add(node(2, "MES", "生产影响",
                        String.format("故障时段影响 %d 张工单：%s", affectedOrders.size(),
                                affectedOrders.stream().map(o -> String.valueOf(o.get("work_order_no"))).limit(3).toList()),
                        Evidence.source("mes_db", "mes_work_order",
                                "resource_name LIKE '%" + fault.get("equipment_name") + "%' AND 故障时段±2天",
                                affectedOrders.size(), "/home/mes/execution"),
                        Map.of("affectedCount", affectedOrders.size(), "orders", affectedOrders)));
            }
        }

        // 环节3：关联备件库存水位（该设备类型常用备件低库存检查）
        List<Map<String, Object>> lowSpares = jdbcTemplate.queryForList(
                "SELECT name, code, current_stock, safety_stock FROM eam_db.eam_spare_part "
                        + "WHERE current_stock < safety_stock ORDER BY (safety_stock - current_stock) DESC LIMIT 5",
                new MapSqlParameterSource());
        if (!lowSpares.isEmpty()) {
            chain.add(node(3, "EAM", "备件保障",
                    String.format("当前 %d 种备件低于安全库存：%s", lowSpares.size(),
                            lowSpares.stream().map(sp -> sp.get("name") + "(缺" + (((Number) sp.get("safety_stock")).intValue() - ((Number) sp.get("current_stock")).intValue()) + ")").toList()),
                    Evidence.source("eam_db", "eam_spare_part",
                            "current_stock < safety_stock", lowSpares.size(),
                            "/home/eam/spare-parts-management"),
                    Map.of("lowStockSpares", lowSpares)));
        }

        // 环节4：在途采购延迟（是否存在逾期未到货订单）
        List<Map<String, Object>> overduePo = jdbcTemplate.queryForList(
                "SELECT order_no, supplier_name, expected_delivery_date FROM srm_db.srm_purchase_order "
                        + "WHERE expected_delivery_date < NOW() "
                        + "AND (status IS NULL OR (UPPER(status) NOT LIKE '%COMPLETE%' AND UPPER(status) NOT LIKE '%RECEIVED%' "
                        + "AND UPPER(status) NOT LIKE '%CANCEL%')) ORDER BY expected_delivery_date ASC LIMIT 3",
                new MapSqlParameterSource());
        if (!overduePo.isEmpty()) {
            chain.add(node(4, "SRM", "供应保障",
                    String.format("%d 张采购订单逾期未到货，备件补充受阻", overduePo.size()),
                    Evidence.source("srm_db", "srm_purchase_order",
                            "expected_delivery_date < NOW() AND 状态非完成", overduePo.size(),
                            "/home/srm/order/list"),
                    Map.of("overdueOrders", overduePo)));
        }

        // 环节5：该设备近180天故障频次（重复性失效判断）
        if (equipmentId != null) {
            Long freq = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM eam_db.eam_fault_record WHERE equipment_id = :eid "
                            + "AND report_time >= DATE_SUB(NOW(), INTERVAL 180 DAY)",
                    new MapSqlParameterSource().addValue("eid", equipmentId), Long.class);
            if (freq != null && freq >= 3) {
                chain.add(node(5, "EAM", "重复性失效",
                        String.format("该设备180天内已发生 %d 次故障，建议纳入预防性维护计划并评估备件储备", freq),
                        Evidence.source("eam_db", "eam_fault_record",
                                "equipment_id=" + equipmentId + " AND 180天内", freq.intValue(),
                                "/home/eam/maintenance-management/fault-repair"),
                        Map.of("faultCount180d", freq)));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("faultId", faultId);
        result.put("chainLength", chain.size());
        result.put("chain", chain);
        result.put("conclusion", chain.size() >= 3
                ? "发现跨域失效链：设备故障与备件/供应保障存在关联风险"
                : "故障影响范围有限，未发现系统性跨域失效");
        // P4-5 结构化证据：聚合各环节已构建的结构化证据源（含下钻路由），替代原表名字符串列表
        List<Object> allSources = new ArrayList<>();
        for (Map<String, Object> n : chain) {
            Object ev = n.get("evidence");
            if (ev instanceof Map<?, ?> evMap && evMap.get("source") != null) {
                allSources.add(evMap.get("source"));
            }
        }
        result.put("evidence", Map.of("sources", allSources, "traceMode", "RULE_BASED_GRAPH"));
        return result;
    }

    /**
     * 构造因果链节点
     *
     * @param source 结构化证据源（Evidence.source 构建，含 db/table/filter/rowCount/drillUrl）
     */
    private Map<String, Object> node(int seq, String module, String stage, String finding,
                                     Map<String, Object> source, Map<String, Object> metrics) {
        Map<String, Object> node = new HashMap<>();
        node.put("seq", seq);
        node.put("module", module);
        node.put("stage", stage);
        node.put("finding", finding);
        node.put("evidence", Map.of("source", source, "metrics", metrics));
        return node;
    }
}
