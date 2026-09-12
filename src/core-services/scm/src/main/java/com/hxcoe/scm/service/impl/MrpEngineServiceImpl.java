package com.hxcoe.scm.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.ApsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hxcoe.scm.client.CrmClient;
import com.hxcoe.scm.client.dto.crm.SalesOrderDTO;
import com.hxcoe.scm.client.dto.crm.SalesOrderItemDTO;
import java.util.HashMap;

import com.hxcoe.scm.client.dto.srm.PurchaseRequestDTO;
import com.hxcoe.scm.client.dto.aps.ProductionPlanDTO;

import com.hxcoe.scm.service.MrpEngineService;
import com.hxcoe.scm.service.BomService;
import com.hxcoe.scm.entity.MrpPlanEntity;
import com.hxcoe.scm.entity.MrpResultEntity;
import com.hxcoe.scm.entity.DemandForecastEntity;
import com.hxcoe.scm.entity.IntegrationTaskEntity;
import com.hxcoe.scm.repository.MrpPlanRepository;
import com.hxcoe.scm.repository.MrpResultRepository;
import com.hxcoe.scm.repository.ForecastRepository;
import com.hxcoe.scm.repository.IntegrationTaskRepository;
import com.hxcoe.scm.client.SrmClient;
import com.hxcoe.scm.client.WmsClient;
import com.hxcoe.scm.client.dto.WmsInventoryDTO;
import com.hxcoe.common.result.PageResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

@Service
public class MrpEngineServiceImpl implements MrpEngineService {

    private static final Logger logger = LoggerFactory.getLogger(MrpEngineServiceImpl.class);

    @Autowired
    private MrpPlanRepository mrpPlanRepository;

    @Autowired
    private MrpResultRepository mrpResultRepository;

    @Autowired
    private ForecastRepository forecastRepository;

    @Autowired
    private SrmClient srmClient;

    @Autowired
    private ApsClient apsClient;

    @Autowired
    private CrmClient crmClient;

    @Autowired
    private WmsClient wmsClient;

    @Autowired
    private BomService bomService;

    @Autowired
    private IntegrationTaskRepository integrationTaskRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    @Override
    public void releasePlan(Long planId) {
        MrpPlanEntity plan = mrpPlanRepository.findById(planId).orElse(null);
        if (plan == null) return;
        
        List<MrpResultEntity> results = mrpResultRepository.findByPlanId(planId);
        
        for (MrpResultEntity result : results) {
            if ("CONFIRMED".equals(result.getStatus())) {
                try {
                    if ("PURCHASE".equals(result.getType())) {
                        PurchaseRequestDTO req = new PurchaseRequestDTO();
                        String requestCode = "MRP-PR-" + planId + "-" + result.getId();
                        req.setRequestCode(requestCode);
                        req.setApplicant("MRP-SYSTEM");
                        req.setDepartment("PLANNING");
                        req.setApplyDate(LocalDateTime.now());
                        req.setStatus("PENDING");
                        req.setPurchaseType("MRP");
                        req.setDescription("MRP建议: " + result.getMaterialName() + " x " + result.getQuantity());
                        req.setExpectedDeliveryDate(result.getRequiredDate() != null ? result.getRequiredDate().atStartOfDay() : null);
                        
                        req.setMaterialCode(result.getMaterialCode());
                        req.setMaterialName(result.getMaterialName());
                        req.setQuantity(result.getQuantity());
                        req.setUnit("PCS"); // 默认单位
                        IntegrationTaskEntity task = upsertTask(planId, result.getId(), "SRM_PURCHASE_REQUEST", requestCode, req);
                        if ("CONFIRMED".equals(task.getStatus())) {
                            result.setStatus("RELEASED");
                            result.setExternalRefType(task.getActionType());
                            result.setExternalRefNo(task.getExternalRefNo());
                            result.setReleasedTime(result.getReleasedTime() == null ? LocalDateTime.now() : result.getReleasedTime());
                            continue;
                        }
                        Result<PurchaseRequestDTO> created = srmClient.createPurchaseRequest(req);
                        if (isRemoteSuccess(created)) {
                            result.setStatus("RELEASED");
                            result.setExternalRefType("SRM_PURCHASE_REQUEST");
                            result.setExternalRefNo(requestCode);
                            result.setReleasedTime(LocalDateTime.now());
                            task.setStatus("CONFIRMED");
                            task.setExternalRefNo(requestCode);
                            task.setLastError(null);
                            integrationTaskRepository.save(task);
                        } else {
                            result.setStatus("FAILED");
                            result.setRejectedReason(created == null ? "SRM返回为空" : created.getMessage());
                            task.setStatus("FAILED");
                            task.setRetryCount(task.getRetryCount() == null ? 1 : task.getRetryCount() + 1);
                            task.setLastError(result.getRejectedReason());
                            integrationTaskRepository.save(task);
                        }
                        
                    } else if ("PRODUCTION".equals(result.getType())) {
                        ProductionPlanDTO prodPlan = new ProductionPlanDTO();
                        String planNo = "MRP-PP-" + planId + "-" + result.getId();
                        prodPlan.setPlanNo(planNo);
                        prodPlan.setPlanName("MRP-" + result.getMaterialName());
                        prodPlan.setPlanType("MRP");
                        prodPlan.setStatus("CREATED");
                        prodPlan.setStartTime(result.getSuggestDate() != null ? result.getSuggestDate().atStartOfDay() : LocalDateTime.now());
                        prodPlan.setEndTime(result.getRequiredDate() != null ? result.getRequiredDate().atStartOfDay() : LocalDateTime.now().plusDays(7));
                        prodPlan.setRemark("数量: " + result.getQuantity());
                        IntegrationTaskEntity task = upsertTask(planId, result.getId(), "APS_PRODUCTION_PLAN", planNo, prodPlan);
                        if ("CONFIRMED".equals(task.getStatus())) {
                            result.setStatus("RELEASED");
                            result.setExternalRefType(task.getActionType());
                            result.setExternalRefNo(task.getExternalRefNo());
                            result.setReleasedTime(result.getReleasedTime() == null ? LocalDateTime.now() : result.getReleasedTime());
                            continue;
                        }
                        Result<ProductionPlanDTO> created = apsClient.createProductionPlan(prodPlan);
                        if (isRemoteSuccess(created)) {
                            result.setStatus("RELEASED");
                            result.setExternalRefType("APS_PRODUCTION_PLAN");
                            result.setExternalRefNo(planNo);
                            result.setReleasedTime(LocalDateTime.now());
                            task.setStatus("CONFIRMED");
                            task.setExternalRefNo(planNo);
                            task.setLastError(null);
                            integrationTaskRepository.save(task);
                        } else {
                            result.setStatus("FAILED");
                            result.setRejectedReason(created == null ? "APS返回为空" : created.getMessage());
                            task.setStatus("FAILED");
                            task.setRetryCount(task.getRetryCount() == null ? 1 : task.getRetryCount() + 1);
                            task.setLastError(result.getRejectedReason());
                            integrationTaskRepository.save(task);
                        }
                    }
                } catch (Exception e) {
                    logger.error("发布计划失败 ID: {}", result.getId(), e);
                    result.setStatus("FAILED");
                    result.setRejectedReason(e.getMessage());
                    String actionType = "PURCHASE".equals(result.getType()) ? "SRM_PURCHASE_REQUEST" : "APS_PRODUCTION_PLAN";
                    IntegrationTaskEntity task = integrationTaskRepository.findByResultIdAndActionType(result.getId(), actionType).orElse(null);
                    if (task != null) {
                        task.setStatus("FAILED");
                        task.setRetryCount(task.getRetryCount() == null ? 1 : task.getRetryCount() + 1);
                        task.setLastError(e.getMessage());
                        integrationTaskRepository.save(task);
                    }
                }
            }
        }
        
        mrpResultRepository.saveAll(results);
        boolean anyReleased = results.stream().anyMatch(r -> "RELEASED".equals(r.getStatus()));
        if (anyReleased) {
            plan.setStatus("RELEASED");
        }
        mrpPlanRepository.save(plan);
    }

    private IntegrationTaskEntity upsertTask(Long planId, Long resultId, String actionType, String externalRefNo, Object request) {
        IntegrationTaskEntity task = integrationTaskRepository.findByResultIdAndActionType(resultId, actionType).orElse(null);
        if (task == null) {
            task = new IntegrationTaskEntity();
            task.setPlanId(planId);
            task.setResultId(resultId);
            task.setActionType(actionType);
            task.setIdempotencyKey(actionType + ":" + resultId);
            task.setStatus("PENDING");
        }
        task.setExternalRefNo(externalRefNo);
        try {
            task.setRequestBody(objectMapper.writeValueAsString(request));
        } catch (Exception e) {
            task.setRequestBody(null);
        }
        return integrationTaskRepository.save(task);
    }

    @Transactional
    @Override
    public MrpPlanEntity runMrp(Map<String, Object> params) {
        // ... (保持不变)
        // 1. 创建运算记录
        MrpPlanEntity plan = new MrpPlanEntity();
        String runNo = "MRP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        plan.setRunNo(runNo);
        plan.setRunName((String) params.getOrDefault("runName", "自动MRP运算"));
        plan.setRunDate(LocalDateTime.now());
        plan.setStatus("RUNNING");
        plan.setDemandSource((String) params.getOrDefault("demandSource", "BOTH"));
        plan.setStartTime(LocalDateTime.now());
        
        plan = mrpPlanRepository.save(plan);
        
        try {
            // 2. 获取毛需求
            List<DemandForecastEntity> forecasts = forecastRepository.findAll();
            Map<String, BigDecimal> demandMap = new HashMap<>();
            
            // 2.1 预测需求
            for (DemandForecastEntity forecast : forecasts) {
                demandMap.merge(forecast.getProductCode(), forecast.getFinalForecast(), BigDecimal::add);
            }
            
            // 2.2 销售订单需求 (新增)
            try {
                // 获取已提交或审批通过的订单
                Result<PageResult<SalesOrderDTO>> crmRes = crmClient.getSalesOrders("submitted", 1, 100);
                if (isRemoteSuccess(crmRes) && crmRes.getData() != null) {
                    List<SalesOrderDTO> orders = crmRes.getData().getRecords();
                    if (orders != null) {
                        for (SalesOrderDTO order : orders) {
                            if (order.getItems() != null) {
                                for (SalesOrderItemDTO item : order.getItems()) {
                                    demandMap.merge(item.getProductCode(), item.getQuantity(), BigDecimal::add);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("获取CRM订单失败: {}", e.getMessage());
            }
            
            // 3. 计算净需求 & 生成建议
            List<MrpResultEntity> results = new ArrayList<>();
            Random random = new Random();
            
            for (Map.Entry<String, BigDecimal> entry : demandMap.entrySet()) {
                String productCode = entry.getKey();
                BigDecimal grossRequirement = entry.getValue();
                
                // 3.1 获取现有库存 (调用WMS)
                BigDecimal currentStock = BigDecimal.ZERO;
                try {
                    Result<List<WmsInventoryDTO>> wmsResult = wmsClient.getInventoryByMaterialCode(productCode);
                    if (isRemoteSuccess(wmsResult) && wmsResult.getData() != null) {
                        currentStock = wmsResult.getData().stream()
                                .map(WmsInventoryDTO::getQuantity)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                    }
                } catch (Exception e) {
                    logger.warn("调用WMS查询库存失败: {}", e.getMessage());
                }

                // 3.2 计算净需求 = 毛需求 - 现有库存
                BigDecimal netRequirement = grossRequirement.subtract(currentStock);
                
                if (netRequirement.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                // 3.3 生成建议
                MrpResultEntity result = new MrpResultEntity();
                result.setPlanId(plan.getId());
                result.setMaterialCode(productCode);
                result.setMaterialName("产品-" + productCode); // 简化名称获取
                result.setQuantity(netRequirement);
                
                boolean isProduction = !productCode.startsWith("RAW") && random.nextBoolean();
                result.setType(isProduction ? "PRODUCTION" : "PURCHASE");
                
                result.setRequiredDate(LocalDate.now().plusDays(30)); 
                result.setSuggestDate(LocalDate.now().plusDays(5));
                result.setSourceId("DEMAND-AGG"); // 需求聚合
                result.setStatus("PENDING");
                
                results.add(result);
                
                // 3.4 模拟原材料需求 (BOM展开)
                if (isProduction) {
                    List<MrpResultEntity> subResults = bomService.explodeBom(
                        productCode, 
                        netRequirement, 
                        plan.getId(), 
                        0L 
                    );
                    results.addAll(subResults);
                }
            }
            
            mrpResultRepository.saveAll(results);
            
            // 4. 更新计划状态
            plan.setEndTime(LocalDateTime.now());
            plan.setStatus("COMPLETED");
            plan.setResultCount(results.size());
            
        } catch (Exception e) {
            plan.setEndTime(LocalDateTime.now());
            plan.setStatus("FAILED");
            e.printStackTrace();
        }
        
        return mrpPlanRepository.save(plan);
    }

    @Override
    public Page<MrpPlanEntity> getMrpHistory(Pageable pageable) {
        return mrpPlanRepository.findAll(pageable);
    }

    @Override
    public List<MrpResultEntity> getMrpResults(Long planId) {
        return mrpResultRepository.findByPlanId(planId);
    }

    @Override
    public Page<MrpResultEntity> queryMrpResults(Long planId, String status, String materialKeyword, String type, Pageable pageable) {
        Specification<MrpResultEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (planId != null) {
                predicates.add(cb.equal(root.get("planId"), planId));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (type != null && !type.isBlank()) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (materialKeyword != null && !materialKeyword.isBlank()) {
                Predicate p1 = cb.like(root.get("materialCode"), "%" + materialKeyword + "%");
                Predicate p2 = cb.like(root.get("materialName"), "%" + materialKeyword + "%");
                predicates.add(cb.or(p1, p2));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return mrpResultRepository.findAll(spec, pageable);
    }

    @Transactional
    @Override
    public MrpResultEntity confirmMrpResult(Long resultId, String operator) {
        MrpResultEntity entity = mrpResultRepository.findById(resultId).orElse(null);
        if (entity == null) return null;
        if ("RELEASED".equals(entity.getStatus())) {
            throw new IllegalStateException("已发布结果不允许确认");
        }
        if (!"PENDING".equals(entity.getStatus()) && !"REJECTED".equals(entity.getStatus())) {
            return entity;
        }
        entity.setStatus("CONFIRMED");
        entity.setConfirmedBy(operator);
        entity.setConfirmedTime(LocalDateTime.now());
        entity.setRejectedReason(null);
        return mrpResultRepository.save(entity);
    }

    @Transactional
    @Override
    public MrpResultEntity rejectMrpResult(Long resultId, String reason, String operator) {
        MrpResultEntity entity = mrpResultRepository.findById(resultId).orElse(null);
        if (entity == null) return null;
        if ("RELEASED".equals(entity.getStatus())) {
            throw new IllegalStateException("已发布结果不允许拒绝");
        }
        entity.setStatus("REJECTED");
        entity.setRejectedReason(reason);
        entity.setConfirmedBy(operator);
        entity.setConfirmedTime(LocalDateTime.now());
        return mrpResultRepository.save(entity);
    }

    @Transactional
    @Override
    public List<MrpResultEntity> batchConfirmMrpResults(List<Long> resultIds, String operator) {
        if (resultIds == null || resultIds.isEmpty()) {
            return List.of();
        }
        List<MrpResultEntity> results = mrpResultRepository.findAllById(resultIds);
        for (MrpResultEntity entity : results) {
            if ("RELEASED".equals(entity.getStatus())) continue;
            if ("PENDING".equals(entity.getStatus()) || "REJECTED".equals(entity.getStatus())) {
                entity.setStatus("CONFIRMED");
                entity.setConfirmedBy(operator);
                entity.setConfirmedTime(LocalDateTime.now());
                entity.setRejectedReason(null);
            }
        }
        return mrpResultRepository.saveAll(results);
    }

    @Transactional
    @Override
    public List<MrpResultEntity> batchRejectMrpResults(List<Long> resultIds, String reason, String operator) {
        if (resultIds == null || resultIds.isEmpty()) {
            return List.of();
        }
        List<MrpResultEntity> results = mrpResultRepository.findAllById(resultIds);
        for (MrpResultEntity entity : results) {
            if ("RELEASED".equals(entity.getStatus())) continue;
            entity.setStatus("REJECTED");
            entity.setRejectedReason(reason);
            entity.setConfirmedBy(operator);
            entity.setConfirmedTime(LocalDateTime.now());
        }
        return mrpResultRepository.saveAll(results);
    }

    private boolean isRemoteSuccess(Result<?> result) {
        return result != null && ResponseStatusAdapter.isSuccess(result.getCode());
    }

    @Transactional
    @Override
    public boolean deleteMrpPlan(Long planId) {
        if (planId == null) return false;
        if (!mrpPlanRepository.existsById(planId)) return false;
        // 先删除关联的MRP结果（避免外键约束）
        List<MrpResultEntity> related = mrpResultRepository.findByPlanId(planId);
        if (related != null && !related.isEmpty()) {
            mrpResultRepository.deleteAll(related);
        }
        mrpPlanRepository.deleteById(planId);
        return true;
    }
}
