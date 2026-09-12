package com.hxcoe.scm.client.dto.crm;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SalesOrderDTO {
    private Long id;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private BigDecimal finalAmount;
    private LocalDate deliveryDate;
    private String status;
    private List<SalesOrderItemDTO> items;
}
