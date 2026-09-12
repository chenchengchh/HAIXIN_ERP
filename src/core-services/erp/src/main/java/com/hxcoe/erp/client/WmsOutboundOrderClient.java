package com.hxcoe.erp.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "wms-service", path = "/api/v1/wms/outbound/orders", contextId = "erpWmsOutboundOrderClient")
public interface WmsOutboundOrderClient {
    @GetMapping
    Result<Object> list(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "orderType", required = false) String orderType,
            @RequestParam(value = "outboundNo", required = false) String outboundNo,
            @RequestParam(value = "waveId", required = false) Long waveId
    );

    @PostMapping
    Result<Object> create(@RequestBody Object body);

    @GetMapping("/{id}")
    Result<Object> detail(@PathVariable("id") Long id);

    @PutMapping("/{id}/approve")
    Result<Object> approve(@PathVariable("id") Long id);

    @PostMapping("/{id}/ship")
    Result<Object> ship(@PathVariable("id") Long id);

    @PutMapping("/{id}/cancel")
    Result<Object> cancel(@PathVariable("id") Long id);
}
