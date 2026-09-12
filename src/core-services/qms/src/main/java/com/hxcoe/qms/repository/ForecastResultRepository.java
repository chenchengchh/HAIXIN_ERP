package com.hxcoe.qms.repository;

import com.hxcoe.qms.entity.ForecastResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 质量趋势预测结果仓库
 */
public interface ForecastResultRepository extends JpaRepository<ForecastResultEntity, Long>, JpaSpecificationExecutor<ForecastResultEntity> {
}
