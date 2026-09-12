package com.hxcoe.aibrain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.repository.AiSuggestionRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * AI 动作执行引擎（P4-1 核心）
 * 职责：CONFIRM 级建议"一键执行"——解析 actionDraft，经网关调用目标模块现有业务端点，
 * 执行结果回写建议状态（成功→EXECUTED 附结果单号；失败→保持 PENDING 附原因，允许重试）
 * 铁律：一律调用现有业务端点，绝不直接 UPDATE 业务表
 */
@Slf4j
@Service
public class ActionExecutorService {

    /** 网关地址（docker 网络内服务名），动作统一经网关路由到目标服务，复用网关鉴权与路由规则 */
    @Value("${ai.action.gateway-url:http://gateway:9000}")
    private String gatewayUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AiSuggestionRepository suggestionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 执行建议关联的主动作草稿（兼容入口，等价于 execute(suggestionId, null)）
     *
     * @param suggestionId 建议ID
     * @return {success, message, data} 执行结果
     */
    public Map<String, Object> execute(Long suggestionId) {
        return execute(suggestionId, null);
    }

    /**
     * 执行建议关联的动作草稿
     * actionIndex 为空时执行主动作（action_draft 字段）；非空时执行 analysis.secondaryActions 中
     * 对应下标的次要动作（P4-6 多专家联动补充动作，如紧急采购申请）。
     * 次要动作执行成功不改变建议状态（保持 PENDING，主动作仍可执行），仅回写 feedback 留痕。
     *
     * @param suggestionId 建议ID
     * @param actionIndex  次要动作下标（可空，空=主动作）
     * @return {success, message, data} 执行结果
     */
    public Map<String, Object> execute(Long suggestionId, Integer actionIndex) {
        AiSuggestionEntity suggestion = suggestionRepository.findById(suggestionId)
                .orElseThrow(() -> new RuntimeException("建议不存在: " + suggestionId));

        // 1. 状态与级别校验：仅 PENDING + CONFIRM 级可执行（幂等防护，已处置建议拒绝重复执行）
        if (!"PENDING".equals(suggestion.getStatus())) {
            throw new RuntimeException("仅待处理建议可执行，当前状态: " + suggestion.getStatus());
        }
        if (!"CONFIRM".equals(suggestion.getAutomationLevel())) {
            throw new RuntimeException("仅 CONFIRM 级建议支持执行，当前级别: " + suggestion.getAutomationLevel());
        }

        // 2. 解析动作草稿契约（主动作取 action_draft；次要动作取 analysis.secondaryActions[actionIndex]）
        Map<String, Object> draft;
        String actionLabel;
        if (actionIndex == null) {
            if (suggestion.getActionDraft() == null || suggestion.getActionDraft().isBlank()) {
                throw new RuntimeException("该建议无动作草稿，不支持执行");
            }
            draft = parseDraft(suggestion.getActionDraft());
            actionLabel = "";
        } else {
            draft = resolveSecondaryAction(suggestion, actionIndex);
            actionLabel = "次要动作[" + draft.getOrDefault("domain", actionIndex) + "]";
        }
        String endpoint = draft.get("endpoint") == null ? null : String.valueOf(draft.get("endpoint"));
        String method = String.valueOf(draft.getOrDefault("method", "POST")).toUpperCase();
        Object payload = draft.get("payload");

        // 端点白名单：仅允许调用本系统 /api/v1/ 前缀的现有业务端点（防任意 URL 调用）
        if (endpoint == null || !endpoint.startsWith("/api/v1/")) {
            throw new RuntimeException("非法动作端点（仅支持 /api/v1/ 前缀）: " + endpoint);
        }
        if (!"POST".equals(method) && !"PUT".equals(method)) {
            throw new RuntimeException("仅支持 POST/PUT 动作: " + method);
        }

        // 3. 经网关调用目标业务端点（Authorization 头由 RestTemplate 拦截器自动中继）
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> httpEntity = new HttpEntity<>(payload, headers);
        String url = gatewayUrl + endpoint;
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.valueOf(method), httpEntity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                // HTTP 2xx 还需校验业务信封 code：本系统各服务异常会包装为 HTTP 200 + code:500 的 Result 信封
                // 成功码兼容 200（ERP ApiResponse）与 0（OA ResultCode.SUCCESS）
                String bodyError = extractEnvelopeError(response.getBody());
                if (bodyError != null) {
                    return fail(suggestion, actionLabel + bodyError);
                }
                // 执行成功：主动作置 EXECUTED 回写业务单号；次要动作保持 PENDING 仅留痕（主动作仍可执行）
                // 反馈追加留痕：多动作场景保留完整执行轨迹，避免后执行动作覆盖先执行记录
                String summary = summarize(response.getBody());
                if (actionIndex == null) {
                    suggestion.setStatus("EXECUTED");
                    suggestion.setResolvedTime(LocalDateTime.now());
                    suggestion.setFeedback(appendFeedback(suggestion.getFeedback(), "已执行：" + summary));
                } else {
                    suggestion.setFeedback(appendFeedback(suggestion.getFeedback(), actionLabel + "已执行：" + summary));
                }
                suggestionRepository.save(suggestion);
                log.info("AI动作执行成功: id={}, type={}, endpoint={}, 结果={}",
                        suggestionId, suggestion.getSuggestionType(), endpoint, summary);
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", actionLabel + "执行成功：" + summary);
                result.put("data", response.getBody());
                return result;
            }
            return fail(suggestion, actionLabel + "HTTP " + response.getStatusCode().value());
        } catch (HttpStatusCodeException e) {
            // 业务端点返回 4xx/5xx：回写响应体摘要便于定位（如唯一键冲突/参数校验失败）
            return fail(suggestion, actionLabel + "HTTP " + e.getStatusCode().value() + " "
                    + truncate(e.getResponseBodyAsString(), 300));
        } catch (Exception e) {
            return fail(suggestion, actionLabel + truncate(e.getMessage(), 300));
        }
    }

    /**
     * 解析动作草稿 JSON 为 Map 契约
     *
     * @param json 动作草稿 JSON 字符串
     * @return 草稿契约（endpoint/method/payload/...）
     */
    private Map<String, Object> parseDraft(String json) {
        try {
            return objectMapper.readValue(json,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() { });
        } catch (Exception e) {
            throw new RuntimeException("动作草稿解析失败: " + e.getMessage());
        }
    }

    /**
     * 解析次要动作草稿（P4-6 多专家联动）
     * 从建议 analysis.secondaryActions 数组中取下标 actionIndex 的动作契约，
     * 与 action_draft 契约一致（endpoint/method/payload），额外带 domain 标识来源领域
     *
     * @param suggestion  建议实体
     * @param actionIndex 次要动作下标
     * @return 次要动作草稿契约
     */
    private Map<String, Object> resolveSecondaryAction(AiSuggestionEntity suggestion, int actionIndex) {
        if (suggestion.getAnalysis() == null || suggestion.getAnalysis().isBlank()) {
            throw new RuntimeException("该建议无分析内容，无次要动作可执行");
        }
        Map<String, Object> analysis;
        try {
            analysis = objectMapper.readValue(suggestion.getAnalysis(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() { });
        } catch (Exception e) {
            throw new RuntimeException("建议分析内容解析失败: " + e.getMessage());
        }
        Object secondary = analysis.get("secondaryActions");
        if (!(secondary instanceof java.util.List<?> list) || list.isEmpty()) {
            throw new RuntimeException("该建议无次要动作草稿");
        }
        if (actionIndex < 0 || actionIndex >= list.size()) {
            throw new RuntimeException("次要动作索引越界: " + actionIndex + "（共 " + list.size() + " 项）");
        }
        if (!(list.get(actionIndex) instanceof Map<?, ?> map)) {
            throw new RuntimeException("次要动作草稿格式非法（非对象）");
        }
        Map<String, Object> draft = new HashMap<>();
        map.forEach((k, v) -> draft.put(String.valueOf(k), v));
        return draft;
    }

    /**
     * 执行失败处理：状态保持 PENDING 允许重试，feedback 回写失败原因
     *
     * @param suggestion 建议实体
     * @param reason 失败原因
     * @return 失败结果
     */
    private Map<String, Object> fail(AiSuggestionEntity suggestion, String reason) {
        suggestion.setFeedback(appendFeedback(suggestion.getFeedback(), "执行失败：" + reason));
        suggestionRepository.save(suggestion);
        log.warn("AI动作执行失败: id={}, type={}, 原因={}", suggestion.getId(), suggestion.getSuggestionType(), reason);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "执行失败：" + reason);
        return result;
    }

    /**
     * 解析统一响应信封的业务错误：含 code 字段且非成功码（200/0）时返回错误描述，否则返回 null
     * 背景：部分服务全局异常处理器将系统错误包装为 HTTP 200 + code:500 信封，仅判 HTTP 状态会误判成功
     *
     * @param body 响应体 JSON 字符串
     * @return 错误描述（无错误返回 null）
     */
    private String extractEnvelopeError(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> envelope = objectMapper.readValue(body,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() { });
            Object code = envelope.get("code");
            if (code instanceof Number n) {
                int c = n.intValue();
                if (c == 200 || c == 0) {
                    return null;
                }
                Object msg = envelope.get("msg");
                return "业务码 " + c + "：" + truncate(msg == null ? "未知错误" : String.valueOf(msg), 200);
            }
            return null;
        } catch (Exception e) {
            // 非 JSON 响应体按 HTTP 状态已判定为成功，不拦截
            return null;
        }
    }

    /**
     * 从业务端点响应体提取结果摘要（优先取业务单号，其次取 data 主键）
     *
     * @param body 响应体 JSON 字符串
     * @return 摘要文本
     */
    private String summarize(String body) {
        if (body == null || body.isBlank()) {
            return "目标端点已受理";
        }
        try {
            Map<String, Object> envelope = objectMapper.readValue(body,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() { });
            Object data = envelope.get("data");
            if (data instanceof Map<?, ?> dataMap) {
                // 常见业务单号字段优先展示
                for (String key : new String[]{"requestCode", "orderNo", "code", "workOrderNo", "id"}) {
                    Object v = dataMap.get(key);
                    if (v != null) {
                        return "业务单号 " + v;
                    }
                }
            }
            Object msg = envelope.get("msg");
            return msg == null ? "目标端点已受理" : String.valueOf(msg);
        } catch (Exception e) {
            return truncate(body, 200);
        }
    }

    /**
     * 字符串截断（防 feedback 过长）
     */
    private String truncate(String s, int max) {
        if (s == null) {
            return "未知错误";
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    /**
     * 反馈追加留痕（多动作场景保留完整执行轨迹，避免后执行动作覆盖先执行记录）
     *
     * @param existing 已有反馈内容
     * @param entry    本次追加条目
     * @return 拼接后的反馈
     */
    private String appendFeedback(String existing, String entry) {
        if (existing == null || existing.isBlank()) {
            return entry;
        }
        return existing + "；" + entry;
    }
}
