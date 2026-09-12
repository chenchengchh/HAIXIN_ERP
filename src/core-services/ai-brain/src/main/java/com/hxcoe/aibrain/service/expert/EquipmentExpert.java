package com.hxcoe.aibrain.service.expert;

import com.hxcoe.aibrain.service.ontology.OntologyQueryService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 设备领域专家（P4-6 Specialist 工具化）
 * 封装设备健康评估能力：180 天故障频次（重复性失效判断）
 * 数据访问走本体层（fault_record 对象）
 */
@Slf4j
@Component
public class EquipmentExpert {

    @Autowired
    private OntologyQueryService ontology;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * 评估设备近 180 天故障频次（重复性失效风险）
     *
     * @param equipmentId   设备ID（可空，空时按名称模糊匹配）
     * @param equipmentName 设备名称
     * @return 设备领域发现
     */
    public ExpertFinding assessFaultHistory(Object equipmentId, String equipmentName) {
        OntologyQueryService.OntologyQuery query = ontology.findObjects("fault_record");
        if (query.blocked()) {
            return ExpertFinding.unknown("EQUIPMENT", query.blockReason());
        }
        // 180 天窗口计数（时间条件走注册表表名的条件 SQL，表名来自本体）
        String table = ontology.sourceTableOf("fault_record");
        MapSqlParameterSource p = new MapSqlParameterSource();
        String where;
        if (equipmentId != null) {
            where = "equipment_id = :eid";
            p.addValue("eid", equipmentId);
        } else if (equipmentName != null && !equipmentName.isBlank()) {
            where = "equipment_name LIKE :ename";
            p.addValue("ename", "%" + equipmentName + "%");
        } else {
            return new ExpertFinding("EQUIPMENT", "UNKNOWN", "未提供设备标识，跳过故障史评估");
        }
        Long freq = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table + " WHERE " + where
                        + " AND report_time >= DATE_SUB(NOW(), INTERVAL 180 DAY)",
                p, Long.class);
        long count = freq == null ? 0L : freq;

        ExpertFinding finding = new ExpertFinding();
        finding.setDomain("EQUIPMENT");
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("faultCount180d", count);
        finding.setMetrics(metrics);

        if (count >= 3) {
            finding.setStatus("CRITICAL");
            finding.setSummary(String.format("该设备 180 天内已发生 %d 次故障，属重复性失效，建议纳入预防性维护计划", count));
        } else if (count > 0) {
            finding.setStatus("WARNING");
            finding.setSummary(String.format("该设备 180 天内有 %d 次故障记录", count));
        } else {
            finding.setStatus("OK");
            finding.setSummary("该设备 180 天内无故障记录");
        }
        return finding;
    }
}
