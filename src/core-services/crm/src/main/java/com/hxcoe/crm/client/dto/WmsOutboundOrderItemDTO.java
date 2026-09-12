package com.hxcoe.crm.client.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * WMS出库单明细DTO
 */
@Data
public class WmsOutboundOrderItemDTO {

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 规格型号
     */
    private String specification;

    /**
     * 计划数量
     */
    private BigDecimal planQuantity;

    /**
     * 单位
     */
    private String unit;
}
