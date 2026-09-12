package com.hxcoe.scm.service;

import com.hxcoe.scm.entity.DemandForecastEntity;
import com.hxcoe.scm.entity.ForecastVersionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public interface ForecastService {
    
    /**
     * 生成预测数据 (基于模拟算法)
     */
    List<DemandForecastEntity> generateForecast(String period);

    /**
     * 分页查询预测数据
     */
    Page<DemandForecastEntity> getForecastList(Specification<DemandForecastEntity> spec, Pageable pageable);

    /**
     * 更新预测调整
     */
    DemandForecastEntity updateForecast(Long id, DemandForecastEntity forecast);

    /**
     * 批量保存预测
     */
    void saveForecasts(List<DemandForecastEntity> forecasts);

    List<ForecastVersionEntity> getForecastVersions(String period);

    ForecastVersionEntity createForecastVersion(String period, Long fromVersionId);

    ForecastVersionEntity publishForecastVersion(Long versionId);

    ForecastVersionEntity rollbackForecastVersion(Long versionId);

    List<DemandForecastEntity> updateForecastsByVersion(Long versionId, List<DemandForecastEntity> forecasts);

    Map<String, Object> getForecastAccuracy(String period, Long versionId);
}
