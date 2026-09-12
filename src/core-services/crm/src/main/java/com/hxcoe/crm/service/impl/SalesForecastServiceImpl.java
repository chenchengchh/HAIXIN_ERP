package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.SalesForecast;
import com.hxcoe.crm.repository.SalesForecastRepository;
import com.hxcoe.crm.service.SalesForecastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 销售预测服务实现类
 */
@Service
public class SalesForecastServiceImpl implements SalesForecastService {

    @Autowired
    private SalesForecastRepository salesForecastRepository;

    /**
     * 按周期查询销售预测列表
     * @param period 周期（如 2026-Q1 或 2026-07）
     * @return 销售预测列表
     */
    @Override
    public List<SalesForecast> getForecastByPeriod(String period) {
        return salesForecastRepository.findByPeriod(period);
    }

    /**
     * 创建销售预测
     * @param forecast 预测数据
     * @return 创建后的预测记录
     */
    @Override
    public SalesForecast createForecast(SalesForecast forecast) {
        return salesForecastRepository.save(forecast);
    }
}
