package com.hxcoe.scm.client.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CrmSalesOrderDTO {
    private Long id;
    private String orderNo;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String status;
    // For simplicity, we assume we can get order items or just use total amount for forecast trend
}
