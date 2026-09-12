package com.hxcoe.aibrain.service.expert;

import com.hxcoe.aibrain.service.ontology.OntologyQueryService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 库存领域专家（P4-6 Specialist 工具化）
 * 封装备件库存评估能力：低库存备件清单、缺口计算、补货动作草稿
 * 数据访问一律走本体层（spare_part 对象，含 trusted 闸门）
 */
@Slf4j
@Component
public class InventoryExpert {

    @Autowired
    private OntologyQueryService ontology;

    /**
     * 评估当前备件库存水位（低库存备件 = current_stock &lt; safety_stock）
     *
     * @return 库存领域发现（含缺口最大备件的补货动作草稿）
     */
    public ExpertFinding assessLowSpares() {
        OntologyQueryService.OntologyQuery query = ontology.findObjects("spare_part");
        if (query.blocked()) {
            return ExpertFinding.unknown("INVENTORY", query.blockReason());
        }
        List<Map<String, Object>> all = query.list();
        List<Map<String, Object>> low = all.stream()
                .filter(sp -> intOf(sp.get("current_stock")) < intOf(sp.get("safety_stock")))
                .sorted((a, b) -> gap(b) - gap(a))
                .toList();

        ExpertFinding finding = new ExpertFinding();
        finding.setDomain("INVENTORY");
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalSpares", all.size());
        metrics.put("lowStockCount", low.size());
        metrics.put("lowStockSamples", low.stream().limit(3).map(sp -> Map.of(
                "name", String.valueOf(sp.get("name")),
                "code", String.valueOf(sp.get("code")),
                "gap", gap(sp))).toList());
        finding.setMetrics(metrics);

        if (low.isEmpty()) {
            finding.setStatus("OK");
            finding.setSummary("备件库存水位正常，无低于安全库存项");
            return finding;
        }

        Map<String, Object> worst = low.get(0);
        finding.setStatus(low.size() >= 3 ? "CRITICAL" : "WARNING");
        finding.setSummary(String.format("%d 种备件低于安全库存，缺口最大：%s（缺 %d）",
                low.size(), worst.get("name"), gap(worst)));

        // 补充动作草稿：为缺口最大备件生成 SRM 采购申请（契约同 P4-1 action_draft）
        // payload 字段严格对齐 PurchaseRequestEntity（urgency/remark 等非实体字段会被 Jackson 静默丢弃，禁止使用）
        Map<String, Object> action = new HashMap<>();
        action.put("type", "CREATE_PURCHASE_REQUEST");
        action.put("endpoint", "/api/v1/srm/purchase-requests");
        action.put("method", "POST");
        Map<String, Object> payload = new HashMap<>();
        payload.put("requestCode", "PR-AI-ORCH-" + worst.get("code"));
        payload.put("materialCode", worst.get("code"));
        payload.put("materialName", worst.get("name"));
        payload.put("quantity", Math.max(gap(worst), 1));
        payload.put("unit", worst.get("unit"));
        payload.put("status", "PENDING");
        payload.put("applicant", "AI决策引擎");
        payload.put("purchaseType", "紧急采购");
        payload.put("expectedDeliveryDate", java.time.LocalDateTime.now().plusDays(7)
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        payload.put("description", "AI多专家会诊联动补货（P4-6）：当前库存 " + intOf(worst.get("current_stock"))
                + "，安全库存 " + intOf(worst.get("safety_stock")) + "，缺口 " + gap(worst));
        action.put("payload", payload);
        action.put("description", "为缺口最大备件 " + worst.get("name") + " 创建紧急采购申请");
        action.put("confirmText", "确认为 " + worst.get("name") + " 创建紧急采购申请（数量 " + Math.max(gap(worst), 1) + "）？");
        action.put("rollbackHint", "采购申请创建后可在 SRM 采购申请列表撤销");
        finding.setActionDraft(action);
        return finding;
    }

    private int gap(Map<String, Object> sp) {
        return intOf(sp.get("safety_stock")) - intOf(sp.get("current_stock"));
    }

    private int intOf(Object v) {
        return v instanceof Number n ? n.intValue() : 0;
    }
}
