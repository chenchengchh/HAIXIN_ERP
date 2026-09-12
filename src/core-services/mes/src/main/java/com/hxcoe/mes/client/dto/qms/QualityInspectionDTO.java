package com.hxcoe.mes.client.dto.qms;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class QualityInspectionDTO {
    private String inspectionCode;
    private String productCode;
    private String productName;
    private String sourceType;
    private String sourceNo;
    private BigDecimal quantity;
    private String status;
}
