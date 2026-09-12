package com.hxcoe.erp.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "aps-service", path = "/api/v1/aps/resource-load", contextId = "erpApsResourceLoadClient")
public interface ApsResourceLoadClient {
    @GetMapping
    Result<Object> list(
            @RequestParam(value = "planId", required = false) Long planId,
            @RequestParam(value = "resourceIds", required = false) List<Long> resourceIds,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "timeScale", required = false) String timeScale
    );
}

