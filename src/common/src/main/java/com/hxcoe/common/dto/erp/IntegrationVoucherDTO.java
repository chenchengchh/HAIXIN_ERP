package com.hxcoe.common.dto.erp;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 集成凭证 DTO（跨服务财务凭证契约）。
 * <p>注意：跨服务契约固定使用小驼峰命名，通过 @JsonNaming 显式声明，
 * 避免受各服务全局 Jackson 命名策略（如 ERP 的 SNAKE_CASE）影响。</p>
 */
@Data
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class IntegrationVoucherDTO {
    private String voucherCode;
    private String voucherType;
    private LocalDateTime voucherDate;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String sourceSystem;
    private String sourceId;
}
