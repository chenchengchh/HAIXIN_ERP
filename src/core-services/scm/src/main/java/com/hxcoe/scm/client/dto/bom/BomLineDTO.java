package com.hxcoe.scm.client.dto.bom;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BomLineDTO {
    private Long id;
    private Long childMaterialId;
    private String childMaterialCode; // 需要BOM服务填充此字段
    private String childMaterialName; // 需要BOM服务填充此字段
    private BigDecimal quantity;
    private BigDecimal scrapRate;
}
