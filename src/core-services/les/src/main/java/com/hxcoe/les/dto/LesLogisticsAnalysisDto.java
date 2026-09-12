package com.hxcoe.les.dto;

import lombok.Data;

@Data
public class LesLogisticsAnalysisDto {

    private Long id;
    private Long planId;
    private Integer actualDuration;
    private Integer plannedDuration;
    private Integer delayMinutes;
    private Double vehicleUtilization;
    private Double driverUtilization;
    private Double costPerKm;
    private String createTime;
}

