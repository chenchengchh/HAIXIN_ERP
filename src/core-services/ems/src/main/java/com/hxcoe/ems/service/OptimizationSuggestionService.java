package com.hxcoe.ems.service;

import com.hxcoe.ems.entity.OptimizationSuggestionEntity;
import com.hxcoe.ems.repository.OptimizationSuggestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 优化建议服务类
 * 实现优化建议相关业务逻辑
 *
 * @author author
 * @date 2026-01-01
 */
@Service
public class OptimizationSuggestionService {

    private final OptimizationSuggestionRepository optimizationSuggestionRepository;
    private static final Logger logger = LoggerFactory.getLogger(OptimizationSuggestionService.class);

    @Autowired
    public OptimizationSuggestionService(OptimizationSuggestionRepository optimizationSuggestionRepository) {
        this.optimizationSuggestionRepository = optimizationSuggestionRepository;
    }

    /**
     * 获取所有优化建议
     *
     * @return 优化建议列表
     */
    public List<OptimizationSuggestionEntity> getAllOptimizationSuggestions() {
        return optimizationSuggestionRepository.findAll();
    }

    /**
     * 根据ID获取优化建议
     *
     * @param id 建议ID
     * @return 优化建议
     */
    public Optional<OptimizationSuggestionEntity> getOptimizationSuggestionById(Long id) {
        return optimizationSuggestionRepository.findById(id);
    }

    /**
     * 保存优化建议
     *
     * @param optimizationSuggestion 优化建议
     * @return 保存后的优化建议
     */
    public OptimizationSuggestionEntity saveOptimizationSuggestion(OptimizationSuggestionEntity optimizationSuggestion) {
        return optimizationSuggestionRepository.save(optimizationSuggestion);
    }

    /**
     * 批量保存优化建议
     *
     * @param optimizationSuggestionList 优化建议列表
     * @return 保存后的优化建议列表
     */
    public List<OptimizationSuggestionEntity> saveAllOptimizationSuggestions(List<OptimizationSuggestionEntity> optimizationSuggestionList) {
        return optimizationSuggestionRepository.saveAll(optimizationSuggestionList);
    }

    /**
     * 根据条件查询优化建议
     *
     * @param status 状态
     * @param targetArea 目标区域
     * @return 优化建议列表
     */
    public List<OptimizationSuggestionEntity> getOptimizationSuggestionsByConditions(String status, String targetArea) {
        List<OptimizationSuggestionEntity> result = new ArrayList<>();

        try {
            // 获取所有数据
            List<OptimizationSuggestionEntity> allData = optimizationSuggestionRepository.findAll();
            
            if (allData != null && !allData.isEmpty()) {
                // 根据条件过滤
                if (status != null && targetArea != null) {
                    result = allData.stream()
                            .filter(data -> data != null && status.equals(data.getStatus()) && targetArea.equals(data.getTargetArea()))
                            .toList();
                } else if (status != null) {
                    result = allData.stream()
                            .filter(data -> data != null && status.equals(data.getStatus()))
                            .toList();
                } else if (targetArea != null) {
                    result = allData.stream()
                            .filter(data -> data != null && targetArea.equals(data.getTargetArea()))
                            .toList();
                } else {
                    result = allData;
                }
            }
        } catch (Exception e) {
            logger.error("Error occurred while getting optimization suggestions: {}", e.getMessage(), e);
            throw new RuntimeException("获取优化建议失败");
        }

        return result;
    }

    public Page<OptimizationSuggestionEntity> getOptimizationSuggestionsPage(String status, String targetArea, Pageable pageable) {
        String s = status == null || status.isBlank() ? null : status;
        String a = targetArea == null || targetArea.isBlank() ? null : targetArea;

        if (s != null && a != null) {
            return optimizationSuggestionRepository.findByStatusAndTargetArea(s, a, pageable);
        }
        if (s != null) {
            return optimizationSuggestionRepository.findByStatus(s, pageable);
        }
        if (a != null) {
            return optimizationSuggestionRepository.findByTargetArea(a, pageable);
        }
        return optimizationSuggestionRepository.findAll(pageable);
    }

    /**
     * 采纳优化建议
     *
     * @param id 建议ID
     * @return 采纳后的优化建议
     */
    public Optional<OptimizationSuggestionEntity> adoptOptimizationSuggestion(Long id) {
        Optional<OptimizationSuggestionEntity> suggestionOpt = optimizationSuggestionRepository.findById(id);
        if (suggestionOpt.isPresent()) {
            OptimizationSuggestionEntity suggestion = suggestionOpt.get();
            suggestion.setStatus("adopted");
            return Optional.of(optimizationSuggestionRepository.save(suggestion));
        }
        return Optional.empty();
    }

    /**
     * 删除优化建议
     *
     * @param id 建议ID
     */
    public void deleteOptimizationSuggestion(Long id) {
        optimizationSuggestionRepository.deleteById(id);
    }
}
