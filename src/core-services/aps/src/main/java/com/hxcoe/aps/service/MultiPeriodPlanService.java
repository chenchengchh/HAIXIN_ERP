package com.hxcoe.aps.service;

import com.hxcoe.aps.entity.MultiPeriodPlanEntity;
import com.hxcoe.aps.entity.MultiPeriodPlanItemEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 多周期计划服务接口
 */
public interface MultiPeriodPlanService {

    /**
     * 获取所有多周期计划（按创建时间倒序）
     */
    List<MultiPeriodPlanEntity> getAllPlans();

    /**
     * 根据ID查询多周期计划
     */
    MultiPeriodPlanEntity getPlanById(Long id);

    /**
     * 查询计划明细（周期片段列表）
     */
    List<MultiPeriodPlanItemEntity> getPlanItems(Long planId);

    /**
     * 生成多周期计划：将源生产计划的数量按周期类型均分到periodCount个周期
     *
     * @param sourcePlanId 源生产计划ID
     * @param periodType   周期类型（daily/weekly/monthly/quarterly）
     * @param periodCount  周期数量
     * @param startDate    开始日期
     * @param algorithm    排程算法
     * @param objectives   优化目标列表
     * @return 生成的多周期计划（含明细）
     */
    Map<String, Object> generatePlan(Long sourcePlanId, String periodType, Integer periodCount,
                                     LocalDate startDate, String algorithm, List<String> objectives);

    /**
     * 删除多周期计划及其明细
     */
    void deletePlan(Long id);
}
