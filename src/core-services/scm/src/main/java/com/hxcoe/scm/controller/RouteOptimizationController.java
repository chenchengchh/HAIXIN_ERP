package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/scm/route-optimization", "/api/scm/route-optimization"})
public class RouteOptimizationController {

    @PostMapping("/optimize")
    public ApiResponse<Map<String, Object>> optimize(@RequestBody Map<String, Object> req) {
        String start = String.valueOf(req.getOrDefault("startLocation", ""));
        String end = String.valueOf(req.getOrDefault("endLocation", ""));
        String mode = String.valueOf(req.getOrDefault("transportMode", "road"));
        String goal = String.valueOf(req.getOrDefault("optimizationGoal", "cost"));
        BigDecimal weight = toDecimal(req.get("weight"));

        BigDecimal distance = estimateDistance(start, end);
        BigDecimal speed = switch (mode) {
            case "air" -> new BigDecimal("700");
            case "rail" -> new BigDecimal("120");
            case "water" -> new BigDecimal("40");
            default -> new BigDecimal("80");
        };
        BigDecimal hours = distance.divide(speed, 2, RoundingMode.HALF_UP);
        BigDecimal baseCostPerKm = switch (mode) {
            case "air" -> new BigDecimal("8.0");
            case "rail" -> new BigDecimal("2.0");
            case "water" -> new BigDecimal("1.2");
            default -> new BigDecimal("1.8");
        };
        BigDecimal cost = distance.multiply(baseCostPerKm).multiply(weight.max(BigDecimal.ONE).divide(new BigDecimal("10"), 4, RoundingMode.HALF_UP).add(BigDecimal.ONE));

        if ("time".equals(goal)) {
            cost = cost.multiply(new BigDecimal("1.1"));
        } else if ("distance".equals(goal)) {
            cost = cost.multiply(new BigDecimal("0.98"));
        }

        BigDecimal fuel = distance.multiply(new BigDecimal("0.12")).multiply(weight.max(BigDecimal.ONE).divide(new BigDecimal("10"), 4, RoundingMode.HALF_UP).add(BigDecimal.ONE));
        BigDecimal carbon = fuel.multiply(new BigDecimal("2.6"));

        Map<String, Object> result = new HashMap<>();
        result.put("route", start + " → " + end);
        result.put("estimatedTime", hours + "小时");
        result.put("estimatedCost", cost.setScale(0, RoundingMode.HALF_UP));
        result.put("distance", distance.setScale(0, RoundingMode.HALF_UP));
        result.put("fuelConsumption", fuel.setScale(1, RoundingMode.HALF_UP));
        result.put("carbonEmission", carbon.setScale(1, RoundingMode.HALF_UP));
        return ApiResponse.success("成功", result);
    }

    private BigDecimal toDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof Number n) return new BigDecimal(n.toString());
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal estimateDistance(String start, String end) {
        int h = Math.abs((start + "|" + end).hashCode());
        int km = 300 + (h % 1200);
        return new BigDecimal(km);
    }
}

