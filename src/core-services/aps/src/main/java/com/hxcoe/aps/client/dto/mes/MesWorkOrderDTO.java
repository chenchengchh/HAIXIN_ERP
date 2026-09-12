package com.hxcoe.aps.client.dto.mes;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MesWorkOrderDTO {
    private String workOrderNo;
    private String sourceId;
    private String productCode;
    private String productName;
    private BigDecimal planQuantity;
    private String resourceName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}
