package com.hxcoe.mes.client.dto.qms;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InspectionResultDTO {
    private String sourceNo;
    private String result; // PASS, FAIL
    private BigDecimal qualifiedQuantity;
    private BigDecimal unqualifiedQuantity;
}
