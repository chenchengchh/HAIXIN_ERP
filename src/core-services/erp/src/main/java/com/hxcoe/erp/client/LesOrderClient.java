package com.hxcoe.erp.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "les-service", path = "/api/v1/les/orders", contextId = "erpLesOrderClient")
public interface LesOrderClient {
    @PostMapping
    Result<Object> create(@RequestBody Object body);
}

