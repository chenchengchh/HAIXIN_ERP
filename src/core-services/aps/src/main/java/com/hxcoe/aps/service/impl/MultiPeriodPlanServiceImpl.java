package com.hxcoe.aps.service.impl;

import com.hxcoe.aps.entity.MultiPeriodPlanEntity;
import com.hxcoe.aps.entity.MultiPeriodPlanItemEntity;
import com.hxcoe.aps.entity.ProductionPlanEntity;
import com.hxcoe.aps.repository.MultiPeriodPlanItemRepository;
import com.hxcoe.aps.repository.MultiPeriodPlanRepository;
import com.hxcoe.aps.repository.ProductionPlanRepository;
import com.hxcoe.aps.service.MultiPeriodPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 多周期计划服务实现
 */
@Service
public class MultiPeriodPlanServiceImpl implements MultiPeriodPlanService {

    private static final Logger logger = LoggerFactory.getLogger(MultiPeriodPlanServiceImpl.class);

    @Autowired
    private MultiPeriodPlanRepository multiPeriodPlanRepository;

    @Autowired
    private MultiPeriodPlanItemRepository multiPeriodPlanItemRepository;

    @Autowired
    private ProductionPlanRepository productionPlanRepository;

    @Override
    public List<MultiPeriodPlanEntity> getAllPlans() {
        return multiPeriodPlanRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedTime().compareTo(a.getCreatedTime()))
                .toList();
    }

    @Override
    public MultiPeriodPlanEntity getPlanById(Long id) {
        return multiPeriodPlanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("多周期计划不存在: " + id));
    }

    @Override
    public List<MultiPeriodPlanItemEntity> getPlanItems(Long planId) {
        return multiPeriodPlanItemRepository.findByPlanIdOrderByPeriodIndexAsc(planId);
    }

    /**
     * 生成多周期计划：校验源计划 -> 计算周期边界 -> 均分数量 -> 事务保存主表与明细
     */
    @Override
    @Transactional
    public Map<String, Object> generatePlan(Long sourcePlanId, String periodType, Integer periodCount,
                                            LocalDate startDate, String algorithm, List<String> objectives) {
        // 1. 参数校验
        ProductionPlanEntity sourcePlan = productionPlanRepository.findById(sourcePlanId)
                .orElseThrow(() -> new RuntimeException("源生产计划不存在: " + sourcePlanId));
        if (periodCount == null || periodCount < 1 || periodCount > 12) {
            throw new RuntimeException("周期数量必须在1-12之间");
        }
        if (startDate == null) {
            throw new RuntimeException("开始日期不能为空");
        }
        String normalizedType = periodType != null ? periodType.toLowerCase() : "weekly";

        // 2. 计算各周期边界并均分数量（余数归入最后一个周期）
        BigDecimal totalQty = sourcePlan.getQuantity();
        BigDecimal baseQty = totalQty != null
                ? totalQty.divide(new BigDecimal(periodCount), 0, RoundingMode.FLOOR)
                : null;
        LocalDate periodStart = startDate;
        LocalDate lastEnd = startDate;
        List<MultiPeriodPlanItemEntity> items = new ArrayList<>();
        for (int i = 1; i <= periodCount; i++) {
            LocalDate periodEnd = computePeriodEnd(periodStart, normalizedType);
            BigDecimal qty = baseQty;
            if (totalQty != null && i == periodCount) {
                // 最后一个周期 = 总量 - 前面周期已分配量，避免精度丢失
                qty = totalQty.subtract(baseQty.multiply(new BigDecimal(periodCount - 1)));
            }
            // 生产数量为整数件数，统一去除小数位，保证序列化输出一致
            if (qty != null) {
                qty = qty.setScale(0, RoundingMode.FLOOR);
            }
            items.add(MultiPeriodPlanItemEntity.builder()
                    .periodIndex(i)
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .quantity(qty)
                    .status("已生成")
                    .build());
            lastEnd = periodEnd;
            periodStart = periodEnd.plusDays(1);
        }

        // 3. 保存主计划
        MultiPeriodPlanEntity plan = MultiPeriodPlanEntity.builder()
                .planNo(generatePlanNo())
                .sourcePlanId(sourcePlan.getId())
                .sourcePlanNo(sourcePlan.getPlanNo())
                .planName(sourcePlan.getPlanName() + "-多周期拆分")
                .periodType(normalizedType)
                .periodCount(periodCount)
                .startDate(startDate)
                .endDate(lastEnd)
                .algorithm(algorithm)
                .status("已生成")
                .remark(objectives != null && !objectives.isEmpty()
                        ? "优化目标: " + String.join("、", objectives) : null)
                .build();
        MultiPeriodPlanEntity saved = multiPeriodPlanRepository.save(plan);

        // 4. 保存明细
        for (MultiPeriodPlanItemEntity item : items) {
            item.setPlanId(saved.getId());
        }
        List<MultiPeriodPlanItemEntity> savedItems = multiPeriodPlanItemRepository.saveAll(items);

        logger.info("生成多周期计划成功: planNo={}, sourcePlanId={}, periodType={}, periodCount={}",
                saved.getPlanNo(), sourcePlanId, normalizedType, periodCount);

        Map<String, Object> result = new HashMap<>();
        result.put("plan", saved);
        result.put("items", savedItems);
        return result;
    }

    /**
     * 计算周期结束日期：daily=当天，weekly=+6天，monthly=+1月-1天，quarterly=+3月-1天
     */
    private LocalDate computePeriodEnd(LocalDate periodStart, String periodType) {
        return switch (periodType) {
            case "daily" -> periodStart;
            case "monthly" -> periodStart.plusMonths(1).minusDays(1);
            case "quarterly" -> periodStart.plusMonths(3).minusDays(1);
            default -> periodStart.plusDays(6); // weekly默认
        };
    }

    /**
     * 生成计划编号：MP-yyyyMMdd-NNN（当日序号递增，冲突时继续累加）
     */
    private String generatePlanNo() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = 1;
        String planNo;
        do {
            planNo = String.format("MP-%s-%03d", datePart, seq++);
        } while (multiPeriodPlanRepository.findByPlanNo(planNo) != null);
        return planNo;
    }

    /**
     * 删除多周期计划：先删明细再删主表，同一事务保证一致性
     */
    @Override
    @Transactional
    public void deletePlan(Long id) {
        if (!multiPeriodPlanRepository.existsById(id)) {
            throw new RuntimeException("多周期计划不存在: " + id);
        }
        multiPeriodPlanItemRepository.deleteByPlanId(id);
        multiPeriodPlanRepository.deleteById(id);
        logger.info("删除多周期计划成功: id={}", id);
    }
}
