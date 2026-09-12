package com.hxcoe.ems.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProcessAnomalyRequest {
    @Size(max = 50, message = "processedBy长度不能超过50")
    private String processedBy;

    @Size(max = 500, message = "remark长度不能超过500")
    private String remark;
}

