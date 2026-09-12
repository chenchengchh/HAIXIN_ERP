package com.hxcoe.scm.client.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SrmPurchaseRequestDTO {
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private LocalDate requiredDate;
    private String remark;
    private String source; // e.g. "MRP"
    private String sourceId; // MRP Result ID
}
