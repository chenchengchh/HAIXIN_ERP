package com.hxcoe.mes.client.dto.bom;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BomLineDTO {
    private Long id;
    private Long childMaterialId;
    private String childMaterialCode;
    private String childMaterialName;
    private BigDecimal quantity;
    private BigDecimal scrapRate;
}
