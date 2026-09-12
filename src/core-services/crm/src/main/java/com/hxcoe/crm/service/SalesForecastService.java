package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.SalesForecast;

import java.util.List;

/**
 * 销售预测服务接口
 */
public interface SalesForecastService {

    /**
     * 按周期查询销售预测列表
     * @param period 周期（如 2026-Q1 或 2026-07）
     * @return 销售预测列表
     */
    List<SalesForecast> getForecastByPeriod(String period);

    /**
     * 创建销售预测
     * @param forecast 预测数据
     * @return 创建后的预测记录
     */
    SalesForecast createForecast(SalesForecast forecast);
}
