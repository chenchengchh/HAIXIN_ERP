package com.hxcoe.wms.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WmsInventoryDTO {
    private Long id;
    private String materialCode;
    private String materialName;
    private String warehouseCode;
    private BigDecimal quantity;
    private BigDecimal lockedQuantity;
    private String unit;
}

