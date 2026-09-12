package com.hxcoe.ems.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePlanStatusRequest {
    @NotNull(message = "status不能为空")
    @Min(value = 1, message = "status必须在1-4之间")
    @Max(value = 4, message = "status必须在1-4之间")
    private Integer status;
}

