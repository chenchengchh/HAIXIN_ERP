package com.hxcoe.scm.client.dto.srm;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class PurchaseRequestDTO {
    private String requestCode;
    private String applicant;
    private String department;
    private LocalDateTime applyDate;
    private String description;
    private Double expectedAmount;
    private LocalDateTime expectedDeliveryDate;
    private String purchaseType;
    private String status;

    // Added fields
    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private String unit;
}
