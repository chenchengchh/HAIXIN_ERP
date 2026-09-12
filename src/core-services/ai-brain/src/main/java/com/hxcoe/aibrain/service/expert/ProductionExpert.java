package com.hxcoe.aibrain.service.expert;

import com.hxcoe.aibrain.service.ontology.OntologyQueryService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 生产领域专家（P4-6 Specialist 工具化）
 * 封装生产影响评估能力：指定设备资源的在产工单（故障/停机的生产影响面）
 * 数据访问走本体层（work_order 对象）
 */
@Slf4j
@Component
public class ProductionExpert {

    @Autowired
    private OntologyQueryService ontology;

    /**
     * 评估指定设备资源的在产工单影响面
     *
     * @param resourceName 设备/资源名称（模糊匹配 resource_name）
     * @return 生产领域发现
     */
    public ExpertFinding assessAffectedOrders(String resourceName) {
        OntologyQueryService.OntologyQuery query = ontology.findObjects("work_order");
        if (query.blocked()) {
            return ExpertFinding.unknown("PRODUCTION", query.blockReason());
        }
        if (resourceName == null || resourceName.isBlank()) {
            ExpertFinding f = new ExpertFinding("PRODUCTION", "UNKNOWN", "未提供设备资源名，跳过生产影响评估");
            return f;
        }
        List<Map<String, Object>> active = query
                .whereLike("resourceName", resourceName)
                .limit(20)
                .list().stream()
                .filter(o -> {
                    String st = String.valueOf(o.get("status"));
                    return st == null || "null".equals(st)
                            || !(st.toUpperCase().contains("COMPLETE") || st.toUpperCase().contains("DONE") || st.contains("完成"));
                })
                .toList();

        ExpertFinding finding = new ExpertFinding();
        finding.setDomain("PRODUCTION");
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("affectedCount", active.size());
        metrics.put("orderNos", active.stream().limit(5).map(o -> String.valueOf(o.get("work_order_no"))).toList());
        finding.setMetrics(metrics);

        if (active.isEmpty()) {
            finding.setStatus("OK");
            finding.setSummary("该设备当前无在产工单，停机维护无生产影响");
        } else {
            finding.setStatus("WARNING");
            finding.setSummary(String.format("该设备关联 %d 张在产工单（%s），维护窗口需与排产协调",
                    active.size(), active.get(0).get("work_order_no")));
        }
        return finding;
    }
}
