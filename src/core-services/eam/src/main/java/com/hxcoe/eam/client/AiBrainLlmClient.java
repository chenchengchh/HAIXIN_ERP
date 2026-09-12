package com.hxcoe.eam.client;

import com.hxcoe.common.result.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * EAM 调用 ai-brain LLM 技能对话端点的 Feign 客户端（P4-7 S08 维修 Copilot 语义升级）。
 *
 * <p>维修 Copilot 规则召回候选故障后，将候选列表交给 ai-brain 的本地 Ollama 模型做语义重排序，
 * 突破 SQL LIKE 关键词匹配的天花板（同义/近义表述识别）。
 *
 * <p>降级约定：ai-brain 侧 LLM 不可用时返回 {@code degraded=true} 且 {@code content=null}，
 * 或 Feign 调用本身异常时，调用方必须回退为规则排序结果（静默降级，不向用户报错）。
 */
@FeignClient(name = "ai-brain-service", path = "/api/v1/ai-brain/llm", contextId = "eamAiBrainLlmClient")
public interface AiBrainLlmClient {

    /**
     * 技能专用 LLM 对话
     *
     * @param body 请求体（skillCode 技能编码；userPrompt 用户提示词；systemPrompt/templateVars 可选）
     * @return 统一信封：data.degraded 是否降级、data.provider 提供者、data.content 模型输出文本
     */
    @PostMapping("/skill-chat")
    Result<Map<String, Object>> skillChat(@RequestBody Map<String, Object> body);
}
