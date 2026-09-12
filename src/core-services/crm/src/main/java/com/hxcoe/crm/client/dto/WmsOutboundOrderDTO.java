package com.hxcoe.crm.client.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * WMS出库单DTO
 */
@Data
public class WmsOutboundOrderDTO {

    /**
     * 来源单号 (销售订单号)
     */
    private String sourceNo;

    /**
     * 订单类型: SALES-销售出库
     */
    private String orderType;

    /**
     * 客户ID
     */
    private Long customerId;

    /**
     * 客户名称
     */
    private String customerName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 出库单明细
     */
    private List<WmsOutboundOrderItemDTO> items;
}
