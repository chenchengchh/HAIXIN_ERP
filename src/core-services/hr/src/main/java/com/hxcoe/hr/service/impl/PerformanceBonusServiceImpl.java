package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.PerformanceBonusEntity;
import com.hxcoe.hr.repository.PerformanceBonusRepository;
import com.hxcoe.hr.service.PerformanceBonusService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 绩效奖金Service实现类
 */
@Service
public class PerformanceBonusServiceImpl implements PerformanceBonusService {

    @Autowired
    private PerformanceBonusRepository performanceBonusRepository;

    @Override
    public PerformanceBonusEntity createBonus(PerformanceBonusEntity bonus) {
        return performanceBonusRepository.save(bonus);
    }

    @Override
    public PerformanceBonusEntity getBonusById(Long id) {
        return performanceBonusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("绩效奖金记录不存在: " + id));
    }

    @Override
    @Transactional
    public PerformanceBonusEntity updateBonus(Long id, PerformanceBonusEntity bonus) {
        PerformanceBonusEntity existingBonus = getBonusById(id);
        // 更新绩效奖金字段
        existingBonus.setEmployeeId(bonus.getEmployeeId());
        existingBonus.setAppraisalPeriod(bonus.getAppraisalPeriod());
        existingBonus.setPerformanceScore(bonus.getPerformanceScore());
        existingBonus.setPerformanceLevel(bonus.getPerformanceLevel());
        existingBonus.setBonusAmount(bonus.getBonusAmount());
        existingBonus.setStatus(bonus.getStatus());
        existingBonus.setEvaluatorId(bonus.getEvaluatorId());
        existingBonus.setRemark(bonus.getRemark());
        return performanceBonusRepository.save(existingBonus);
    }

    @Override
    public void deleteBonus(Long id) {
        PerformanceBonusEntity existingBonus = getBonusById(id);
        performanceBonusRepository.delete(existingBonus);
    }

    @Override
    public List<PerformanceBonusEntity> getAllBonuses() {
        return performanceBonusRepository.findAll();
    }

    @Override
    public Page<PerformanceBonusEntity> getBonusesByPage(String appraisalPeriod, Pageable pageable) {
        // 考核周期为空时查询全部，否则按考核周期过滤
        Specification<PerformanceBonusEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (appraisalPeriod != null && !appraisalPeriod.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("appraisalPeriod"), appraisalPeriod));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return performanceBonusRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public PerformanceBonusEntity approve(Long id, Integer status) {
        PerformanceBonusEntity existingBonus = getBonusById(id);
        // 更新审批状态
        existingBonus.setStatus(status);
        return performanceBonusRepository.save(existingBonus);
    }
}
