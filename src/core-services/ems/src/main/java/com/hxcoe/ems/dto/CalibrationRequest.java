package com.hxcoe.ems.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CalibrationRequest {
    @NotNull(message = "meterId不能为空")
    private Long meterId;

    @NotBlank(message = "meterName不能为空")
    @Size(max = 100, message = "meterName长度不能超过100")
    private String meterName;

    @NotNull(message = "rawValue不能为空")
    private Double rawValue;

    @NotNull(message = "coefficient不能为空")
    @DecimalMin(value = "0.000001", message = "coefficient必须大于0")
    private Double coefficient;

    @Size(max = 500, message = "reason长度不能超过500")
    private String reason;
}

