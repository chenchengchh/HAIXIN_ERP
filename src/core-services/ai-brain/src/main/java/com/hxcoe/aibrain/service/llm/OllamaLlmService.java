package com.hxcoe.aibrain.service.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.aibrain.service.DecisionLogService;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Ollama 本地模型 LLM 实现（P4-7 k-LLM 网关，数据不出域）
 * 通过 Ollama /api/chat 非流式接口调用本地开源模型（默认 qwen2.5:3b，可配置切换）。
 * 设计铁律：
 * - 可信算法优先：LLM 只做语义增强（重排序/草稿生成/字段校验），决策阈值仍由规则引擎确定
 * - 失败静默降级：任何异常（服务不可达/超时/解析失败）返回 null 并留痕，调用方走规则逻辑
 * - 全程可观测：每次调用记录 token 消耗与耗时到 ai_decision_log（triggerType=LLM_CALL）
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "ai.llm.provider", havingValue = "ollama")
public class OllamaLlmService implements LlmService {

    /** Ollama 服务地址（容器内经 host.docker.internal 访问宿主机 Ollama） */
    @Value("${ai.llm.ollama.base-url:http://host.docker.internal:11434}")
    private String baseUrl;

    /** 默认模型（qwen2.5:3b 轻量快速；可配置 qwen3.5:4b 提质） */
    @Value("${ai.llm.ollama.model:qwen2.5:3b}")
    private String model;

    /** 调用超时（毫秒）：本地模型 CPU 推理较慢，默认 90s */
    @Value("${ai.llm.ollama.timeout-ms:90000}")
    private int timeoutMs;

    /** 最大生成 token 数（限制输出长度防失控，同时压低推理耗时） */
    @Value("${ai.llm.ollama.max-tokens:1024}")
    private int maxTokens;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DecisionLogService decisionLogService;

    private volatile RestTemplate restTemplate;

    /**
     * 通用对话（无技能归属时使用，留痕 skillCode=GENERIC）
     */
    @Override
    public String chat(String systemPrompt, String userPrompt) {
        return chatForSkill("GENERIC", systemPrompt, userPrompt);
    }

    /**
     * 带技能归属的对话：每次调用记录 token/耗时到决策日志
     *
     * @param skillCode    发起技能编码（用于 token 消耗归集）
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户提示词
     * @return 模型输出文本（失败返回 null）
     */
    @Override
    public String chatForSkill(String skillCode, String systemPrompt, String userPrompt) {
        long start = System.currentTimeMillis();
        int tokens = 0;
        String error = null;
        String content = null;
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("stream", false);
            // qwen3.5 等思考型模型关闭思考过程（仅取最终回答，压时延）；非思考模型忽略该字段
            body.put("think", false);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt == null ? "" : systemPrompt),
                    Map.of("role", "user", "content", userPrompt == null ? "" : userPrompt)));
            body.put("options", Map.of("temperature", 0.2, "num_predict", maxTokens));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String resp = restTemplate().postForObject(baseUrl + "/api/chat",
                    new HttpEntity<>(body, headers), String.class);

            JsonNode root = objectMapper.readTree(resp);
            JsonNode message = root.path("message").path("content");
            content = message.isTextual() ? message.asText().trim() : null;
            tokens = root.path("prompt_eval_count").asInt(0) + root.path("eval_count").asInt(0);
            if (content == null || content.isEmpty()) {
                error = "模型返回空内容";
                content = null;
            }
        } catch (Exception e) {
            error = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.warn("Ollama调用失败（skill={}, 降级为规则逻辑）: {}", skillCode, error);
            content = null;
        } finally {
            decisionLogService.logLlmCall(skillCode, tokens,
                    (int) (System.currentTimeMillis() - start), error);
        }
        return content;
    }

    /**
     * 证据转写：LLM 可用时用模型组织语言，失败回退模板拼接
     */
    @Override
    public String translateToAdvice(String scenario, Map<String, Object> evidence) {
        String sys = "你是工业智能决策助手。把给定的结构化检测指标转写为一段面向值班经理的中文处置建议"
                + "（100字以内，先说结论再说依据，不要编造指标中不存在的数据）。";
        String user = "场景：" + scenario + "\n指标：" + (evidence == null ? "{}" : evidence.toString());
        String llmText = chatForSkill(scenario, sys, user);
        if (llmText != null) {
            return llmText;
        }
        // 模板降级（与 RuleBasedLlmService 一致）
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
     * 因果链压缩：LLM 可用时做语义压缩，失败回退截断
     */
    @Override
    public String summarizeChain(String chainSummary) {
        if (chainSummary == null || chainSummary.isBlank()) {
            return "未发现可追溯链路。";
        }
        String llmText = chatForSkill("CHAIN_SUMMARY",
                "你是根因分析助手。把跨域失效链各环节发现压缩为一句中文结论（60字以内，突出因果传递关系）。",
                chainSummary);
        if (llmText != null) {
            return llmText;
        }
        return chainSummary.length() <= 200 ? chainSummary : chainSummary.substring(0, 200) + "…";
    }

    @Override
    public String provider() {
        return "ollama";
    }

    /**
     * 当前模型标识（状态端点展示用）
     *
     * @return 模型名
     */
    public String model() {
        return model;
    }

    /**
     * 构建带超时的 RestTemplate（双重检查锁懒加载，避免启动期初始化开销）
     */
    private RestTemplate restTemplate() {
        if (restTemplate == null) {
            synchronized (this) {
                if (restTemplate == null) {
                    restTemplate = new RestTemplateBuilder()
                            .setConnectTimeout(Duration.ofMillis(Math.min(timeoutMs, 10000)))
                            .setReadTimeout(Duration.ofMillis(timeoutMs))
                            .build();
                }
            }
        }
        return restTemplate;
    }
}
