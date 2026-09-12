package com.hxcoe.scm.service.impl;

import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.CrmClient;
import com.hxcoe.scm.client.dto.crm.SalesOrderDTO;
import com.hxcoe.scm.entity.ForecastConfigEntity;
import com.hxcoe.scm.entity.ForecastDataSourceEntity;
import com.hxcoe.scm.repository.ForecastConfigRepository;
import com.hxcoe.scm.repository.ForecastDataSourceRepository;
import com.hxcoe.scm.service.ForecastConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForecastConfigServiceImpl implements ForecastConfigService {

    @Autowired
    private ForecastConfigRepository forecastConfigRepository;

    @Autowired
    private ForecastDataSourceRepository forecastDataSourceRepository;

    @Autowired
    private CrmClient crmClient;

    @Override
    public ForecastConfigEntity getConfig() {
        return forecastConfigRepository.findTopByEnabledOrderByUpdatedTimeDesc(1).orElseGet(() -> {
            ForecastConfigEntity cfg = new ForecastConfigEntity();
            cfg.setModelType("MOVING_AVERAGE");
            cfg.setHistoryMonths(12);
            cfg.setSmoothingAlpha(null);
            cfg.setEnabled(1);
            return forecastConfigRepository.save(cfg);
        });
    }

    @Transactional
    @Override
    public ForecastConfigEntity saveConfig(ForecastConfigEntity config) {
        if (config == null) {
            throw new IllegalArgumentException("config不能为空");
        }
        ForecastConfigEntity existing = getConfig();
        existing.setModelType(config.getModelType());
        existing.setHistoryMonths(config.getHistoryMonths());
        existing.setSmoothingAlpha(config.getSmoothingAlpha());
        existing.setEnabled(config.getEnabled());
        return forecastConfigRepository.save(existing);
    }

    @Override
    public List<ForecastDataSourceEntity> listDataSources() {
        return forecastDataSourceRepository.findAll();
    }

    @Transactional
    @Override
    public ForecastDataSourceEntity createDataSource(ForecastDataSourceEntity dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("dataSource不能为空");
        }
        return forecastDataSourceRepository.save(dataSource);
    }

    @Transactional
    @Override
    public ForecastDataSourceEntity updateDataSource(Long id, ForecastDataSourceEntity dataSource) {
        ForecastDataSourceEntity existing = forecastDataSourceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("数据源不存在"));
        existing.setName(dataSource.getName());
        existing.setSourceType(dataSource.getSourceType());
        existing.setEnabled(dataSource.getEnabled());
        return forecastDataSourceRepository.save(existing);
    }

    @Transactional
    @Override
    public void deleteDataSource(Long id) {
        forecastDataSourceRepository.deleteById(id);
    }

    @Transactional
    @Override
    public ForecastDataSourceEntity testDataSource(Long id) {
        ForecastDataSourceEntity ds = forecastDataSourceRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("数据源不存在"));
        ds.setLastTestTime(LocalDateTime.now());
        try {
            if ("CRM".equalsIgnoreCase(ds.getSourceType())) {
                Result<PageResult<SalesOrderDTO>> res = crmClient.getSalesOrders(null, 1, 1);
                if (res != null && ResponseStatusAdapter.isSuccess(res.getCode())) {
                    ds.setStatus("OK");
                    ds.setLastError(null);
                } else {
                    ds.setStatus("FAILED");
                    ds.setLastError(res == null ? "CRM返回为空" : res.getMessage());
                }
            } else {
                ds.setStatus("UNKNOWN");
                ds.setLastError("暂不支持该数据源类型的连接测试");
            }
        } catch (Exception e) {
            ds.setStatus("FAILED");
            ds.setLastError(e.getMessage());
        }
        return forecastDataSourceRepository.save(ds);
    }

    @Override
    public Map<String, Object> testModel(String period) {
        Map<String, Object> result = new HashMap<>();
        ForecastConfigEntity cfg = getConfig();
        result.put("period", period);
        result.put("modelType", cfg.getModelType());
        result.put("historyMonths", cfg.getHistoryMonths());
        result.put("status", "OK");
        result.put("message", "模型测试接口已接入配置（预测生成会按配置取数计算）");
        return result;
    }
}

