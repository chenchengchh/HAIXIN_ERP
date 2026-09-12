package com.hxcoe.plm.client.dto.bom;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BomLineDTO {
    private Long id;
    private Long childMaterialId;
    private String childMaterialCode;
    private String childMaterialName;
    private BigDecimal quantity;
    private String unit;
    private Integer level;
    private Integer sequence;
    private BigDecimal scrapRate;
}
