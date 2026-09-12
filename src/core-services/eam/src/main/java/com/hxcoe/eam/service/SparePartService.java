package com.hxcoe.eam.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.eam.client.WmsSpareIssueClient;
import com.hxcoe.eam.entity.SparePartEntity;
import com.hxcoe.eam.entity.SpareInventoryEntity;
import com.hxcoe.eam.entity.SpareDemandPlanEntity;
import com.hxcoe.eam.entity.SpareIssueEntity;
import com.hxcoe.eam.repository.SparePartRepository;
import com.hxcoe.eam.repository.SpareInventoryRepository;
import com.hxcoe.eam.repository.SpareDemandPlanRepository;
import com.hxcoe.eam.repository.SpareIssueRepository;
import com.hxcoe.eam.repository.MaintenancePlanRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class SparePartService {

    /**
     * 默认备件仓库编码（EAM领用时推送WMS扣减的目标仓库）
     */
    private static final String DEFAULT_SPARE_WAREHOUSE = "WH-SPARE";

    @Autowired
    private SparePartRepository sparePartRepository;

    @Autowired
    private SpareInventoryRepository inventoryRepository;

    @Autowired
    private SpareDemandPlanRepository demandPlanRepository;

    @Autowired
    private SpareIssueRepository issueRepository;

    @Autowired
    private MaintenancePlanRepository maintenancePlanRepository;

    /**
     * WMS备件库存扣减客户端（可选注入，WMS不可用时仅记录告警，不回滚EAM本地领用）
     */
    @Autowired(required = false)
    private WmsSpareIssueClient wmsSpareIssueClient;

    public List<SparePartEntity> getAllSpareParts() {
        return sparePartRepository.findAll();
    }

    public SparePartEntity saveSparePart(SparePartEntity sparePart) {
        return sparePartRepository.save(sparePart);
    }

    public void deleteSparePart(Long id) {
        sparePartRepository.deleteById(id);
    }
    
    public List<SpareInventoryEntity> getAllInventory() {
        List<SpareInventoryEntity> list = inventoryRepository.findAll();
        list.forEach(item -> {
            if (item.getSpareId() != null) {
                sparePartRepository.findById(item.getSpareId()).ifPresent(part -> {
                    item.setSafetyStock(part.getSafetyStock());
                });
            }
        });
        return list;
    }
    
    public SpareInventoryEntity saveInventory(SpareInventoryEntity inventory) {
        if (inventory.getSpareId() != null) {
            sparePartRepository.findById(inventory.getSpareId()).ifPresent(part -> {
                inventory.setSpareName(part.getName());
                inventory.setSpareCode(part.getCode());
            });
        }
        // 库存调整时同步备件总库存：按调整差值更新 spare_part.current_stock，保持两表一致
        if (inventory.getId() != null && inventory.getQuantity() != null && inventory.getSpareId() != null) {
            inventoryRepository.findById(inventory.getId()).ifPresent(existing -> {
                int oldQty = existing.getQuantity() == null ? 0 : existing.getQuantity();
                int delta = inventory.getQuantity() - oldQty;
                if (delta != 0) {
                    sparePartRepository.findById(inventory.getSpareId()).ifPresent(part -> {
                        int newStock = (part.getCurrentStock() == null ? 0 : part.getCurrentStock()) + delta;
                        part.setCurrentStock(Math.max(0, newStock));
                        sparePartRepository.save(part);
                    });
                }
            });
        }
        return inventoryRepository.save(inventory);
    }
    
    public List<SpareDemandPlanEntity> getAllDemandPlans() {
        List<SpareDemandPlanEntity> list = demandPlanRepository.findAll();
        list.forEach(item -> {
            if (item.getSpareId() != null) {
                sparePartRepository.findById(item.getSpareId()).ifPresent(part -> {
                    item.setCurrentStock(part.getCurrentStock());
                    // Calculate gap
                    if (item.getRequiredQty() != null && part.getCurrentStock() != null) {
                        item.setGap(Math.max(0, item.getRequiredQty() - part.getCurrentStock()));
                    }
                });
            }
            if (item.getMaintenancePlanId() != null) {
                maintenancePlanRepository.findById(item.getMaintenancePlanId()).ifPresent(plan -> {
                    item.setMaintenancePlanName(plan.getDescription());
                });
            }
        });
        return list;
    }
    
    public SpareDemandPlanEntity saveDemandPlan(SpareDemandPlanEntity plan) {
        if (plan.getSpareId() != null) {
            sparePartRepository.findById(plan.getSpareId()).ifPresent(part -> {
                plan.setSpareName(part.getName());
                plan.setSpareCode(part.getCode());
            });
        }
        return demandPlanRepository.save(plan);
    }
    
    public List<SpareIssueEntity> getAllIssues() {
        return issueRepository.findAll();
    }
    
    public SpareIssueEntity saveIssue(SpareIssueEntity issue) {
        if (issue.getSpareId() != null) {
            sparePartRepository.findById(issue.getSpareId()).ifPresent(part -> {
                issue.setSpareName(part.getName());
                issue.setSpareCode(part.getCode());
            });
        }
        // 新建领用申请默认状态为待审批（前端不传status时兜底）
        if (issue.getStatus() == null || issue.getStatus().isBlank()) {
            issue.setStatus("pending");
        }
        return issueRepository.save(issue);
    }
    
    // 业务逻辑：批准领用申请，扣减库存
    public SpareIssueEntity approveIssue(Long issueId) {
        SpareIssueEntity issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new RuntimeException("Issue not found"));
            
        if ("approved".equals(issue.getStatus())) {
            throw new RuntimeException("Issue already approved");
        }
        
        // 扣减备件总库存
        SparePartEntity sparePart = sparePartRepository.findById(issue.getSpareId())
            .orElseThrow(() -> new RuntimeException("Spare part not found"));
            
        if (sparePart.getCurrentStock() < issue.getQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }
        
        sparePart.setCurrentStock(sparePart.getCurrentStock() - issue.getQuantity());
        sparePartRepository.save(sparePart);

        // 同步扣减库存明细记录（存在时），保持与备件总库存一致
        inventoryRepository.findAll().stream()
            .filter(inv -> issue.getSpareId().equals(inv.getSpareId()))
            .findFirst()
            .ifPresent(inv -> {
                inv.setQuantity(inv.getQuantity() - issue.getQuantity());
                inventoryRepository.save(inv);
            });

        // 更新申请状态
        issue.setStatus("approved");
        SpareIssueEntity saved = issueRepository.save(issue);

        // EAM→WMS 库存联动：本地领用保存成功后，异步推送WMS扣减对应备件库存
        pushWmsSpareDeduction(saved);
        return saved;
    }

    /**
     * 拒绝领用申请：仅更新状态为rejected，不涉及库存变动
     *
     * @param issueId 领用申请ID
     * @return 更新后的领用记录
     */
    public SpareIssueEntity rejectIssue(Long issueId) {
        SpareIssueEntity issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new RuntimeException("Issue not found"));
        if (!"pending".equals(issue.getStatus())) {
            throw new RuntimeException("Only pending issue can be rejected");
        }
        issue.setStatus("rejected");
        return issueRepository.save(issue);
    }

    /**
     * 归还备件：更新归还状态为returned，并回补备件库存与库存记录
     *
     * @param issueId 领用申请ID
     * @return 更新后的领用记录
     */
    public SpareIssueEntity returnIssue(Long issueId) {
        SpareIssueEntity issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new RuntimeException("Issue not found"));
        if (!"approved".equals(issue.getStatus())) {
            throw new RuntimeException("Only approved issue can be returned");
        }
        if ("returned".equals(issue.getReturnStatus())) {
            throw new RuntimeException("Issue already returned");
        }
        // 回补备件总库存
        if (issue.getSpareId() != null) {
            sparePartRepository.findById(issue.getSpareId()).ifPresent(part -> {
                part.setCurrentStock(part.getCurrentStock() + issue.getQuantity());
                sparePartRepository.save(part);
            });
            // 同步回补库存明细记录（存在时）
            inventoryRepository.findAll().stream()
                .filter(inv -> issue.getSpareId().equals(inv.getSpareId()))
                .findFirst()
                .ifPresent(inv -> {
                    inv.setQuantity(inv.getQuantity() + issue.getQuantity());
                    inventoryRepository.save(inv);
                });
        }
        issue.setReturnStatus("returned");
        return issueRepository.save(issue);
    }

    /**
     * 推送备件领用事件到WMS扣减库存（EAM→WMS 备件领用库存联动闭环）
     * WMS扣减失败（如库存不足、服务不可用）时仅记录告警，不回滚EAM本地领用，保持最终一致
     *
     * @param issue 已批准的备件领用记录
     */
    private void pushWmsSpareDeduction(SpareIssueEntity issue) {
        if (wmsSpareIssueClient == null) {
            log.warn("WMS备件扣减客户端不可用，跳过库存联动: issueId={}, spareCode={}",
                    issue.getId(), issue.getSpareCode());
            return;
        }
        // 组装领用事件体：备件编码、数量、目标仓库、领用时间
        Map<String, Object> body = new HashMap<>();
        String eventId = UUID.randomUUID().toString();
        body.put("eventId", eventId);
        body.put("spareCode", issue.getSpareCode());
        body.put("spareName", issue.getSpareName());
        body.put("quantity", issue.getQuantity());
        body.put("warehouseCode", DEFAULT_SPARE_WAREHOUSE);
        body.put("workOrderNo", issue.getRemark());
        body.put("issueTime", LocalDateTime.now().toString());
        try {
            Result<Map<String, Object>> result = wmsSpareIssueClient.deductSpareStock(body);
            if (result == null || result.getCode() == null || result.getCode() != 0) {
                log.warn("WMS备件库存扣减失败（EAM领用不回滚，待人工对账）: issueId={}, spareCode={}, eventId={}, msg={}",
                        issue.getId(), issue.getSpareCode(), eventId, result == null ? null : result.getMsg());
            } else {
                log.info("WMS备件库存扣减成功: issueId={}, spareCode={}, eventId={}, result={}",
                        issue.getId(), issue.getSpareCode(), eventId, result.getData());
            }
        } catch (Exception e) {
            log.warn("WMS备件库存扣减调用异常（EAM领用不回滚）: issueId={}, spareCode={}, eventId={}, error={}",
                    issue.getId(), issue.getSpareCode(), eventId, e.getMessage());
        }
    }
}
