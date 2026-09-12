package com.hxcoe.aps.service.impl;

import com.hxcoe.aps.entity.DeviationAlertEntity;
import com.hxcoe.aps.repository.DeviationAlertRepository;
import com.hxcoe.aps.service.DeviationAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeviationAlertServiceImpl implements DeviationAlertService {

    @Autowired
    private DeviationAlertRepository deviationAlertRepository;

    @Override
    public Map<String, Object> getPlanCompareData(Long planId, String startTime, String endTime) {
        Map<String, Object> compareData = new HashMap<>();
        compareData.put("planId", planId);
        compareData.put("plannedProduction", 1000);
        compareData.put("actualProduction", 800);
        compareData.put("deviation", -20);
        return compareData;
    }

    @Override
    public List<Map<String, Object>> getProgressTrackingData(Long planId, Long orderId, String status) {
        return new ArrayList<>();
    }

    @Override
    public List<DeviationAlertEntity> getDeviationAlerts(Long planId, String severity, String status) {
        if (severity != null && status != null) {
            return deviationAlertRepository.findByPlanIdAndSeverityAndStatus(planId, severity, status);
        } else if (severity != null) {
            return deviationAlertRepository.findByPlanIdAndSeverity(planId, severity);
        } else if (status != null) {
            return deviationAlertRepository.findByPlanIdAndStatus(planId, status);
        } else {
            return deviationAlertRepository.findByPlanId(planId);
        }
    }

    @Override
    public DeviationAlertEntity createDeviationAlert(DeviationAlertEntity alert) {
        return deviationAlertRepository.save(alert);
    }

    @Override
    public DeviationAlertEntity processAlert(Long id) {
        DeviationAlertEntity alert = deviationAlertRepository.findById(id).orElse(null);
        if (alert != null) {
            alert.setStatus("PROCESSED");
            return deviationAlertRepository.save(alert);
        }
        return null;
    }

    @Override
    public Map<String, Object> getKpiMetrics(Long planId, String startTime, String endTime) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalProduction", 1000);
        metrics.put("efficiency", 85.5);
        metrics.put("onTimeDeliveryRate", 95.0);
        return metrics;
    }
}
