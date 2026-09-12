package com.hxcoe.erp.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "eam-service", path = "/api/v1/eam/assets", contextId = "erpEamAssetClient")
public interface EamAssetClient {
    @GetMapping
    Result<Object> list();

    @GetMapping("/{id}")
    Result<Object> getById(@PathVariable("id") Long id);
}
