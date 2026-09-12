package com.hxcoe.bom.service.impl;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.bom.entity.MaterialEntity;
import com.hxcoe.bom.entity.SubstituteEntity;
import com.hxcoe.bom.entity.SubstituteRuleEntity;
import com.hxcoe.bom.repository.MaterialRepository;
import com.hxcoe.bom.repository.SubstituteRepository;
import com.hxcoe.bom.repository.SubstituteRuleRepository;
import com.hxcoe.bom.service.SubstituteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 替代料管理Service实现类
 */
@Service
public class SubstituteServiceImpl implements SubstituteService {

    @Autowired
    private SubstituteRepository substituteRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private SubstituteRuleRepository ruleRepository;

    @Override
    public Result<SubstituteEntity> createSubstitute(SubstituteEntity substitute) {
        // 前端按物料编码提交时，解析为物料ID（数据库约束非空）
        Result<SubstituteEntity> resolveResult = resolveMaterialIds(substitute);
        if (resolveResult != null) {
            return resolveResult;
        }

        // 设置默认值
        if (substitute.getStatus() == null) {
            substitute.setStatus(1); // 默认启用
        }
        if (substitute.getRatio() == null) {
            substitute.setRatio(1.0); // 默认1:1替代
        }
        substitute.setCreatedTime(LocalDateTime.now());

        SubstituteEntity savedSubstitute = substituteRepository.save(substitute);
        return Result.success(enrich(savedSubstitute));
    }

    @Override
    public Result<SubstituteEntity> updateSubstitute(Long id, SubstituteEntity substitute) {
        SubstituteEntity existingSubstitute = substituteRepository.findById(id).orElse(null);
        if (existingSubstitute == null) {
            return Result.error("替代关系不存在");
        }

        // 前端按物料编码提交时，解析为物料ID（数据库约束非空）
        Result<SubstituteEntity> resolveResult = resolveMaterialIds(substitute);
        if (resolveResult != null) {
            return resolveResult;
        }

        substitute.setId(id);
        // 保留原创建时间，仅刷新更新时间，避免全量覆盖丢审计字段
        substitute.setCreatedTime(existingSubstitute.getCreatedTime());
        substitute.setUpdatedTime(LocalDateTime.now());
        SubstituteEntity updatedSubstitute = substituteRepository.save(substitute);
        return Result.success(enrich(updatedSubstitute));
    }

    /**
     * 解析主/替代料编码为物料ID，并按替代类型处理bomLineId。
     * 校验失败时返回错误Result，成功时返回null。
     */
    private Result<SubstituteEntity> resolveMaterialIds(SubstituteEntity substitute) {
        if (substitute.getMainMaterialId() == null && substitute.getMainMaterialCode() != null
                && !substitute.getMainMaterialCode().isBlank()) {
            MaterialEntity main = materialRepository.findFirstByMaterialCode(substitute.getMainMaterialCode().trim()).orElse(null);
            if (main == null) {
                return Result.error("主料不存在: " + substitute.getMainMaterialCode());
            }
            substitute.setMainMaterialId(main.getId());
        }
        if (substitute.getSubMaterialId() == null && substitute.getSubMaterialCode() != null
                && !substitute.getSubMaterialCode().isBlank()) {
            MaterialEntity sub = materialRepository.findFirstByMaterialCode(substitute.getSubMaterialCode().trim()).orElse(null);
            if (sub == null) {
                return Result.error("替代料不存在: " + substitute.getSubMaterialCode());
            }
            substitute.setSubMaterialId(sub.getId());
        }
        if (substitute.getMainMaterialId() == null) {
            return Result.error("主料不能为空");
        }
        if (substitute.getSubMaterialId() == null) {
            return Result.error("替代料不能为空");
        }
        // 替代类型为全局（1）时，不关联具体BOM行
        if (substitute.getSubstituteType() != null && substitute.getSubstituteType() == 1) {
            substitute.setBomLineId(null);
        }
        return null;
    }

    /**
     * 回填主/替代料的编码、名称、规格及替代类型，供前端展示。
     */
    private SubstituteEntity enrich(SubstituteEntity substitute) {
        if (substitute == null) {
            return null;
        }
        if (substitute.getMainMaterialId() != null) {
            MaterialEntity main = materialRepository.findById(substitute.getMainMaterialId()).orElse(null);
            if (main != null) {
                substitute.setMainMaterialCode(main.getMaterialCode());
                substitute.setMainMaterialName(main.getMaterialName());
                substitute.setMainMaterialSpec(main.getMaterialSpec());
            }
        }
        if (substitute.getSubMaterialId() != null) {
            MaterialEntity sub = materialRepository.findById(substitute.getSubMaterialId()).orElse(null);
            if (sub != null) {
                substitute.setSubMaterialCode(sub.getMaterialCode());
                substitute.setSubMaterialName(sub.getMaterialName());
                substitute.setSubMaterialSpec(sub.getMaterialSpec());
            }
        }
        // 推导替代类型：未关联BOM行为全局替代，否则为局部替代
        substitute.setSubstituteType(substitute.getBomLineId() == null ? 1 : 2);
        return substitute;
    }

    /**
     * 批量回填替代关系列表。
     */
    private List<SubstituteEntity> enrichAll(List<SubstituteEntity> substitutes) {
        if (substitutes != null) {
            substitutes.forEach(this::enrich);
        }
        return substitutes;
    }

    @Override
    public Result<Void> deleteSubstitute(Long id) {
        if (!substituteRepository.existsById(id)) {
            return Result.error("替代关系不存在");
        }
        
        substituteRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<SubstituteEntity> getSubstituteById(Long id) {
        SubstituteEntity substitute = substituteRepository.findById(id).orElse(null);
        if (substitute == null) {
            return Result.error("替代关系不存在");
        }
        return Result.success(enrich(substitute));
    }

    @Override
    public Result<List<SubstituteEntity>> getSubstitutesByMainMaterialId(Long mainMaterialId) {
        List<SubstituteEntity> substitutes = substituteRepository.findByMainMaterialId(mainMaterialId);
        List<SubstituteEntity> enriched = enrichAll(substitutes);
        // 应用启用的替代规则对替代料排序，使规则配置产生实际效果
        applySubstituteRules(enriched);
        return Result.success(enriched);
    }

    /**
     * 应用启用的替代规则对替代料列表进行排序。
     * 当前支持：
     * - 优先级策略（ruleType=1）：按策略配置排序，如"preferLowCost"低成本优先
     * - 无规则时按替代料自身priority字段升序排序
     *
     * @param substitutes 已enrich的替代料列表（会被原地排序）
     */
    private void applySubstituteRules(List<SubstituteEntity> substitutes) {
        if (substitutes == null || substitutes.isEmpty()) {
            return;
        }
        // 查询所有启用的优先级策略规则，按规则优先级升序（数字越小越优先）
        List<SubstituteRuleEntity> activeRules = ruleRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), 1));
            predicates.add(cb.equal(root.get("ruleType"), 1)); // 优先级策略
            return cb.and(predicates.toArray(new Predicate[0]));
        });
        activeRules.sort(Comparator.comparingInt(r -> r.getPriority() == null ? 99 : r.getPriority()));

        boolean ruleApplied = false;
        for (SubstituteRuleEntity rule : activeRules) {
            String config = rule.getStrategyConfig();
            if (config != null && config.contains("\"preferLowCost\":true")) {
                // 低成本优先：按替代料单价升序排序，单价低的排前面
                substitutes.sort(Comparator.comparing(
                    s -> getMaterialPrice(s.getSubMaterialId()),
                    Comparator.nullsLast(Comparator.naturalOrder())
                ));
                ruleApplied = true;
                break; // 只应用第一个匹配的低成本优先规则
            }
        }

        if (!ruleApplied) {
            // 没有匹配的规则，按替代料自身priority字段升序排序
            substitutes.sort(Comparator.comparingInt(s -> s.getPriority() == null ? 99 : s.getPriority()));
        }
    }

    /**
     * 获取物料单价，用于替代规则的成本比较。
     * @param materialId 物料ID
     * @return 物料单价，不存在时返回null
     */
    private BigDecimal getMaterialPrice(Long materialId) {
        if (materialId == null) {
            return null;
        }
        return materialRepository.findById(materialId)
                .map(MaterialEntity::getUnitPrice)
                .orElse(null);
    }

    @Override
    public Result<List<SubstituteEntity>> getSubstitutesByBomLineId(Long bomLineId) {
        List<SubstituteEntity> substitutes = substituteRepository.findByBomLineId(bomLineId);
        return Result.success(enrichAll(substitutes));
    }

    @Override
    public Result<List<SubstituteEntity>> getSubstitutesByMaterialAndBomLine(Long mainMaterialId, Long bomLineId) {
        List<SubstituteEntity> substitutes = substituteRepository.findByMainMaterialIdAndBomLineId(mainMaterialId, bomLineId);
        return Result.success(enrichAll(substitutes));
    }

    @Override
    public Result<PageResult<SubstituteEntity>> getSubstitutesPage(
            Long mainMaterialId,
            String mainMaterialCode,
            String subMaterialCode,
            Long bomLineId,
            Integer status,
            Integer substituteType,
            Integer page,
            Integer size) {
        int safePage = page == null || page <= 0 ? 0 : page - 1;
        int safeSize = size == null || size <= 0 ? 10 : size;

        Long resolvedMainMaterialId = mainMaterialId;
        if (resolvedMainMaterialId == null && mainMaterialCode != null && !mainMaterialCode.isBlank()) {
            MaterialEntity m = materialRepository.findFirstByMaterialCode(mainMaterialCode.trim()).orElse(null);
            resolvedMainMaterialId = m == null ? null : m.getId();
        }

        Long resolvedSubMaterialId = null;
        if (subMaterialCode != null && !subMaterialCode.isBlank()) {
            MaterialEntity m = materialRepository.findFirstByMaterialCode(subMaterialCode.trim()).orElse(null);
            resolvedSubMaterialId = m == null ? null : m.getId();
        }

        if ((mainMaterialCode != null && !mainMaterialCode.isBlank() && resolvedMainMaterialId == null)
                || (subMaterialCode != null && !subMaterialCode.isBlank() && resolvedSubMaterialId == null)) {
            return Result.success(PageResult.build(0L, safeSize, safePage + 1, List.of()));
        }

        Long finalMainMaterialId = resolvedMainMaterialId;
        Long finalSubMaterialId = resolvedSubMaterialId;
        Specification<SubstituteEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            if (finalMainMaterialId != null) {
                predicates.add(cb.equal(root.get("mainMaterialId"), finalMainMaterialId));
            }
            if (finalSubMaterialId != null) {
                predicates.add(cb.equal(root.get("subMaterialId"), finalSubMaterialId));
            }
            if (bomLineId != null) {
                predicates.add(cb.equal(root.get("bomLineId"), bomLineId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            // 替代类型过滤：1-全局（bomLineId为空） 2-局部（bomLineId非空）
            if (substituteType != null) {
                if (substituteType == 1) {
                    predicates.add(cb.isNull(root.get("bomLineId")));
                } else if (substituteType == 2) {
                    predicates.add(cb.isNotNull(root.get("bomLineId")));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<SubstituteEntity> result = substituteRepository.findAll(spec, PageRequest.of(safePage, safeSize));
        PageResult<SubstituteEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                enrichAll(result.getContent())
        );
        return Result.success(pageResult);
    }

    @Override
    public Result<SubstituteEntity> enableSubstitute(Long id) {
        SubstituteEntity substitute = substituteRepository.findById(id).orElse(null);
        if (substitute == null) {
            return Result.error("替代关系不存在");
        }
        
        substitute.setStatus(1); // 启用
        substitute.setUpdatedTime(LocalDateTime.now());
        SubstituteEntity updatedSubstitute = substituteRepository.save(substitute);
        return Result.success(enrich(updatedSubstitute));
    }

    @Override
    public Result<SubstituteEntity> disableSubstitute(Long id) {
        SubstituteEntity substitute = substituteRepository.findById(id).orElse(null);
        if (substitute == null) {
            return Result.error("替代关系不存在");
        }

        substitute.setStatus(0); // 禁用
        substitute.setUpdatedTime(LocalDateTime.now());
        SubstituteEntity updatedSubstitute = substituteRepository.save(substitute);
        return Result.success(enrich(updatedSubstitute));
    }
}
