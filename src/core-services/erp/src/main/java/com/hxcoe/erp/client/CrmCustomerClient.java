package com.hxcoe.erp.client;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "crm-service", path = "/api/v1/crm/customer", contextId = "erpCrmCustomerClient")
public interface CrmCustomerClient {

    @GetMapping("/list")
    Result<PageResult<Object>> list(@RequestParam Map<String, Object> params);

    @GetMapping("/{id}")
    Result<Object> getById(@PathVariable("id") Long id);

    @PostMapping
    Result<Object> create(@RequestBody Map<String, Object> body);

    @PutMapping
    Result<Object> update(@RequestBody Map<String, Object> body);

    @DeleteMapping("/{id}")
    Result<Boolean> delete(@PathVariable("id") Long id);
}

