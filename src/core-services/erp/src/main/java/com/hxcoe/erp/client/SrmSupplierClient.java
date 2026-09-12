package com.hxcoe.erp.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "srm-service", path = "/api/v1/srm/suppliers", contextId = "erpSrmSupplierClient")
public interface SrmSupplierClient {
    @GetMapping
    Result<Object> page(@RequestParam("page") int page, @RequestParam("size") int size);

    @GetMapping("/{id}")
    Result<Object> detail(@PathVariable("id") Long id);
}
