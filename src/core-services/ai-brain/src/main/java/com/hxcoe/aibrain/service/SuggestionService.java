package com.hxcoe.aibrain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.repository.AiSuggestionRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI 建议服务（S13 反馈学习环核心）
 * 职责：建议幂等落库、人工处置回写、采纳率统计
 * 铁律：CONFIRM 级建议执行时一律调用现有业务端点，不直接 UPDATE 业务表
 */
@Slf4j
@Service
public class SuggestionService {

    @Autowired
    private AiSuggestionRepository suggestionRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 幂等落库建议：同 eventId 已存在时不重复生成（不论处置状态）
     *
     * @param suggestion 建议实体（eventId 必填）
     * @return true=新增，false=已存在跳过
     */
    public boolean publish(AiSuggestionEntity suggestion) {
        if (suggestion.getEventId() != null) {
            var existing = suggestionRepository.findByEventId(suggestion.getEventId());
            if (existing.isPresent()) {
                // 幂等拒绝：eventId 唯一键约束下，已处置状态再插入会抛重复键500，
                // 一律跳过，调用方需要时按 eventId 回查已落库建议
                return false;
            }
        }
        if (suggestion.getCreatedTime() == null) {
            suggestion.setCreatedTime(LocalDateTime.now());
        }
        suggestionRepository.save(suggestion);
        return true;
    }

    /**
     * 构造 evidence 证据链 JSON（统一格式：sources + metrics + queryTime）
     *
     * @param sources 数据来源表列表
     * @param metrics 关键指标（名称→值）
     * @return JSON 字符串
     */
    public String buildEvidence(List<String> sources, Map<String, Object> metrics) {
        try {
            Map<String, Object> evidence = new HashMap<>();
            evidence.put("sources", sources);
            evidence.put("metrics", metrics == null ? Map.of() : metrics);
            evidence.put("queryTime", LocalDateTime.now().toString().replace('T', ' ').substring(0, 19));
            return objectMapper.writeValueAsString(evidence);
        } catch (Exception e) {
            log.warn("构造证据链JSON失败: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 人工处置（S13 学习环入口）
     *
     * @param id 建议ID
     * @param action ACCEPTED/REJECTED/MODIFIED/EXECUTED
     * @param feedback 处置说明
     * @return 更新后的建议
     */
    public AiSuggestionEntity feedback(Long id, String action, String feedback) {
        AiSuggestionEntity entity = suggestionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("建议不存在: " + id));
        entity.setStatus(action);
        entity.setFeedback(feedback);
        entity.setResolvedTime(LocalDateTime.now());
        AiSuggestionEntity saved = suggestionRepository.save(entity);
        log.info("AI建议人工处置: id={}, type={}, action={}", id, entity.getSuggestionType(), action);
        return saved;
    }

    /**
     * 按幂等键查询建议
     *
     * @param eventId 幂等键
     * @return 建议实体（无则 null）
     */
    public AiSuggestionEntity getByEventId(String eventId) {
        return suggestionRepository.findByEventId(eventId).orElse(null);
    }

    /**
     * 采纳率统计（学习环分析：按类型统计处置分布）
     *
     * @return {byType: [{type, accepted, rejected, pending, acceptRate}]}
     */
    public Map<String, Object> adoptionStats() {
        List<AiSuggestionEntity> all = suggestionRepository.findAll();
        Map<String, long[]> byType = new HashMap<>();
        for (AiSuggestionEntity s : all) {
            long[] arr = byType.computeIfAbsent(s.getSuggestionType(), k -> new long[4]);
            switch (s.getStatus() == null ? "PENDING" : s.getStatus()) {
                case "ACCEPTED", "EXECUTED" -> arr[0]++;
                case "REJECTED" -> arr[1]++;
                case "MODIFIED" -> arr[2]++;
                default -> arr[3]++;
            }
        }
        List<Map<String, Object>> rows = byType.entrySet().stream().map(e -> {
            Map<String, Object> row = new HashMap<String, Object>();
            long decided = e.getValue()[0] + e.getValue()[1] + e.getValue()[2];
            row.put("type", e.getKey());
            row.put("accepted", e.getValue()[0]);
            row.put("rejected", e.getValue()[1]);
            row.put("modified", e.getValue()[2]);
            row.put("pending", e.getValue()[3]);
            row.put("acceptRate", decided == 0 ? 0 : Math.round(e.getValue()[0] * 1000.0 / decided) / 10.0);
            return row;
        }).toList();
        Map<String, Object> result = new HashMap<>();
        result.put("byType", rows);
        result.put("totalCount", all.size());
        return result;
    }
}
