package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.SalesForecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 销售预测数据访问接口
 */
@Repository
public interface SalesForecastRepository extends JpaRepository<SalesForecast, Long> {

    /**
     * 根据周期查询销售预测列表
     * @param period 周期（如 2026-Q1 或 2026-07）
     * @return 销售预测列表
     */
    List<SalesForecast> findByPeriod(String period);
}
