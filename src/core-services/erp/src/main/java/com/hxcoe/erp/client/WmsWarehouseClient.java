package com.hxcoe.erp.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "wms-service", path = "/api/v1/wms/warehouse", contextId = "erpWmsWarehouseClient")
public interface WmsWarehouseClient {
    @GetMapping("/list")
    Result<Object> list(@RequestParam("page") int page, @RequestParam("size") int size);

    @PostMapping("/create")
    Result<Object> create(@RequestBody Object body);

    @PutMapping("/{id}")
    Result<Object> update(@PathVariable("id") Long id, @RequestBody Object body);

    @DeleteMapping("/{id}")
    Result<Object> delete(@PathVariable("id") Long id);
}

