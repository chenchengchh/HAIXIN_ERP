package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.PerformanceBonusEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 绩效奖金Service接口
 */
public interface PerformanceBonusService {

    /**
     * 创建绩效奖金
     * @param bonus 绩效奖金实体
     * @return 绩效奖金实体
     */
    PerformanceBonusEntity createBonus(PerformanceBonusEntity bonus);

    /**
     * 根据ID查询绩效奖金
     * @param id 绩效奖金ID
     * @return 绩效奖金实体
     */
    PerformanceBonusEntity getBonusById(Long id);

    /**
     * 更新绩效奖金
     * @param id 绩效奖金ID
     * @param bonus 绩效奖金实体
     * @return 绩效奖金实体
     */
    PerformanceBonusEntity updateBonus(Long id, PerformanceBonusEntity bonus);

    /**
     * 删除绩效奖金
     * @param id 绩效奖金ID
     */
    void deleteBonus(Long id);

    /**
     * 查询全部绩效奖金
     * @return 绩效奖金列表
     */
    List<PerformanceBonusEntity> getAllBonuses();

    /**
     * 分页查询绩效奖金（支持按考核周期过滤）
     * @param appraisalPeriod 考核周期（可空，为空时查询全部）
     * @param pageable 分页参数
     * @return 绩效奖金分页列表
     */
    Page<PerformanceBonusEntity> getBonusesByPage(String appraisalPeriod, Pageable pageable);

    /**
     * 审批绩效奖金（更新状态）
     * @param id 绩效奖金ID
     * @param status 状态（0待审批 1已批准 2已发放）
     * @return 更新后的绩效奖金实体
     */
    PerformanceBonusEntity approve(Long id, Integer status);
}
