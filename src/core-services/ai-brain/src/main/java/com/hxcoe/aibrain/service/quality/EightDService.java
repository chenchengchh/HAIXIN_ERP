package com.hxcoe.aibrain.service.quality;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.service.SuggestionService;
import com.hxcoe.aibrain.service.llm.LlmService;
import com.hxcoe.aibrain.service.llm.PromptTemplateService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * 质量 8D 报告骨架自动生成（S09，P4-7 LLM 增强版）
 * 从不合格品登记表自动填充 D1-D8 骨架：问题描述、临时对策（历史同类）、根因候选（关联批次/供应商）
 * 人从"写报告"变为"审报告"（CONFIRM 级，人审后生效）
 * P4-7 升级：D4 根因假设与 D5 永久对策草稿由 LLM 基于缺陷语义生成（本地 Ollama，数据不出域），
 * LLM 不可用/输出解析失败时静默回退规则模板版（保证功能永远可用）。
 */
@Slf4j
@Service
public class EightDService {

    /** LLM 输出中的 JSON 对象提取（容忍 ```json 包裹与前后多余文字） */
    private static final Pattern JSON_BLOCK = Pattern.compile("\\{[\\s\\S]*\\}");

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LlmService llmService;

    @Autowired
    private PromptTemplateService promptTemplateService;

    /**
     * 为指定不合格品登记单生成 8D 骨架
     *
     * @param ncId 不合格品登记ID
     * @return 8D 骨架（D1-D8）
     */
    public Map<String, Object> generate(Long ncId) {
        MapSqlParameterSource p = new MapSqlParameterSource().addValue("ncId", ncId);
        List<Map<String, Object>> ncs = jdbcTemplate.queryForList(
                "SELECT * FROM qms_db.qms_nc_registration WHERE id = :ncId", p);
        if (ncs.isEmpty()) {
            return Map.of("success", false, "message", "不合格品登记不存在: " + ncId);
        }
        Map<String, Object> nc = ncs.get(0);

        // D3 临时对策候选：同缺陷类型的历史处置记录
        List<Map<String, Object>> similarDisposals = jdbcTemplate.queryForList(
                "SELECT d.registration_no, d.disposal_description, d.process_result FROM qms_db.qms_nc_disposal d "
                        + "WHERE d.registration_id IN (SELECT id FROM qms_db.qms_nc_registration WHERE defect_type = :dtype AND id <> :ncId2) "
                        + "AND d.process_result IS NOT NULL ORDER BY d.id DESC LIMIT 3",
                new MapSqlParameterSource()
                        .addValue("dtype", nc.get("defect_type"))
                        .addValue("ncId2", ncId));

        // D4 根因候选：同物料近90天不合格频次（批次集中度判断来料问题）
        Long sameMaterialCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM qms_db.qms_nc_registration WHERE material_code = :mcode "
                        + "AND registration_time >= DATE_SUB(NOW(), INTERVAL 90 DAY)",
                new MapSqlParameterSource().addValue("mcode", nc.get("material_code")), Long.class);
        sameMaterialCount = sameMaterialCount == null ? 0 : sameMaterialCount;

        Map<String, Object> eightD = new HashMap<>();
        eightD.put("D1_团队", Map.of("建议负责人", nc.getOrDefault("discovery_department", "质量部") + "主管",
                "建议成员", List.of("质量工程师", "生产班组长", "工艺工程师")));
        eightD.put("D2_问题描述", String.format("物料【%s】(%s) 发现【%s】缺陷 %s 件，缺陷等级：%s。%s",
                nc.get("material_name"), nc.get("material_code"), nc.get("defect_type"),
                nc.get("quantity"), nc.getOrDefault("defect_level", "未定"),
                nc.getOrDefault("defect_description", "")));
        eightD.put("D3_临时对策", similarDisposals.isEmpty()
                ? List.of("隔离不合格批次", "暂停该批次上线使用", "全检在库同物料")
                : similarDisposals.stream().map(d -> "参考 " + d.get("registration_no") + "：" + d.get("process_result")).toList());
        eightD.put("D4_根因候选", sameMaterialCount >= 3
                ? List.of("该物料90天内不合格 " + sameMaterialCount + " 次，疑似供应商来料质量问题", "建议核查供应商批次工艺变更")
                : List.of("单次事件，建议排查：来料批次差异/运输存储条件/上线工艺参数"));
        eightD.put("D5_永久对策", "待 D4 根因确认后填写");

        // P4-7 LLM 增强：基于缺陷语义生成 D4 根因假设与 D5 永久对策草稿（失败静默保留规则版）
        boolean llmEnhanced = tryEnhanceWithLlm(eightD, nc, similarDisposals, sameMaterialCount);

        eightD.put("D6_效果验证", "对策实施后连续3批检验合格判定有效");
        eightD.put("D7_标准化", "更新检验标准/作业指导书并培训");
        eightD.put("D8_关闭总结", "团队确认后关闭");

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("ncId", ncId);
        result.put("registrationNo", nc.get("registration_no"));
        result.put("eightD", eightD);
        result.put("evidence", Map.of("sources",
                List.of("qms_db.qms_nc_registration", "qms_db.qms_nc_disposal"),
                "metrics", Map.of("sameMaterialNcCount90d", sameMaterialCount,
                        "llmEnhanced", llmEnhanced,
                        "llmProvider", llmService.provider())));
        return result;
    }

    /**
     * LLM 语义增强 D4/D5：把不合格品信息与历史处置记录交给本地模型生成根因假设与对策草稿
     * 降级策略：LLM 不可用、输出非 JSON、JSON 缺 d4/d5 数组均视为失败，保留规则版内容（返回 false）
     *
     * @param eightD            8D 骨架（成功时原地替换 D4_根因候选/D5_永久对策）
     * @param nc                不合格品登记行
     * @param similarDisposals  同缺陷类型历史处置记录
     * @param sameMaterialCount 同物料90天不合格频次
     * @return true=LLM 增强已生效；false=保留规则版
     */
    private boolean tryEnhanceWithLlm(Map<String, Object> eightD, Map<String, Object> nc,
                                      List<Map<String, Object>> similarDisposals, long sameMaterialCount) {
        String systemPrompt = promptTemplateService.getTemplate("EIGHT_D_ROOTCAUSE");
        if (systemPrompt == null) {
            return false;
        }
        try {
            StringBuilder user = new StringBuilder();
            user.append("【不合格品信息】\n");
            user.append("物料：").append(nc.get("material_name")).append("(").append(nc.get("material_code")).append(")\n");
            user.append("缺陷类型：").append(nc.get("defect_type")).append("，缺陷等级：")
                    .append(nc.getOrDefault("defect_level", "未定")).append("，数量：").append(nc.get("quantity")).append("\n");
            user.append("缺陷描述：").append(nc.getOrDefault("defect_description", "无")).append("\n");
            user.append("同物料90天内不合格次数：").append(sameMaterialCount).append("\n");
            if (!similarDisposals.isEmpty()) {
                user.append("【历史同类处置记录】\n");
                for (Map<String, Object> d : similarDisposals) {
                    user.append("- ").append(d.get("registration_no")).append("：").append(d.get("process_result")).append("\n");
                }
            }
            String llmOut = llmService.chatForSkill("EIGHT_D_ROOTCAUSE", systemPrompt, user.toString());
            if (llmOut == null) {
                return false;
            }
            Matcher m = JSON_BLOCK.matcher(llmOut);
            if (!m.find()) {
                log.warn("8D LLM 输出未含 JSON，保留规则版: {}", abbreviate(llmOut));
                return false;
            }
            JsonNode root = objectMapper.readTree(m.group());
            List<String> d4 = extractStringList(root.path("d4"));
            List<String> d5 = extractStringList(root.path("d5"));
            if (d4.isEmpty() && d5.isEmpty()) {
                return false;
            }
            if (!d4.isEmpty()) {
                eightD.put("D4_根因候选", d4);
            }
            if (!d5.isEmpty()) {
                eightD.put("D5_永久对策", d5);
            }
            return true;
        } catch (Exception e) {
            log.warn("8D LLM 增强失败（保留规则版）: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从 JSON 节点提取字符串数组（限3条，过滤空串与非文本节点）
     */
    private List<String> extractStringList(JsonNode node) {
        List<String> items = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                if (item.isTextual() && !item.asText().isBlank()) {
                    items.add(item.asText().trim());
                }
                if (items.size() >= 3) {
                    break;
                }
            }
        }
        return items;
    }

    /**
     * 日志截断辅助（LLM 原始输出可能较长）
     */
    private String abbreviate(String text) {
        return text == null ? "null" : (text.length() <= 120 ? text : text.substring(0, 120) + "…");
    }

    /**
     * 生成 8D 骨架并落建议（供决策中心确认后回填 QMS）
     */
    public AiSuggestionEntity generateAndPublish(Long ncId) throws Exception {
        Map<String, Object> draft = generate(ncId);
        if (!Boolean.TRUE.equals(draft.get("success"))) {
            return null;
        }
        AiSuggestionEntity s = new AiSuggestionEntity();
        s.setSuggestionType("EIGHT_D");
        s.setModule("QMS");
        s.setSeverity("INFO");
        s.setTitle("8D报告骨架已生成：" + draft.get("registrationNo"));
        s.setAutomationLevel("CONFIRM");
        s.setEventId("EIGHT-D-" + ncId);
        s.setAnalysis(objectMapper.writeValueAsString(draft));
        Map<String, Object> action = new HashMap<>();
        action.put("type", "FILL_NC_DISPOSAL");
        action.put("endpoint", "/api/v1/qms/nc-disposal");
        action.put("method", "POST");
        action.put("description", "将8D骨架回填至不合格品处置单（人工确认）");
        s.setActionDraft(objectMapper.writeValueAsString(action));
        suggestionService.publish(s);
        // 幂等回查：无论本次新增还是已存在（含已处置），都返回库中持久化实体，避免重复生成与瞬时对象无ID
        AiSuggestionEntity persisted = suggestionService.getByEventId(s.getEventId());
        return persisted != null ? persisted : s;
    }
}
