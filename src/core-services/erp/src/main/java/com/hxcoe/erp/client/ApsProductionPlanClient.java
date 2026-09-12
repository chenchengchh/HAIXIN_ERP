package com.hxcoe.erp.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "aps-service", path = "/api/v1/aps/production-plans", contextId = "erpApsProductionPlanClient")
public interface ApsProductionPlanClient {

    @GetMapping("/page")
    Result<Object> page(@RequestParam("page") Integer page, @RequestParam("size") Integer size);
}

