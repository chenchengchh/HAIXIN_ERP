package com.hxcoe.erp.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "wms-service", path = "/api/v1/wms/inbound/asn", contextId = "erpWmsInboundAsnClient")
public interface WmsInboundAsnClient {
    @GetMapping
    Result<Object> list(@RequestParam("page") int page, @RequestParam("size") int size);

    @GetMapping("/{id}")
    Result<Object> detail(@PathVariable("id") Long id);

    @PostMapping
    Result<Object> create(@RequestBody Object body);

    @PutMapping("/{id}")
    Result<Object> update(@PathVariable("id") Long id, @RequestBody Object body);

    @DeleteMapping("/{id}")
    Result<Object> delete(@PathVariable("id") Long id);

    @PutMapping("/{id}/confirm")
    Result<Object> confirm(@PathVariable("id") Long id);

    @PutMapping("/{id}/receive")
    Result<Object> receive(@PathVariable("id") Long id);

    @PutMapping("/{id}/start")
    Result<Object> start(@PathVariable("id") Long id);

    @PutMapping("/{id}/cancel")
    Result<Object> cancel(@PathVariable("id") Long id);
}
