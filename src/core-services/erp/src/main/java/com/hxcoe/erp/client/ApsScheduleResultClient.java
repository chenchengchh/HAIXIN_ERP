package com.hxcoe.erp.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "aps-service", path = "/api/v1/aps/schedule-results", contextId = "erpApsScheduleResultClient")
public interface ApsScheduleResultClient {

    @GetMapping("/plan/{planId}")
    Result<Object> listByPlan(@PathVariable("planId") Long planId);

    @PostMapping("/{id}/release")
    Result<Object> release(@PathVariable("id") Long id);
}

