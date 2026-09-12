package com.hxcoe.erp.client;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "hr-service", path = "/api/v1/hr/employees", contextId = "erpHrEmployeeClient")
public interface HrEmployeeClient {

    @GetMapping("/page")
    Result<PageResult<Object>> page(@RequestParam Map<String, Object> params);

    @GetMapping("/{id}")
    Result<Object> getById(@PathVariable("id") Long id);

    @PostMapping
    Result<Object> create(@RequestBody Map<String, Object> body);

    @PutMapping("/{id}")
    Result<Object> update(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    @DeleteMapping("/{id}")
    Result<Void> delete(@PathVariable("id") Long id);
}
