package com.hxcoe.scm.client.dto.aps;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductionPlanDTO {
    private String planNo;
    private String planName;
    private String planType;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String remark;
}
