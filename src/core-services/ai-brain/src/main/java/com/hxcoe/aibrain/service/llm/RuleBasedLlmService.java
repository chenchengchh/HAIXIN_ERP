package com.hxcoe.aibrain.service.llm;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 规则版 LLM 降级实现（默认，零外部依赖）
 * 用模板将结构化证据转写为自然语言，保证 LLM 服务未接入/不可用时系统功能完整。
 * 触发条件：ai.llm.provider=rule 或未配置（matchIfMissing=true）
 */
@Service
@ConditionalOnProperty(name = "ai.llm.provider", havingValue = "rule", matchIfMissing = true)
public class RuleBasedLlmService implements LlmService {

    /**
     * 模板转写：把证据键值对展开为"基于[数据源]，检测到[指标]：建议人工复核趋势"
     *
     * @param scenario 场景标识
     * @param evidence 结构化证据
     * @return 自然语言建议
     */
    @Override
    public String translateToAdvice(String scenario, Map<String, Object> evidence) {
        if (evidence == null || evidence.isEmpty()) {
            return "【" + scenario + "】规则引擎已生成建议，证据详情见分析字段。";
        }
        String metrics = evidence.entrySet().stream()
                .limit(6)
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("，"));
        return "【" + scenario + "】检测到关键指标：" + metrics + "。建议结合业务上下文复核后处置。";
    }

    /**
     * 链路摘要降级：直接截断拼接（不做语义压缩）
     *
     * @param chainSummary 链路摘要
     * @return 一句话结论
     */
    @Override
    public String summarizeChain(String chainSummary) {
        if (chainSummary == null || chainSummary.isBlank()) {
            return "未发现可追溯链路。";
        }
        return chainSummary.length() <= 200 ? chainSummary : chainSummary.substring(0, 200) + "…";
    }

    /**
     * 提供者标识
     *
     * @return "rule"（模板降级实现）
     */
    @Override
    public String provider() {
        return "rule";
    }

    /**
     * 通用对话：规则实现无真实模型能力，返回 null 通知调用方走规则降级
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return 恒为 null
     */
    @Override
    public String chat(String systemPrompt, String userPrompt) {
        return null;
    }
}
