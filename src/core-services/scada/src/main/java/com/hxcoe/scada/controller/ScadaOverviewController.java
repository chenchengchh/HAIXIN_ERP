package com.hxcoe.scada.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scada.repository.ScadaActiveAlarmRepository;
import com.hxcoe.scada.repository.ScadaCollectPointRepository;
import com.hxcoe.scada.repository.ScadaDeviceRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/scada/overview")
public class ScadaOverviewController {

    private final ScadaDeviceRepository deviceRepository;
    private final ScadaCollectPointRepository collectPointRepository;
    private final ScadaActiveAlarmRepository activeAlarmRepository;

    public ScadaOverviewController(
            ScadaDeviceRepository deviceRepository,
            ScadaCollectPointRepository collectPointRepository,
            ScadaActiveAlarmRepository activeAlarmRepository
    ) {
        this.deviceRepository = deviceRepository;
        this.collectPointRepository = collectPointRepository;
        this.activeAlarmRepository = activeAlarmRepository;
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        long totalDevices = deviceRepository.count();
        long onlineDevices = deviceRepository.count((root, query, cb) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();
            predicates.add(cb.equal(root.get("deviceStatus"), "在线"));
            return cb.and(predicates.toArray(new Predicate[0]));
        });
        long collectionPoints = collectPointRepository.count();
        long activeAlarms = activeAlarmRepository.count();

        Map<String, Object> data = new HashMap<>();
        data.put("totalDevices", totalDevices);
        data.put("onlineDevices", onlineDevices);
        data.put("activeAlarms", activeAlarms);
        data.put("collectionPoints", collectionPoints);
        return ApiResponse.success(data);
    }
}

