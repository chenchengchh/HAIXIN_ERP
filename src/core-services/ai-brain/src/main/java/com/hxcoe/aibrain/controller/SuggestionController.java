package com.hxcoe.aibrain.controller;

import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.repository.AiSuggestionRepository;
import com.hxcoe.aibrain.service.ActionExecutorService;
import com.hxcoe.aibrain.service.SuggestionService;
import com.hxcoe.common.result.Result;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 建议控制器（决策中心数据源）
 * 提供建议列表查询与人工处置（S13 学习环）端点
 */
@RestController
@RequestMapping("/api/v1/ai-brain/suggestions")
public class SuggestionController {

    @Autowired
    private AiSuggestionRepository suggestionRepository;

    @Autowired
    private SuggestionService suggestionService;

    @Autowired
    private ActionExecutorService actionExecutorService;

    /**
     * 查询建议列表（决策中心页）
     *
     * @param status 状态过滤（可选，默认查 PENDING）
     * @param type 类型过滤（可选）
     * @return 建议列表
     */
    @GetMapping
    public Result<List<AiSuggestionEntity>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type
    ) {
        List<AiSuggestionEntity> list;
        if (type != null && !type.isBlank()) {
            list = suggestionRepository.findBySuggestionTypeOrderByCreatedTimeDesc(type);
        } else {
            list = suggestionRepository.findByStatusOrderByCreatedTimeDesc(
                    (status == null || status.isBlank()) ? "PENDING" : status);
        }
        return Result.success("查询成功", list);
    }

    /**
     * 人工处置建议（采纳/拒绝/修改/已执行）
     *
     * @param id 建议ID
     * @param body {action, feedback}
     * @return 更新后的建议
     */
    @PutMapping("/{id}/feedback")
    public Result<AiSuggestionEntity> feedback(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String action = body.getOrDefault("action", "ACCEPTED");
        String feedback = body.get("feedback");
        return Result.success("处置成功", suggestionService.feedback(id, action, feedback));
    }

    /**
     * 一键执行建议动作（P4-1 动作执行引擎）
     * CONFIRM 级建议经前端确认后调用：解析 actionDraft 经网关调目标模块现有端点，
     * 成功置 EXECUTED 回写业务单号，失败保持 PENDING 回写原因允许重试。
     * actionIndex（P4-6 多专家联动）：非空时执行 analysis.secondaryActions 中对应次要动作，
     * 执行成功建议保持 PENDING 仅留痕，主动作仍可执行
     *
     * @param id          建议ID
     * @param actionIndex 次要动作下标（可空，空=主动作）
     * @return 执行结果 {success, message, data}
     */
    @PostMapping("/{id}/execute")
    public Result<Map<String, Object>> execute(@PathVariable Long id,
                                               @RequestParam(required = false) Integer actionIndex) {
        Map<String, Object> result = actionExecutorService.execute(id, actionIndex);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return Result.success(String.valueOf(result.get("message")), result);
        }
        return Result.fail(String.valueOf(result.get("message")));
    }

    /**
     * 采纳率统计（学习环分析）
     *
     * @return 按类型的采纳率分布
     */
    @GetMapping("/adoption-stats")
    public Result<Map<String, Object>> adoptionStats() {
        return Result.success("查询成功", suggestionService.adoptionStats());
    }
}
