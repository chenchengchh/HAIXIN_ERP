package com.hxcoe.mes.dto;

import lombok.Data;

@Data
public class WipLocationUpdateRequest {
    private String snCode;
    private String workOrderNo;
    private String stationId;
    private String stationName;
    private String stepId;
    private String stepName;
    private String status;
}
