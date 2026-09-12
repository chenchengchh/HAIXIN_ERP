package com.hxcoe.scm.service;

import com.hxcoe.scm.entity.ForecastConfigEntity;
import com.hxcoe.scm.entity.ForecastDataSourceEntity;

import java.util.List;
import java.util.Map;

public interface ForecastConfigService {
    ForecastConfigEntity getConfig();

    ForecastConfigEntity saveConfig(ForecastConfigEntity config);

    List<ForecastDataSourceEntity> listDataSources();

    ForecastDataSourceEntity createDataSource(ForecastDataSourceEntity dataSource);

    ForecastDataSourceEntity updateDataSource(Long id, ForecastDataSourceEntity dataSource);

    void deleteDataSource(Long id);

    ForecastDataSourceEntity testDataSource(Long id);

    Map<String, Object> testModel(String period);
}

