package com.hxcoe.les.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.les.dto.LesLogisticsAnalysisDto;
import com.hxcoe.les.dto.LesTransportStatsDto;
import com.hxcoe.les.entity.LesServiceQualityEntity;
import com.hxcoe.les.entity.LesTransportCostEntity;

public interface LesAnalysisService {

    /**
     * 分页查询运输成本
     *
     * @param page   页码（从1开始）
     * @param size   每页条数
     * @param planId 计划ID（可选）
     * @return 分页结果
     */
    Result<PageResult<LesTransportCostEntity>> fetchTransportCosts(int page, int size, Long planId);

    /**
     * 查询计划最新成本
     *
     * @param planId 计划ID
     * @return 成本数据
     */
    Result<LesTransportCostEntity> getPlanCost(Long planId);

    /**
     * 获取物流分析数据（按计划输出）
     *
     * @param page      页码（从1开始）
     * @param size      每页条数
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 分页结果
     */
    Result<PageResult<LesLogisticsAnalysisDto>> fetchLogisticsAnalysis(int page, int size, String startDate, String endDate);

    /**
     * 分页查询服务质量评估列表
     *
     * @param page      页码（从1开始）
     * @param size      每页条数
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 分页结果
     */
    Result<PageResult<LesServiceQualityEntity>> fetchServiceQualities(int page, int size, String startDate, String endDate);

    /**
     * 获取运输统计数据
     *
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 统计结果
     */
    Result<LesTransportStatsDto> fetchTransportStats(String startDate, String endDate);

    /**
     * 获取运输效率分析（首版输出聚合对象）
     *
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 分析结果
     */
    Result<Object> fetchEfficiencyAnalysis(String startDate, String endDate);

    /**
     * 获取资源利用率分析（首版输出聚合对象）
     *
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 分析结果
     */
    Result<Object> fetchUtilizationAnalysis(String startDate, String endDate);

    /**
     * 获取异常事件分析（首版输出聚合对象）
     *
     * @param startDate 开始日期（可选）
     * @param endDate   结束日期（可选）
     * @return 分析结果
     */
    Result<Object> fetchAnomalyAnalysis(String startDate, String endDate);
}

