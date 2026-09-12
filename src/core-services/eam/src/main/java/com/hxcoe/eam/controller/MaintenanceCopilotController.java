package com.hxcoe.eam.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.eam.service.MaintenanceCopilotService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 维修 Copilot 控制器（AI决策系统 P0-3，规则版）
 * 为维修工提供相似故障的历史处置方案检索，响应含 evidence 证据链
 */
@RestController
@RequestMapping("/api/v1/eam/copilot")
public class MaintenanceCopilotController {

    @Autowired
    private MaintenanceCopilotService maintenanceCopilotService;

    /**
     * 相似故障维修建议检索
     *
     * @param equipmentId 设备ID（可选）
     * @param symptom 症状关键词（可选，匹配故障类型/描述）
     * @param limit 返回条数（默认5，上限20）
     * @return 统一信封 {success, evidence, advices}
     */
    @GetMapping("/fault-advice")
    public Result<Map<String, Object>> faultAdvice(
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) String symptom,
            @RequestParam(required = false) Integer limit
    ) {
        return Result.success("查询成功", maintenanceCopilotService.faultAdvice(equipmentId, symptom, limit));
    }
}
