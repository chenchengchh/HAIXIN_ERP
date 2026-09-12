package com.hxcoe.scm.service.impl;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.client.BomClient;
import com.hxcoe.scm.client.dto.bom.BomLineDTO;
import com.hxcoe.scm.client.dto.bom.BomStructureDTO;
import com.hxcoe.scm.client.dto.bom.MaterialDTO;
import com.hxcoe.scm.entity.MrpResultEntity;
import com.hxcoe.scm.service.BomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BomServiceImpl implements BomService {

    private static final Logger logger = LoggerFactory.getLogger(BomServiceImpl.class);

    @Autowired
    private BomClient bomClient;

    @Override
    public List<MrpResultEntity> explodeBom(String productCode, BigDecimal quantity, Long planId, Long sourceResultId) {
        List<MrpResultEntity> rawMaterials = new ArrayList<>();

        try {
            // 1. 根据编码查询物料ID
            ApiResponse<PageResult<MaterialDTO>> materialRes = bomClient.getMaterials(productCode, null, 1, 1);
            if (materialRes == null || materialRes.getCode() == null || materialRes.getCode() != 200 || materialRes.getData() == null || materialRes.getData().getTotal() == 0 || materialRes.getData().getRecords() == null || materialRes.getData().getRecords().isEmpty()) {
                logger.warn("未找到物料信息: {}", productCode);
                // 降级：使用模拟逻辑或直接返回空
                return fallbackExplode(productCode, quantity, planId, sourceResultId);
            }
            
            MaterialDTO material = materialRes.getData().getRecords().get(0);
            
            // 2. 获取BOM结构
            ApiResponse<BomStructureDTO> bomRes = bomClient.getBomTree(material.getId());
            if (bomRes.getCode() == null || bomRes.getCode() != 200 || bomRes.getData() == null || bomRes.getData().getLines() == null) {
                logger.warn("未找到BOM结构: {}", productCode);
                return fallbackExplode(productCode, quantity, planId, sourceResultId);
            }
            
            // 3. 遍历BOM行生成需求
            for (BomLineDTO line : bomRes.getData().getLines()) {
                MrpResultEntity raw = new MrpResultEntity();
                raw.setPlanId(planId);
                
                // 这里假设BOM服务返回了ChildMaterialCode/Name，如果没有，可能需要再次查询
                // 实际项目中BOM服务的getTree接口通常会Fetch子物料信息
                String childCode = line.getChildMaterialCode() != null ? line.getChildMaterialCode() : "ID-" + line.getChildMaterialId();
                String childName = line.getChildMaterialName() != null ? line.getChildMaterialName() : "未知物料";
                
                raw.setMaterialCode(childCode);
                raw.setMaterialName(childName);
                
                // 需求数量 = 订单数 * 单耗 * (1 + 损耗率)
                BigDecimal usage = line.getQuantity();
                BigDecimal scrap = line.getScrapRate() != null ? line.getScrapRate() : BigDecimal.ZERO;
                BigDecimal reqQty = quantity.multiply(usage).multiply(BigDecimal.ONE.add(scrap));
                
                raw.setQuantity(reqQty);
                raw.setType("PURCHASE"); // 简化：假设子件都是采购件
                
                // 时间推算：假设采购提前期5天
                raw.setRequiredDate(LocalDate.now().plusDays(5));
                raw.setSuggestDate(LocalDate.now());
                raw.setSourceId("MRP-" + sourceResultId);
                raw.setStatus("PENDING");
                
                rawMaterials.add(raw);
            }
            
        } catch (Exception e) {
            logger.error("BOM展开失败: {}", e.getMessage());
            return fallbackExplode(productCode, quantity, planId, sourceResultId);
        }

        return rawMaterials;
    }
    
    private List<MrpResultEntity> fallbackExplode(String productCode, BigDecimal quantity, Long planId, Long sourceResultId) {
        List<MrpResultEntity> rawMaterials = new ArrayList<>();
        // 模拟逻辑 (保持原有逻辑作为兜底)
        for (int i = 1; i <= 2; i++) {
            MrpResultEntity raw = new MrpResultEntity();
            raw.setPlanId(planId);
            raw.setMaterialCode("RAW-" + productCode + "-" + i);
            raw.setMaterialName("原材料-" + (i==1?"A":"B"));
            raw.setQuantity(quantity.multiply(new BigDecimal(i))); 
            raw.setType("PURCHASE");
            raw.setRequiredDate(LocalDate.now().plusDays(5));
            raw.setSuggestDate(LocalDate.now());
            raw.setSourceId("MRP-" + sourceResultId);
            raw.setStatus("PENDING");
            rawMaterials.add(raw);
        }
        return rawMaterials;
    }
}
