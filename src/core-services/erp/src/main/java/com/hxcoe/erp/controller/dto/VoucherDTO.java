package com.hxcoe.erp.controller.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class VoucherDTO {
    private String voucherNo;
    private LocalDate voucherDate;
    private String description;
    private BigDecimal totalAmount;
    private String type; // INVENTORY_IN, INVENTORY_OUT
}
