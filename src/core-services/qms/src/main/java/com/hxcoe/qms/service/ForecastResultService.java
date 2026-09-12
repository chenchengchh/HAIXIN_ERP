package com.hxcoe.qms.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.ForecastResultEntity;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * 质量趋势预测服务接口
 */
public interface ForecastResultService {

    /**
     * 分页查询预测结果
     *
     * @param forecastType 预测类型
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Result<PageResult<ForecastResultEntity>> page(String forecastType, String status, Pageable pageable);

    /**
     * 获取预测详情
     *
     * @param id 预测ID
     * @return 预测详情
     */
    Result<ForecastResultEntity> getById(Long id);

    /**
     * 生成预测结果
     *
     * @param params 预测参数
     * @return 生成结果
     */
    Result<ForecastResultEntity> generate(Map<String, Object> params);

    /**
     * 获取预测图表数据
     *
     * @param id 预测ID
     * @return 图表数据
     */
    Result<Map<String, Object>> getChartData(Long id);
}
