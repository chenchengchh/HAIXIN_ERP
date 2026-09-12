package com.hxcoe.aibrain.service.llm;

import java.util.Map;

/**
 * LLM 翻译层接口（P2 预留）
 * 设计原则：可信算法优先，LLM 只做"翻译"——把规则引擎产出的结构化证据转写为自然语言建议，
 * 不参与决策本身（决策阈值、触发条件、动作草稿全部由规则引擎确定，保证可解释性与可审计性）。
 *
 * 实现策略：
 * - 默认 RuleBasedLlmService（模板转写，零外部依赖，永远可用）
 * - 未来接入 Spring AI（OpenAI/通义/文心）时新增实现并切换 ai.llm.provider 配置即可，
 *   调用方无需改动（面向接口编程 + 条件装配）
 */
public interface LlmService {

    /**
     * 将结构化证据转写为自然语言建议文本
     *
     * @param scenario 场景标识（如 FAULT_PREDICTION/DELIVERY_RISK/EIGHT_D）
     * @param evidence 规则引擎产出的结构化证据（指标值/数据源/推演步骤）
     * @return 自然语言建议文本（失败时实现方必须降级为模板文本，禁止抛异常阻断主流程）
     */
    String translateToAdvice(String scenario, Map<String, Object> evidence);

    /**
     * 将因果链证据压缩为一句话结论
     *
     * @param chainSummary 链路各环节 finding 摘要
     * @return 一句话结论
     */
    String summarizeChain(String chainSummary);

    /**
     * 当前实现提供者标识（rule/openai/qwen...），用于状态端点展示与日志审计
     *
     * @return 提供者标识
     */
    String provider();

    /**
     * 通用对话能力（P4-7 扩展）
     * 规则降级实现返回 null（表示"无真实 LLM 可用"），调用方据此走规则逻辑；
     * 真实模型实现（Ollama 等）失败时也必须返回 null 而非抛异常（调用方静默降级）。
     *
     * @param systemPrompt 系统提示词（角色/任务/输出格式约束）
     * @param userPrompt   用户提示词（待处理内容）
     * @return 模型输出文本（不可用/失败返回 null）
     */
    String chat(String systemPrompt, String userPrompt);

    /**
     * 带技能归属的对话（P4-7 扩展）
     * 技能编码用于 token 消耗归集与审计留痕；默认实现忽略归属直接转发 chat()，
     * 真实模型实现（如 OllamaLlmService）覆写后按技能记录调用日志。
     *
     * @param skillCode    发起技能编码（MAINTENANCE_COPILOT_RERANK/EIGHT_D_ROOTCAUSE 等）
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return 模型输出文本（不可用/失败返回 null）
     */
    default String chatForSkill(String skillCode, String systemPrompt, String userPrompt) {
        return chat(systemPrompt, userPrompt);
    }

    /**
     * 带回退的对话：LLM 不可用或失败时返回 fallback 文本
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @param fallback     回退文本
     * @return 模型输出或回退文本
     */
    default String chatWithFallback(String systemPrompt, String userPrompt, String fallback) {
        String r = chat(systemPrompt, userPrompt);
        return (r == null || r.isBlank()) ? fallback : r;
    }
}
