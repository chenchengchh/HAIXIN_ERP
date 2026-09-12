package com.hxcoe.mes.client.dto.wms;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WmsOutboundOrderDTO {
    private String outboundNo;
    private String sourceNo;
    private String orderType; // MATERIAL
    private String status; // CREATED
    private Long customerId; // 可以借用存车间ID
    private String customerName; // 车间名称
    private BigDecimal totalQty;
}
