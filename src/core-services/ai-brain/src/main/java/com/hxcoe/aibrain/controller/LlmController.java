package com.hxcoe.aibrain.controller;

import com.hxcoe.aibrain.service.llm.LlmService;
import com.hxcoe.aibrain.service.llm.PromptTemplateService;
import com.hxcoe.common.result.Result;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LLM 技能对话端点（P4-7 k-LLM 网关对外出口）
 * 供本服务技能与其他服务（如 EAM 维修 Copilot）发起"带技能归属"的 LLM 调用：
 * - systemPrompt 可直接传入，也可省略由 ai_prompt_template 表按 skillCode 取启用中最高版本模板渲染
 * - LLM 不可用/失败时 degraded=true 且 content=null，调用方必须走规则降级逻辑（静默，不报错）
 * - 本端点只读不落业务库，调用留痕由 OllamaLlmService 写入 ai_decision_log（triggerType=LLM_CALL）
 */
@RestController
@RequestMapping("/api/v1/ai-brain/llm")
public class LlmController {

    @Autowired
    private LlmService llmService;

    @Autowired
    private PromptTemplateService promptTemplateService;

    /**
     * 技能专用对话
     *
     * @param request 对话请求（skillCode 必填；userPrompt 必填；systemPrompt 可选，缺省走模板渲染；templateVars 可选）
     * @return 统一信封 {success, degraded, provider, content}
     */
    @PostMapping("/skill-chat")
    public Result<Map<String, Object>> skillChat(@RequestBody SkillChatRequest request) {
        if (request.getSkillCode() == null || request.getSkillCode().isBlank()) {
            return Result.error("skillCode 不能为空");
        }
        if (request.getUserPrompt() == null || request.getUserPrompt().isBlank()) {
            return Result.error("userPrompt 不能为空");
        }

        // 系统提示词：显式传入优先，否则按 skillCode 从模板表渲染
        String systemPrompt = request.getSystemPrompt();
        if (systemPrompt == null || systemPrompt.isBlank()) {
            systemPrompt = promptTemplateService.render(request.getSkillCode(), request.getTemplateVars());
        }
        if (systemPrompt == null || systemPrompt.isBlank()) {
            return Result.error("未找到技能提示词模板: " + request.getSkillCode() + "（可显式传入 systemPrompt）");
        }

        String content = llmService.chatForSkill(request.getSkillCode(), systemPrompt, request.getUserPrompt());

        Map<String, Object> data = new HashMap<>();
        data.put("degraded", content == null);
        data.put("provider", llmService.provider());
        data.put("content", content);
        return Result.success(content == null ? "LLM 不可用，已降级" : "调用成功", data);
    }

    /** 技能对话请求体 */
    public static class SkillChatRequest {
        /** 技能编码（MAINTENANCE_COPILOT_RERANK/EIGHT_D_ROOTCAUSE/VISION_INVOICE_EXTRACT 等） */
        private String skillCode;
        /** 用户提示词（待处理内容） */
        private String userPrompt;
        /** 系统提示词（可选，缺省时按 skillCode 从 ai_prompt_template 渲染） */
        private String systemPrompt;
        /** 模板变量上下文（可选，配合模板渲染 ${var} 占位符） */
        private Map<String, Object> templateVars;

        public String getSkillCode() {
            return skillCode;
        }

        public void setSkillCode(String skillCode) {
            this.skillCode = skillCode;
        }

        public String getUserPrompt() {
            return userPrompt;
        }

        public void setUserPrompt(String userPrompt) {
            this.userPrompt = userPrompt;
        }

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }

        public Map<String, Object> getTemplateVars() {
            return templateVars;
        }

        public void setTemplateVars(Map<String, Object> templateVars) {
            this.templateVars = templateVars;
        }
    }
}
