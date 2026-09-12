package com.hxcoe.aibrain.service.preaudit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.service.SuggestionService;
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
 * AI 审批预审员（S07）
 * 审批任务到达时先行核查：供应商资质（SRM）、金额合理性（同类历史对比）、表单完整性
 * 预审意见仅供审批人参考（AUTO 级，审批权始终在人），不落业务表。
 */
@Slf4j
@Service
public class ApprovalPreauditService {

    /** 大额审批提示阈值（元） */
    private static final double LARGE_AMOUNT_THRESHOLD = 100000.0;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 扫描所有待审批任务并生成预审意见（幂等：eventId=PREAUDIT-实例ID）
     *
     * @return 新增预审意见数
     */
    public int scanPendingTasks() {
        // 注意：统一审批实例落库 oa_approval_process_instance（旧表 oa_approval_instance 已废弃为空表），
        // 误 JOIN 旧表会导致扫描结果恒为空、预审意见永不生成
        List<Map<String, Object>> tasks = jdbcTemplate.queryForList(
                "SELECT t.id AS task_id, t.instance_id, t.node_name, t.assignee_name, "
                        + "i.process_code, i.title, i.initiator_name, i.form_data "
                        + "FROM oa_db.oa_approval_task t "
                        + "JOIN oa_db.oa_approval_process_instance i ON i.id = t.instance_id "
                        + "WHERE t.status = 'pending'",
                new MapSqlParameterSource());
        int created = 0;
        for (Map<String, Object> task : tasks) {
            try {
                AiSuggestionEntity opinion = preaudit(task);
                if (opinion != null && suggestionService.publish(opinion)) {
                    created++;
                }
            } catch (Exception e) {
                log.warn("预审实例{}失败: {}", task.get("instance_id"), e.getMessage());
            }
        }
        log.info("审批预审: 扫描待办{}项, 新增预审意见{}条", tasks.size(), created);
        return created;
    }

    /**
     * 对单个审批实例执行预审核查
     */
    private AiSuggestionEntity preaudit(Map<String, Object> task) throws Exception {
        Long instanceId = ((Number) task.get("instance_id")).longValue();
        String processCode = String.valueOf(task.get("process_code"));
        String formDataRaw = task.get("form_data") == null ? "{}" : String.valueOf(task.get("form_data"));
        Map<String, Object> formData = objectMapper.readValue(formDataRaw, Map.class);

        List<String> findings = new ArrayList<>();
        List<String> risks = new ArrayList<>();
        Map<String, Object> metrics = new HashMap<>();
        List<String> sources = new ArrayList<>(List.of("oa_db.oa_approval_task", "oa_db.oa_approval_process_instance"));

        // 1. 供应商资质核查（采购/供应商相关流程）
        if (processCode.toUpperCase().contains("PURCHASE") || processCode.toUpperCase().contains("SUPPLIER")
                || processCode.contains("采购") || processCode.contains("供应商")) {
            checkSupplierQualification(formData, findings, risks, metrics);
            sources.add("srm_db.srm_supplier");
            sources.add("srm_db.srm_supplier_qualification");
        }

        // 2. 金额合理性核查（提取表单金额字段）
        checkAmount(formData, findings, risks, metrics);

        // 3. 表单完整性核查（关键字段空值）
        checkFormCompleteness(formData, findings, metrics);

        // 无发现不发声
        if (findings.isEmpty() && risks.isEmpty()) {
            return null;
        }

        AiSuggestionEntity s = new AiSuggestionEntity();
        s.setSuggestionType("PREAUDIT");
        s.setModule("OA");
        s.setSeverity(risks.isEmpty() ? "INFO" : "WARNING");
        s.setTitle(String.format("预审意见【%s】：%s", task.get("title"),
                risks.isEmpty() ? "核查通过，附提示 " + findings.size() + " 条" : "发现存疑项 " + risks.size() + " 条"));
        s.setAutomationLevel("AUTO");
        s.setEventId("PREAUDIT-" + instanceId);

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("instanceId", instanceId);
        analysis.put("taskId", task.get("task_id"));
        analysis.put("nodeName", task.get("node_name"));
        analysis.put("assigneeName", task.get("assignee_name"));
        analysis.put("initiatorName", task.get("initiator_name"));
        analysis.put("risks", risks);
        analysis.put("findings", findings);
        analysis.put("conclusion", risks.isEmpty() ? "PASS_WITH_NOTES" : "REVIEW_RECOMMENDED");
        analysis.put("evidence", Map.of("sources", sources, "metrics", metrics));
        s.setAnalysis(objectMapper.writeValueAsString(analysis));
        return s;
    }

    /**
     * 供应商资质核查：状态、评分、资质有效期
     */
    private void checkSupplierQualification(Map<String, Object> formData,
                                            List<String> findings, List<String> risks,
                                            Map<String, Object> metrics) {
        Object supplierId = formData.get("supplierId");
        Object supplierName = formData.getOrDefault("supplierName", formData.get("supplier"));
        if (supplierId == null && supplierName == null) {
            return;
        }
        MapSqlParameterSource p = new MapSqlParameterSource();
        String condition;
        if (supplierId != null) {
            condition = "id = :sid";
            p.addValue("sid", Long.parseLong(String.valueOf(supplierId)));
        } else {
            condition = "supplier_name = :sname";
            p.addValue("sname", String.valueOf(supplierName));
        }
        List<Map<String, Object>> suppliers = jdbcTemplate.queryForList(
                "SELECT id, supplier_name, status, rating FROM srm_db.srm_supplier WHERE " + condition, p);
        if (suppliers.isEmpty()) {
            risks.add("供应商在SRM库中不存在，资质未建档");
            return;
        }
        Map<String, Object> supplier = suppliers.get(0);
        metrics.put("supplierName", supplier.get("supplier_name"));
        metrics.put("supplierRating", supplier.get("rating"));
        String status = supplier.get("status") == null ? "" : String.valueOf(supplier.get("status"));
        if (!status.isEmpty() && !"ACTIVE".equalsIgnoreCase(status) && !"合格".equals(status) && !"1".equals(status)) {
            risks.add(String.format("供应商【%s】SRM状态为 %s，非正常合作状态", supplier.get("supplier_name"), status));
        }
        Double rating = supplier.get("rating") instanceof Number ? ((Number) supplier.get("rating")).doubleValue() : null;
        if (rating != null && rating < 3.0) {
            risks.add(String.format("供应商【%s】评分 %.1f 低于3.0，历史表现不佳", supplier.get("supplier_name"), rating));
        } else if (rating != null) {
            findings.add(String.format("供应商【%s】评分 %.1f，资质正常", supplier.get("supplier_name"), rating));
        }
    }

    /**
     * 金额核查：大额提示（提取 amount/totalAmount/price 类字段）
     */
    private void checkAmount(Map<String, Object> formData,
                             List<String> findings, List<String> risks,
                             Map<String, Object> metrics) {
        for (String key : List.of("totalAmount", "amount", "finalAmount", "price")) {
            Object v = formData.get(key);
            if (v instanceof Number) {
                double amount = ((Number) v).doubleValue();
                metrics.put("amount", amount);
                if (amount >= LARGE_AMOUNT_THRESHOLD) {
                    risks.add(String.format("审批金额 %.2f 元超过大额阈值 %.0f 元，建议核对预算占用", amount, LARGE_AMOUNT_THRESHOLD));
                } else {
                    findings.add(String.format("审批金额 %.2f 元，金额区间正常", amount));
                }
                return;
            }
        }
    }

    /**
     * 表单完整性：空值关键字段提示
     */
    private void checkFormCompleteness(Map<String, Object> formData,
                                       List<String> findings, Map<String, Object> metrics) {
        List<String> emptyKeys = new ArrayList<>();
        for (Map.Entry<String, Object> e : formData.entrySet()) {
            if (e.getValue() == null || (e.getValue() instanceof String && ((String) e.getValue()).isBlank())) {
                emptyKeys.add(e.getKey());
            }
        }
        if (!emptyKeys.isEmpty() && emptyKeys.size() <= 5) {
            findings.add("表单存在空值字段：" + String.join("、", emptyKeys));
            metrics.put("emptyFields", emptyKeys);
        }
    }

    /**
     * 查询指定审批实例的预审意见（供 OA 审批详情页调用）
     *
     * @param instanceId 审批实例ID
     * @return 预审意见（无则 null）
     */
    public AiSuggestionEntity getByInstanceId(Long instanceId) {
        return suggestionService.getByEventId("PREAUDIT-" + instanceId);
    }
}
