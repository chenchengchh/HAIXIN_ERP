package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.Opportunity;
import com.hxcoe.crm.repository.OpportunityRepository;
import com.hxcoe.crm.service.OpportunityService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 商机服务实现类
 */
@Service
public class OpportunityServiceImpl implements OpportunityService {
    
    @Autowired
    private OpportunityRepository opportunityRepository;

    @Override
    public Page<Opportunity> getOpportunitiesList(String opportunityName, String customerName, String stage, String status, Pageable pageable) {
        // 处理空字符串，确保查询条件正确
        opportunityName = opportunityName != null ? opportunityName : "";
        customerName = customerName != null ? customerName : "";
        stage = stage != null ? stage : "";
        status = status != null ? status : "";
        
        return opportunityRepository.findByOpportunityNameContainingAndCustomerNameContainingAndStageContainingAndStatusContaining(
                opportunityName, customerName, stage, status, pageable);
    }

    @Override
    public Opportunity createOpportunity(Opportunity opportunity) {
        return opportunityRepository.save(opportunity);
    }

    @Override
    public Opportunity getOpportunityDetail(Long id) {
        return opportunityRepository.findById(id).orElse(null);
    }

    @Override
    public Opportunity updateOpportunity(Long id, Opportunity opportunity) {
        Opportunity existingOpportunity = opportunityRepository.findById(id).orElse(null);
        if (existingOpportunity != null) {
            // 更新商机信息
            existingOpportunity.setOpportunityName(opportunity.getOpportunityName());
            existingOpportunity.setCustomerId(opportunity.getCustomerId());
            existingOpportunity.setCustomerName(opportunity.getCustomerName());
            existingOpportunity.setLeadId(opportunity.getLeadId());
            existingOpportunity.setStage(opportunity.getStage());
            existingOpportunity.setWinProbability(opportunity.getWinProbability());
            existingOpportunity.setEstimatedAmount(opportunity.getEstimatedAmount());
            existingOpportunity.setActualAmount(opportunity.getActualAmount());
            existingOpportunity.setExpectedCloseDate(opportunity.getExpectedCloseDate());
            existingOpportunity.setActualCloseDate(opportunity.getActualCloseDate());
            existingOpportunity.setOwnerId(opportunity.getOwnerId());
            existingOpportunity.setOwnerName(opportunity.getOwnerName());
            existingOpportunity.setLossReason(opportunity.getLossReason());
            existingOpportunity.setStatus(opportunity.getStatus());
            existingOpportunity.setCompetitors(opportunity.getCompetitors());
            existingOpportunity.setProducts(opportunity.getProducts());
            
            return opportunityRepository.save(existingOpportunity);
        }
        return null;
    }

    @Override
    public String updateOpportunityStage(Long id, Object data) {
        // 这里可以实现商机阶段推进的逻辑
        return "商机阶段推进成功";
    }

    @Override
    public Page<Opportunity> getMyOpportunities(Long ownerId, Pageable pageable) {
        return opportunityRepository.findByOwnerId(ownerId, pageable);
    }

    /**
     * 获取销售漏斗分析：按商机阶段分组统计（仅统计未删除数据）
     * 阶段顺序：initial初步接触 -> qualification资格确认 -> proposal方案报价 -> negotiation商务谈判 -> closed_won赢单
     * 转化率为当前阶段商机数相对上一阶段商机数的百分比，首阶段固定为100
     * @return 销售漏斗数据列表，每项包含 stage/stageName/count/amount/conversionRate
     */
    @Override
    public List<Map<String, Object>> getSalesFunnel() {
        // 漏斗阶段定义（按顺序）：阶段key -> 阶段中文名
        Map<String, String> stageNames = new LinkedHashMap<>();
        stageNames.put("initial", "初步接触");
        stageNames.put("qualification", "资格确认");
        stageNames.put("proposal", "方案报价");
        stageNames.put("negotiation", "商务谈判");
        stageNames.put("closed_won", "赢单");

        // 查询所有未删除的商机
        Specification<Opportunity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<Opportunity> opportunities = opportunityRepository.findAll(spec);

        // 按阶段分组统计商机数量与预计金额
        Map<String, Long> stageCounts = new LinkedHashMap<>();
        Map<String, BigDecimal> stageAmounts = new LinkedHashMap<>();
        for (String stage : stageNames.keySet()) {
            stageCounts.put(stage, 0L);
            stageAmounts.put(stage, BigDecimal.ZERO);
        }
        for (Opportunity opportunity : opportunities) {
            String stage = opportunity.getStage();
            if (stage != null && stageCounts.containsKey(stage)) {
                stageCounts.put(stage, stageCounts.get(stage) + 1);
                if (opportunity.getEstimatedAmount() != null) {
                    stageAmounts.put(stage, stageAmounts.get(stage).add(opportunity.getEstimatedAmount()));
                }
            }
        }

        // 组装漏斗数据，计算相对上一阶段的转化率
        List<Map<String, Object>> funnel = new ArrayList<>();
        long previousCount = -1;
        for (Map.Entry<String, String> entry : stageNames.entrySet()) {
            String stage = entry.getKey();
            long count = stageCounts.get(stage);
            double conversionRate;
            if (previousCount < 0) {
                // 首阶段转化率为100
                conversionRate = 100.0;
            } else {
                conversionRate = previousCount > 0 ? (double) count / previousCount * 100 : 0;
            }

            Map<String, Object> stageData = new LinkedHashMap<>();
            stageData.put("stage", stage);
            stageData.put("stageName", entry.getValue());
            stageData.put("count", count);
            stageData.put("amount", stageAmounts.get(stage));
            stageData.put("conversionRate", Math.round(conversionRate * 100.0) / 100.0);
            funnel.add(stageData);

            previousCount = count;
        }
        return funnel;
    }
}
