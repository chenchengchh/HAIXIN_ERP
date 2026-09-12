package com.hxcoe.scm.client.dto.aps;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ApsProductionPlanDTO {
    private String planNo;
    private String productCode;
    private String productName;
    private BigDecimal quantity;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private String source; // e.g., "MRP"
    private String sourceId;
    private String remark;
}
