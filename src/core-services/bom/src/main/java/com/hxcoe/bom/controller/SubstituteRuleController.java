package com.hxcoe.bom.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResultAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.bom.entity.SubstituteRuleEntity;
import com.hxcoe.bom.service.SubstituteRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 替代规则配置控制器
 * 管理替代料的全局/分类级策略规则
 */
@RestController
@RequestMapping({"/bom/substitute-rules", "/api/v1/bom/substitute-rules"})
public class SubstituteRuleController {

    @Autowired
    private SubstituteRuleService ruleService;

    /**
     * 创建替代规则
     */
    @PostMapping
    public ApiResponse<SubstituteRuleEntity> createRule(@RequestBody SubstituteRuleEntity rule) {
        return ResultAdapter.fromResult(ruleService.createRule(rule));
    }

    /**
     * 更新替代规则
     */
    @PutMapping("/{id}")
    public ApiResponse<SubstituteRuleEntity> updateRule(@PathVariable("id") Long id, @RequestBody SubstituteRuleEntity rule) {
        return ResultAdapter.fromResult(ruleService.updateRule(id, rule));
    }

    /**
     * 删除替代规则
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteRule(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(ruleService.deleteRule(id));
    }

    /**
     * 获取替代规则详情
     */
    @GetMapping("/{id}")
    public ApiResponse<SubstituteRuleEntity> getRuleById(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(ruleService.getRuleById(id));
    }

    /**
     * 分页查询替代规则列表
     */
    @GetMapping
    public ApiResponse<PageResult<SubstituteRuleEntity>> getRuleList(
            @RequestParam(name = "ruleName", required = false) String ruleName,
            @RequestParam(name = "ruleType", required = false) Integer ruleType,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        return ResultAdapter.fromResult(ruleService.getRulesPage(ruleName, ruleType, status, page, size));
    }

    /**
     * 启用规则
     */
    @PutMapping("/{id}/enable")
    public ApiResponse<SubstituteRuleEntity> enableRule(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(ruleService.enableRule(id));
    }

    /**
     * 禁用规则
     */
    @PutMapping("/{id}/disable")
    public ApiResponse<SubstituteRuleEntity> disableRule(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(ruleService.disableRule(id));
    }
}
