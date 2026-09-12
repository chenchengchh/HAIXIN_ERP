package com.hxcoe.erp.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "aps-service", path = "/api/v1/aps/schedule-details", contextId = "erpApsScheduleDetailClient")
public interface ApsScheduleDetailClient {

    @GetMapping("/plan/{planId}")
    Result<Object> listByPlan(@PathVariable("planId") Long planId);
}

