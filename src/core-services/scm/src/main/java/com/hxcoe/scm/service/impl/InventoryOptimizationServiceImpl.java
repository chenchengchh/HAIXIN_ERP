package com.hxcoe.scm.service.impl;

import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.WmsClient;
import com.hxcoe.scm.client.dto.WmsInventoryDTO;
import com.hxcoe.scm.client.SrmClient;
import com.hxcoe.scm.client.dto.srm.PurchaseRequestDTO;
import com.hxcoe.scm.entity.DemandForecastEntity;
import com.hxcoe.scm.entity.InventoryStrategyEntity;
import com.hxcoe.scm.repository.ForecastRepository;
import com.hxcoe.scm.repository.InventoryStrategyRepository;
import com.hxcoe.scm.service.InventoryOptimizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class InventoryOptimizationServiceImpl implements InventoryOptimizationService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryOptimizationServiceImpl.class);

    @Autowired
    private InventoryStrategyRepository strategyRepository;

    @Autowired
    private ForecastRepository forecastRepository;

    @Autowired
    private WmsClient wmsClient;

    @Autowired
    private SrmClient srmClient;

    @Transactional
    @Override
    public void calculateAllStrategies() {
        List<DemandForecastEntity> forecasts = forecastRepository.findAll();
        List<String> materialCodes = forecasts.stream()
                .map(DemandForecastEntity::getProductCode)
                .distinct()
                .collect(Collectors.toList());

        for (String code : materialCodes) {
            Optional<DemandForecastEntity> forecastOpt = forecasts.stream()
                    .filter(f -> f.getProductCode().equals(code))
                    .findFirst();
            
            if (forecastOpt.isPresent()) {
                DemandForecastEntity forecast = forecastOpt.get();
                InventoryStrategyEntity strategy = strategyRepository.findByMaterialCode(code)
                        .orElse(new InventoryStrategyEntity());
                
                strategy.setMaterialCode(code);
                strategy.setMaterialName(forecast.getProductName());
                
                if (strategy.getServiceLevelTarget() == null) {
                    strategy.setServiceLevelTarget(new BigDecimal("0.95")); // 默认95%
                }
                
                // 1. 获取库存流水计算需求波动 (标准差)
                BigDecimal dailyDemand = forecast.getFinalForecast().divide(new BigDecimal("30"), MathContext.DECIMAL32);
                BigDecimal standardDeviation = BigDecimal.ZERO;
                
                try {
                    // 获取过去30天的库存流水
                    Result<List<Map<String, Object>>> txResult = wmsClient.getTransactions(code, 30);
                    if (isRemoteSuccess(txResult) && txResult.getData() != null && !txResult.getData().isEmpty()) {
                        // 简单计算：基于出库数量计算日均出库的标准差
                        // 这里简化为：直接使用出库记录作为样本
                        List<BigDecimal> quantities = new ArrayList<>();
                        for (Map<String, Object> tx : txResult.getData()) {
                            if ("OUT".equals(String.valueOf(tx.get("type")))) {
                                Object qtyObj = tx.get("quantity");
                                if (qtyObj instanceof Number) {
                                    quantities.add(new BigDecimal(qtyObj.toString()));
                                }
                            }
                        }
                        
                        if (!quantities.isEmpty()) {
                            // 计算平均值
                            BigDecimal sum = quantities.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                            BigDecimal avg = sum.divide(new BigDecimal(quantities.size()), MathContext.DECIMAL32);
                            
                            // 计算方差
                            BigDecimal variance = BigDecimal.ZERO;
                            for (BigDecimal q : quantities) {
                                variance = variance.add(q.subtract(avg).pow(2));
                            }
                            variance = variance.divide(new BigDecimal(quantities.size()), MathContext.DECIMAL32);
                            
                            // 标准差
                            standardDeviation = variance.sqrt(MathContext.DECIMAL32);
                            logger.info("物料 {} 基于WMS流水计算的标准差: {}", code, standardDeviation);
                        }
                    }
                } catch (Exception e) {
                    logger.error("获取WMS库存流水失败，使用默认波动率: {}", e.getMessage());
                    // 降级：假设波动率为需求量的 20%
                    standardDeviation = dailyDemand.multiply(new BigDecimal("0.2"));
                }
                
                if (standardDeviation.compareTo(BigDecimal.ZERO) == 0) {
                     standardDeviation = dailyDemand.multiply(new BigDecimal("0.2"));
                }

                // 2. 提前期 (假设7天)
                BigDecimal leadTime = new BigDecimal("7");
                
                // 3. 安全库存 = Z * sigma * sqrt(L)
                // Z值：95%对应1.65，99%对应2.33
                // 简化：使用 Z=2
                BigDecimal zScore = new BigDecimal("2"); 
                if (strategy.getServiceLevelTarget().compareTo(new BigDecimal("0.98")) >= 0) {
                    zScore = new BigDecimal("2.33");
                } else if (strategy.getServiceLevelTarget().compareTo(new BigDecimal("0.90")) < 0) {
                    zScore = new BigDecimal("1.28");
                }
                
                // SS = Z * stdDev * sqrt(LeadTime)
                BigDecimal safetyStock = zScore.multiply(standardDeviation).multiply(leadTime.sqrt(MathContext.DECIMAL32));
                strategy.setSafetyStock(safetyStock);
                
                // 4. 再订货点 = 日均需求 * 提前期 + 安全库存
                BigDecimal reorderPoint = dailyDemand.multiply(leadTime).add(safetyStock);
                strategy.setReorderPoint(reorderPoint);
                
                // 5. EOQ = sqrt(2 * D * S / H)
                // 简化：固定值
                strategy.setEoq(new BigDecimal("1000"));
                
                // 6. ABC分类 (简化)
                strategy.setAbcClass("A");
                if (strategy.getStatus() == null || strategy.getStatus().isBlank()) {
                    strategy.setStatus("DRAFT");
                }
                strategy.setUpdateTime(LocalDateTime.now());
                
                strategyRepository.save(strategy);
            }
        }
        logger.info("库存策略计算完成，共处理 {} 个物料", materialCodes.size());
    }

    @Override
    public PageResult<InventoryStrategyEntity> getStrategies(String materialCode, String abcClass, Pageable pageable) {
        Specification<InventoryStrategyEntity> spec = (root, query, cb) -> {
            if (materialCode != null && !materialCode.isEmpty()) {
                return cb.like(root.get("materialCode"), "%" + materialCode + "%");
            }
            if (abcClass != null && !abcClass.isEmpty()) {
                return cb.equal(root.get("abcClass"), abcClass);
            }
            return null;
        };
        
        Page<InventoryStrategyEntity> page = strategyRepository.findAll(spec, pageable);
        
        return PageResult.build(
            page.getTotalElements(),
            page.getSize(),
            page.getNumber() + 1,
            page.getContent()
        );
    }

    @Transactional
    @Override
    public InventoryStrategyEntity updateStrategy(Long id, InventoryStrategyEntity strategy) {
        Optional<InventoryStrategyEntity> existing = strategyRepository.findById(id);
        if (existing.isPresent()) {
            InventoryStrategyEntity entity = existing.get();
            entity.setServiceLevelTarget(strategy.getServiceLevelTarget());
            if (strategy.getSafetyStock() != null) entity.setSafetyStock(strategy.getSafetyStock());
            if (strategy.getReorderPoint() != null) entity.setReorderPoint(strategy.getReorderPoint());
            if (strategy.getEoq() != null) entity.setEoq(strategy.getEoq());
            if (strategy.getMinStock() != null) entity.setMinStock(strategy.getMinStock());
            if (strategy.getMaxStock() != null) entity.setMaxStock(strategy.getMaxStock());
            entity.setUpdateTime(LocalDateTime.now());
            return strategyRepository.save(entity);
        }
        return null;
    }

    @Transactional
    @Override
    public InventoryStrategyEntity createStrategy(InventoryStrategyEntity strategy) {
        if (strategy == null) return null;
        if (strategy.getStatus() == null || strategy.getStatus().isBlank()) {
            strategy.setStatus("DRAFT");
        }
        Optional<InventoryStrategyEntity> existing = strategy.getMaterialCode() == null ? Optional.empty() : strategyRepository.findByMaterialCode(strategy.getMaterialCode());
        if (existing.isPresent()) {
            InventoryStrategyEntity entity = existing.get();
            if (strategy.getMaterialName() != null) entity.setMaterialName(strategy.getMaterialName());
            if (strategy.getAbcClass() != null) entity.setAbcClass(strategy.getAbcClass());
            if (strategy.getSafetyStock() != null) entity.setSafetyStock(strategy.getSafetyStock());
            if (strategy.getReorderPoint() != null) entity.setReorderPoint(strategy.getReorderPoint());
            if (strategy.getEoq() != null) entity.setEoq(strategy.getEoq());
            if (strategy.getMinStock() != null) entity.setMinStock(strategy.getMinStock());
            if (strategy.getMaxStock() != null) entity.setMaxStock(strategy.getMaxStock());
            if (strategy.getServiceLevelTarget() != null) entity.setServiceLevelTarget(strategy.getServiceLevelTarget());
            return strategyRepository.save(entity);
        }
        return strategyRepository.save(strategy);
    }

    @Override
    public InventoryStrategyEntity getStrategy(Long id) {
        return strategyRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public void deleteStrategy(Long id) {
        strategyRepository.deleteById(id);
    }

    @Transactional
    @Override
    public InventoryStrategyEntity publishStrategy(Long id) {
        InventoryStrategyEntity entity = strategyRepository.findById(id).orElse(null);
        if (entity == null) return null;
        entity.setStatus("PUBLISHED");
        return strategyRepository.save(entity);
    }

    @Transactional
    @Override
    public InventoryStrategyEntity disableStrategy(Long id) {
        InventoryStrategyEntity entity = strategyRepository.findById(id).orElse(null);
        if (entity == null) return null;
        entity.setStatus("DISABLED");
        return strategyRepository.save(entity);
    }

    @Transactional
    @Override
    public List<Map<String, Object>> generateReplenishments() {
        List<InventoryStrategyEntity> strategies = strategyRepository.findByStatus("PUBLISHED");
        List<Map<String, Object>> results = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (InventoryStrategyEntity strategy : strategies) {
            BigDecimal reorderPoint = strategy.getReorderPoint() == null ? BigDecimal.ZERO : strategy.getReorderPoint();
            if (reorderPoint.compareTo(BigDecimal.ZERO) <= 0) continue;

            BigDecimal currentStock = BigDecimal.ZERO;
            try {
                Result<List<WmsInventoryDTO>> invRes = wmsClient.getInventoryByMaterialCode(strategy.getMaterialCode());
                if (isRemoteSuccess(invRes) && invRes.getData() != null) {
                    currentStock = invRes.getData().stream()
                            .map(WmsInventoryDTO::getQuantity)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                }
            } catch (Exception e) {
                currentStock = BigDecimal.ZERO;
            }

            if (currentStock.compareTo(reorderPoint) >= 0) continue;

            BigDecimal eoq = strategy.getEoq() == null ? BigDecimal.ZERO : strategy.getEoq();
            BigDecimal needed = reorderPoint.subtract(currentStock);
            BigDecimal qty = eoq.compareTo(needed) > 0 ? eoq : needed;
            if (qty.compareTo(BigDecimal.ZERO) <= 0) continue;

            String requestCode = "ROP-" + today + "-" + strategy.getMaterialCode();

            Map<String, Object> row = new HashMap<>();
            row.put("materialCode", strategy.getMaterialCode());
            row.put("materialName", strategy.getMaterialName());
            row.put("currentStock", currentStock);
            row.put("reorderPoint", reorderPoint);
            row.put("recommendedQty", qty);
            row.put("purchaseRequestCode", requestCode);

            try {
                PurchaseRequestDTO req = new PurchaseRequestDTO();
                req.setRequestCode(requestCode);
                req.setApplicant("SCM-SYSTEM");
                req.setDepartment("INVENTORY");
                req.setApplyDate(LocalDateTime.now());
                req.setStatus("PENDING");
                req.setPurchaseType("REPLENISH");
                req.setDescription("库存补货建议: " + strategy.getMaterialName() + " x " + qty);
                req.setExpectedDeliveryDate(LocalDateTime.now().plusDays(7));
                req.setMaterialCode(strategy.getMaterialCode());
                req.setMaterialName(strategy.getMaterialName());
                req.setQuantity(qty);
                req.setUnit("PCS");

                Result<PurchaseRequestDTO> created = srmClient.createPurchaseRequest(req);
                row.put("srmCode", created == null ? null : created.getCode());
                row.put("srmMessage", created == null ? "SRM返回为空" : created.getMessage());
            } catch (Exception e) {
                row.put("srmCode", -1);
                row.put("srmMessage", e.getMessage());
            }

            results.add(row);
        }

        return results;
    }

    private boolean isRemoteSuccess(Result<?> result) {
        return result != null && ResponseStatusAdapter.isSuccess(result.getCode());
    }
}
