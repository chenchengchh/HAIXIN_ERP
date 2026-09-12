package com.hxcoe.aibrain.service.vision;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.aibrain.service.llm.LlmService;
import com.hxcoe.aibrain.service.llm.PromptTemplateService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * LLM 增强版视觉理解实现（S11，P4-7）
 * 策略：规则优先，LLM 补缺与校验——
 * 1. 先走纯正则规则版（RuleBasedVisionService）提取字段；
 * 2. 规则未命中的字段交给本地 Ollama 模型从 OCR 文本兜底解析（变体版式/非常规标签）；
 * 3. 规则已命中的字段与 LLM 结果交叉校验，不一致时记入 evidence.discrepancies 供人工复核（不擅自覆盖规则结果）；
 * 4. LLM 不可用/输出解析失败时静默返回规则版结果（降级铁律，功能永远可用）。
 */
@Slf4j
@Primary
@Service
public class LlmEnhancedVisionService implements VisionService {

    /** LLM 输出中的 JSON 对象提取（容忍 ```json 包裹与前后多余文字） */
    private static final Pattern JSON_BLOCK = Pattern.compile("\\{[\\s\\S]*\\}");

    /** 发票关键字段清单 */
    private static final List<String> INVOICE_FIELDS =
            List.of("invoiceNo", "invoiceDate", "totalAmount", "supplierName");

    /** 仪表关键字段清单 */
    private static final List<String> GAUGE_FIELDS = List.of("reading", "unit", "tagCode");

    @Autowired
    private RuleBasedVisionService ruleBasedVisionService;

    @Autowired
    private LlmService llmService;

    @Autowired
    private PromptTemplateService promptTemplateService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 规则提取 + LLM 兜底补缺与交叉校验
     *
     * @param docType 文档类型（invoice/gauge）
     * @param ocrText OCR 识别纯文本
     * @param hint    辅助提示（如仪表点位编码，可为空）
     * @return 结构化提取结果（规则版结果之上合并 LLM 补缺字段）
     */
    @Override
    public VisionParseResult parseDocument(String docType, String ocrText, String hint) {
        // 1. 规则版先行（永远可用、零依赖）
        VisionParseResult result = ruleBasedVisionService.parseDocument(docType, ocrText, hint);
        if (ocrText == null || ocrText.isBlank()
                || (!DOC_TYPE_INVOICE.equals(docType) && !DOC_TYPE_GAUGE.equals(docType))) {
            // 空文本/未知类型直接返回规则版响应，不触发 LLM
            return result;
        }

        List<String> expectedFields = DOC_TYPE_INVOICE.equals(docType) ? INVOICE_FIELDS : GAUGE_FIELDS;
        Map<String, Object> fields = result.getFields() == null ? new HashMap<>() : result.getFields();

        // 2. LLM 兜底解析（模板未注册/LLM 不可用/解析失败均静默返回规则版）
        String skillCode = DOC_TYPE_INVOICE.equals(docType) ? "VISION_INVOICE_EXTRACT" : "VISION_GAUGE_EXTRACT";
        Map<String, Object> llmFields = extractByLlm(skillCode, docType, ocrText, hint);

        Map<String, Object> evidence = result.getEvidence() == null ? new HashMap<>() : result.getEvidence();
        if (llmFields == null) {
            evidence.put("provider", "rule");
            evidence.put("llmUsed", false);
            result.setEvidence(evidence);
            return result;
        }

        // 3. 补缺：LLM 仅补规则未命中的字段（规则优先，不覆盖）
        int filled = 0;
        for (String field : expectedFields) {
            if (!fields.containsKey(field) && llmFields.containsKey(field)) {
                fields.put(field, llmFields.get(field));
                filled++;
            }
        }

        // 4. 交叉校验：双方都有值但不一致的字段记入 evidence.discrepancies（人工复核线索，不覆盖规则值）
        Map<String, String> discrepancies = new HashMap<>();
        for (String field : expectedFields) {
            if (fields.containsKey(field) && llmFields.containsKey(field)) {
                String ruleVal = String.valueOf(fields.get(field));
                String llmVal = String.valueOf(llmFields.get(field));
                if (!ruleVal.equals(llmVal)) {
                    discrepancies.put(field, "rule=" + ruleVal + " | llm=" + llmVal);
                }
            }
        }
        if (!discrepancies.isEmpty()) {
            evidence.put("discrepancies", discrepancies);
        }

        // 5. 重算置信度与消息（补缺后命中率提升）
        int hit = 0;
        for (String field : expectedFields) {
            if (fields.containsKey(field)) {
                hit++;
            }
        }
        result.setFields(fields);
        result.setConfidence(hit / (double) expectedFields.size());
        boolean success = DOC_TYPE_INVOICE.equals(docType) ? hit > 0 : fields.containsKey("reading");
        result.setSuccess(success);
        if (filled > 0) {
            result.setMessage(result.getMessage() + "；其中 " + filled + " 个字段由 LLM 辅助识别");
        }

        evidence.put("provider", "rule+llm");
        evidence.put("llmUsed", true);
        evidence.put("llmFilledFields", filled);
        result.setEvidence(evidence);
        return result;
    }

    @Override
    public String provider() {
        return "rule+llm";
    }

    /**
     * LLM 兜底结构化提取：从 OCR 文本解析业务字段并做类型归一化
     * 失败场景（模板缺失/LLM 返回 null/输出无 JSON/JSON 解析异常）一律返回 null，调用方静默降级
     *
     * @param skillCode 提示词模板技能编码
     * @param docType   文档类型（决定字段类型归一化规则）
     * @param ocrText   OCR 纯文本
     * @param hint      辅助提示（仪表点位编码）
     * @return LLM 提取的字段 Map（失败返回 null）
     */
    private Map<String, Object> extractByLlm(String skillCode, String docType, String ocrText, String hint) {
        String systemPrompt = promptTemplateService.getTemplate(skillCode);
        if (systemPrompt == null) {
            return null;
        }
        try {
            String userPrompt = (hint != null && !hint.isBlank() ? "点位提示：" + hint + "\n" : "")
                    + "OCR文本：\n" + ocrText;
            String llmOut = llmService.chatForSkill(skillCode, systemPrompt, userPrompt);
            if (llmOut == null) {
                return null;
            }
            Matcher m = JSON_BLOCK.matcher(llmOut);
            if (!m.find()) {
                log.warn("视觉 LLM 输出未含 JSON，按规则版返回: {}", abbreviate(llmOut));
                return null;
            }
            JsonNode root = objectMapper.readTree(m.group());
            Map<String, Object> fields = new HashMap<>();
            root.fields().forEachRemaining(e -> {
                Object normalized = normalizeValue(docType, e.getKey(), e.getValue());
                if (normalized != null) {
                    fields.put(e.getKey(), normalized);
                }
            });
            return fields.isEmpty() ? null : fields;
        } catch (Exception e) {
            log.warn("视觉 LLM 兜底解析失败（按规则版返回）: {}", e.getMessage());
            return null;
        }
    }

    /** LLM 对无法识别字段的常见占位输出（视为未命中，不入字段表） */
    private static final java.util.Set<String> PLACEHOLDER_VALUES =
            java.util.Set.of("无", "未知", "无法识别", "未识别", "n/a", "na", "null", "none", "-", "--", "");

    /**
     * 字段值类型归一化：金额/读数转数字，日期/名称/单位/编码转字符串（空白与占位文本丢弃）
     */
    private Object normalizeValue(String docType, String field, JsonNode value) {
        if (value == null || value.isNull()) {
            return null;
        }
        try {
            // 数字型字段：LLM 输出数字或数字字符串均统一转数值类型（保证前端预填类型稳定）
            if (DOC_TYPE_INVOICE.equals(docType) && "totalAmount".equals(field)) {
                String numText = value.asText().replace(",", "").replaceAll("[^0-9.\\-]", "");
                return numText.isBlank() ? null : new BigDecimal(numText);
            }
            if (DOC_TYPE_GAUGE.equals(docType) && "reading".equals(field)) {
                String numText = value.asText().replaceAll("[^0-9.\\-]", "");
                return numText.isBlank() ? null : Double.parseDouble(numText);
            }
            String text = value.asText();
            if (text == null || text.isBlank()) {
                return null;
            }
            String trimmed = text.trim();
            // 占位文本视为未命中（LLM 未按"省略该键"指令执行时的兜底清洗）
            return PLACEHOLDER_VALUES.contains(trimmed.toLowerCase()) ? null : trimmed;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 日志截断辅助（LLM 原始输出可能较长）
     */
    private String abbreviate(String text) {
        return text == null ? "null" : (text.length() <= 120 ? text : text.substring(0, 120) + "…");
    }
}
