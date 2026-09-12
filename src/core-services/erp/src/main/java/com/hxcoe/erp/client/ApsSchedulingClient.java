package com.hxcoe.erp.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "aps-service", path = "/api/v1/aps", contextId = "erpApsSchedulingClient")
public interface ApsSchedulingClient {
    @PostMapping("/scheduling/execute")
    Result<Object> execute(@RequestBody Object params);

    @GetMapping("/scheduling/result/{planId}")
    Result<Object> result(@PathVariable("planId") Long planId);
}

