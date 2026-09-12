package com.hxcoe.erp.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "crm-service", path = "/api/v1/crm/orders", contextId = "erpCrmOrderClient")
public interface CrmOrderClient {
    @GetMapping("/my")
    Result<Object> myOrders(
            @RequestParam("page") Integer page,
            @RequestParam("size") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "customerName", required = false) String customerName
    );

    @GetMapping("/{id}")
    Result<Object> getById(@PathVariable("id") Long id);

    @PostMapping
    Result<Object> create(@RequestBody Object body);

    @PutMapping("/{id}")
    Result<Object> update(@PathVariable("id") Long id, @RequestBody Object body);

    @DeleteMapping("/{id}")
    Result<Object> delete(@PathVariable("id") Long id);

    @PostMapping("/{id}/submit")
    Result<Object> submit(@PathVariable("id") Long id);

    @PostMapping("/{id}/approve")
    Result<Object> approve(@PathVariable("id") Long id);
}
