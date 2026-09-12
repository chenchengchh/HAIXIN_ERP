package com.hxcoe.scm.service;

import com.hxcoe.scm.entity.MrpPlanEntity;
import com.hxcoe.scm.entity.MrpResultEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface MrpEngineService {

    /**
     * 运行MRP
     * @param params 运算参数 (包含运算名称、日期、需求来源等)
     * @return MRP计划实体
     */
    MrpPlanEntity runMrp(Map<String, Object> params);
    
    /**
     * 释放/下达MRP计划 (生成采购申请/生产订单)
     */
    void releasePlan(Long planId);

    /**
     * 获取MRP运算历史
     */
    Page<MrpPlanEntity> getMrpHistory(Pageable pageable);

    /**
     * 获取MRP运算结果详情
     */
    List<MrpResultEntity> getMrpResults(Long planId);

    Page<MrpResultEntity> queryMrpResults(Long planId, String status, String materialKeyword, String type, Pageable pageable);

    MrpResultEntity confirmMrpResult(Long resultId, String operator);

    MrpResultEntity rejectMrpResult(Long resultId, String reason, String operator);

    List<MrpResultEntity> batchConfirmMrpResults(List<Long> resultIds, String operator);

    List<MrpResultEntity> batchRejectMrpResults(List<Long> resultIds, String reason, String operator);

    /**
     * 删除MRP计划（连同关联的运算结果）
     * @param planId 计划ID
     * @return 是否删除成功
     */
    boolean deleteMrpPlan(Long planId);
}
