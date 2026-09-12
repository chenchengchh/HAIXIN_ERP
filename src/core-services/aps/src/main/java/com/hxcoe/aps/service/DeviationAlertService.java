package com.hxcoe.aps.service;

import com.hxcoe.aps.entity.DeviationAlertEntity;
import java.util.List;
import java.util.Map;

public interface DeviationAlertService {

    Map<String, Object> getPlanCompareData(Long planId, String startTime, String endTime);

    List<Map<String, Object>> getProgressTrackingData(Long planId, Long orderId, String status);

    List<DeviationAlertEntity> getDeviationAlerts(Long planId, String severity, String status);

    DeviationAlertEntity createDeviationAlert(DeviationAlertEntity alert);

    DeviationAlertEntity processAlert(Long id);

    Map<String, Object> getKpiMetrics(Long planId, String startTime, String endTime);
}
