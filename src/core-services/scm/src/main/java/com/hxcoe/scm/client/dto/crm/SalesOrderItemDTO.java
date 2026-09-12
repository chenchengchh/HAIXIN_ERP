package com.hxcoe.scm.client.dto.crm;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SalesOrderItemDTO {
    private Long id;
    private String productCode;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal amount;
}
