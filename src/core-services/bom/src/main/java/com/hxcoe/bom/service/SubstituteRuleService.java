package com.hxcoe.bom.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.bom.entity.SubstituteRuleEntity;

/**
 * 替代规则配置Service接口
 */
public interface SubstituteRuleService {

    /**
     * 创建替代规则
     */
    Result<SubstituteRuleEntity> createRule(SubstituteRuleEntity rule);

    /**
     * 更新替代规则
     */
    Result<SubstituteRuleEntity> updateRule(Long id, SubstituteRuleEntity rule);

    /**
     * 删除替代规则
     */
    Result<Void> deleteRule(Long id);

    /**
     * 获取替代规则详情
     */
    Result<SubstituteRuleEntity> getRuleById(Long id);

    /**
     * 分页查询替代规则
     */
    Result<PageResult<SubstituteRuleEntity>> getRulesPage(
            String ruleName, Integer ruleType, Integer status, Integer page, Integer size);

    /**
     * 启用规则
     */
    Result<SubstituteRuleEntity> enableRule(Long id);

    /**
     * 禁用规则
     */
    Result<SubstituteRuleEntity> disableRule(Long id);
}
