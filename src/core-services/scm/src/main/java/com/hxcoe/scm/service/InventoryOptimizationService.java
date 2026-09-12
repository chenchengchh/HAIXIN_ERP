package com.hxcoe.scm.service;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.InventoryStrategyEntity;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface InventoryOptimizationService {
    
    /**
     * 计算并更新所有物料的库存策略
     */
    void calculateAllStrategies();

    /**
     * 获取策略列表
     */
    PageResult<InventoryStrategyEntity> getStrategies(String materialCode, String abcClass, Pageable pageable);

    /**
     * 更新单个策略参数
     */
    InventoryStrategyEntity updateStrategy(Long id, InventoryStrategyEntity strategy);

    InventoryStrategyEntity createStrategy(InventoryStrategyEntity strategy);

    InventoryStrategyEntity getStrategy(Long id);

    void deleteStrategy(Long id);

    InventoryStrategyEntity publishStrategy(Long id);

    InventoryStrategyEntity disableStrategy(Long id);

    List<Map<String, Object>> generateReplenishments();
}
