package com.hxcoe.mes.client.dto.wms;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WmsAsnDTO {
    private String asnNo;
    private String refOrderNo;
    private Long supplierId;
    private String supplierName;
    private Integer status; // 0-待确认
    // WMS侧LocalDateTime按默认ISO格式反序列化，需覆盖全局yyyy-MM-dd HH:mm:ss格式
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expectedArrivalTime;
    private BigDecimal totalQty;
    // 简化：这里暂时不包含 items，或者需要单独定义 ItemDTO
}
