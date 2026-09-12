package com.hxcoe.les.dto;

import lombok.Data;

@Data
public class LesTransportStatsDto {

    private Long totalPlans;
    private Long inTransitPlans;
    private Long completedPlans;
    private Long delayedPlans;
    private Double totalDistance;
    private Double totalCost;
    private Double averageOnTimeRate;
    private Double averageSignSuccessRate;
}

