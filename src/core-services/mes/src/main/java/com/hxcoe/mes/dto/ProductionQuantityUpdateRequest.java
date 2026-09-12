package com.hxcoe.mes.dto;

import lombok.Data;

@Data
public class ProductionQuantityUpdateRequest {
    private Integer actualQuantity;
    private Integer qualifiedQuantity;
    private Integer unqualifiedQuantity;
}

