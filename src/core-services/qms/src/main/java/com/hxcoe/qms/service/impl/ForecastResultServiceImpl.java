package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.ForecastResultEntity;
import com.hxcoe.qms.repository.ForecastResultRepository;
import com.hxcoe.qms.service.ForecastResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 质量趋势预测服务实现
 */
@Service
public class ForecastResultServiceImpl implements ForecastResultService {

    @Autowired
    private ForecastResultRepository forecastResultRepository;

    /**
     * 分页查询预测结果
     *
     * @param forecastType 预测类型
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<ForecastResultEntity>> page(String forecastType, String status, Pageable pageable) {
        Specification<ForecastResultEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (forecastType != null && !forecastType.isBlank()) {
                predicates.add(cb.equal(root.get("forecastType"), forecastType.trim()));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ForecastResultEntity> page = forecastResultRepository.findAll(specification, pageable);
        PageResult<ForecastResultEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    /**
     * 获取预测详情
     *
     * @param id 预测ID
     * @return 预测详情
     */
    @Override
    public Result<ForecastResultEntity> getById(Long id) {
        ForecastResultEntity entity = forecastResultRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("预测结果不存在");
        }
        return Result.success(entity);
    }

    /**
     * 生成预测结果
     *
     * @param params 预测参数
     * @return 生成结果
     */
    @Override
    public Result<ForecastResultEntity> generate(Map<String, Object> params) {
        ForecastResultEntity entity = new ForecastResultEntity();
        entity.setForecastType(params == null ? null : String.valueOf(params.getOrDefault("forecastType", "pass-rate")));
        entity.setPeriod("P" + System.currentTimeMillis());
        entity.setConfidenceInterval(params == null || params.get("confidenceInterval") == null
                ? 0.95
                : Double.parseDouble(String.valueOf(params.get("confidenceInterval"))));
        entity.setStatus("completed");
        entity.setCreateTime(LocalDateTime.now());

        List<Map<String, Object>> data = List.of(
                Map.of("x", 1, "y", 98.1),
                Map.of("x", 2, "y", 97.5),
                Map.of("x", 3, "y", 98.8)
        );
        entity.setForecastData(data);
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        ForecastResultEntity saved = forecastResultRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 获取预测图表数据
     *
     * @param id 预测ID
     * @return 图表数据
     */
    @Override
    public Result<Map<String, Object>> getChartData(Long id) {
        ForecastResultEntity entity = forecastResultRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("预测结果不存在");
        }
        Map<String, Object> chart = new HashMap<>();
        chart.put("forecastType", entity.getForecastType());
        chart.put("period", entity.getPeriod());
        chart.put("confidenceInterval", entity.getConfidenceInterval());
        chart.put("data", entity.getForecastData());
        return Result.success(chart);
    }
}
