package com.hxcoe.scm.service;

import com.hxcoe.scm.entity.MrpResultEntity;
import java.math.BigDecimal;
import java.util.List;

public interface BomService {
    /**
     * 根据产品编码展开 BOM，生成原材料需求建议
     */
    List<MrpResultEntity> explodeBom(String productCode, BigDecimal quantity, Long planId, Long sourceResultId);
}
