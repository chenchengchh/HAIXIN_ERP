package com.hxcoe.bom.service.impl;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.bom.entity.BomCategoryEntity;
import com.hxcoe.bom.entity.SubstituteRuleEntity;
import com.hxcoe.bom.repository.BomCategoryRepository;
import com.hxcoe.bom.repository.SubstituteRuleRepository;
import com.hxcoe.bom.service.SubstituteRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 替代规则配置Service实现类
 */
@Service
public class SubstituteRuleServiceImpl implements SubstituteRuleService {

    @Autowired
    private SubstituteRuleRepository ruleRepository;

    @Autowired
    private BomCategoryRepository categoryRepository;

    /**
     * 创建替代规则
     */
    @Override
    public Result<SubstituteRuleEntity> createRule(SubstituteRuleEntity rule) {
        if (rule.getStatus() == null) {
            rule.setStatus(1); // 默认启用
        }
        if (rule.getPriority() == null) {
            rule.setPriority(99); // 默认低优先级
        }
        rule.setCreatedTime(LocalDateTime.now());
        SubstituteRuleEntity saved = ruleRepository.save(rule);
        return Result.success(enrich(saved));
    }

    /**
     * 更新替代规则
     */
    @Override
    public Result<SubstituteRuleEntity> updateRule(Long id, SubstituteRuleEntity rule) {
        SubstituteRuleEntity existing = ruleRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("替代规则不存在");
        }
        rule.setId(id);
        // 保留原创建审计字段，仅刷新更新时间
        rule.setCreatedTime(existing.getCreatedTime());
        rule.setCreatedBy(existing.getCreatedBy());
        rule.setUpdatedTime(LocalDateTime.now());
        SubstituteRuleEntity updated = ruleRepository.save(rule);
        return Result.success(enrich(updated));
    }

    /**
     * 删除替代规则
     */
    @Override
    public Result<Void> deleteRule(Long id) {
        if (!ruleRepository.existsById(id)) {
            return Result.error("替代规则不存在");
        }
        ruleRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 获取替代规则详情
     */
    @Override
    public Result<SubstituteRuleEntity> getRuleById(Long id) {
        SubstituteRuleEntity rule = ruleRepository.findById(id).orElse(null);
        if (rule == null) {
            return Result.error("替代规则不存在");
        }
        return Result.success(enrich(rule));
    }

    /**
     * 分页查询替代规则
     */
    @Override
    public Result<PageResult<SubstituteRuleEntity>> getRulesPage(
            String ruleName, Integer ruleType, Integer status, Integer page, Integer size) {
        int safePage = page == null || page <= 0 ? 0 : page - 1;
        int safeSize = size == null || size <= 0 ? 10 : size;

        Specification<SubstituteRuleEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            if (ruleName != null && !ruleName.isBlank()) {
                predicates.add(cb.like(root.get("ruleName"), "%" + ruleName.trim() + "%"));
            }
            if (ruleType != null) {
                predicates.add(cb.equal(root.get("ruleType"), ruleType));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SubstituteRuleEntity> result = ruleRepository.findAll(spec, PageRequest.of(safePage, safeSize));
        List<SubstituteRuleEntity> enriched = result.getContent();
        enriched.forEach(this::enrich);

        PageResult<SubstituteRuleEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                enriched
        );
        return Result.success(pageResult);
    }

    /**
     * 启用规则
     */
    @Override
    public Result<SubstituteRuleEntity> enableRule(Long id) {
        SubstituteRuleEntity rule = ruleRepository.findById(id).orElse(null);
        if (rule == null) {
            return Result.error("替代规则不存在");
        }
        rule.setStatus(1);
        rule.setUpdatedTime(LocalDateTime.now());
        SubstituteRuleEntity updated = ruleRepository.save(rule);
        return Result.success(enrich(updated));
    }

    /**
     * 禁用规则
     */
    @Override
    public Result<SubstituteRuleEntity> disableRule(Long id) {
        SubstituteRuleEntity rule = ruleRepository.findById(id).orElse(null);
        if (rule == null) {
            return Result.error("替代规则不存在");
        }
        rule.setStatus(0);
        rule.setUpdatedTime(LocalDateTime.now());
        SubstituteRuleEntity updated = ruleRepository.save(rule);
        return Result.success(enrich(updated));
    }

    /**
     * 回填适用分类名称，供前端展示
     */
    private SubstituteRuleEntity enrich(SubstituteRuleEntity rule) {
        if (rule == null) {
            return null;
        }
        if (rule.getTargetCategoryId() != null) {
            BomCategoryEntity category = categoryRepository.findById(rule.getTargetCategoryId()).orElse(null);
            if (category != null) {
                rule.setTargetCategoryName(category.getName());
            }
        }
        return rule;
    }
}
