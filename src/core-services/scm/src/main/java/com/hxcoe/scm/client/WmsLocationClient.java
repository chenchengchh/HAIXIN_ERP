package com.hxcoe.scm.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "wms-service", path = "/api/v1/wms/base/locations", contextId = "scmWmsLocationClient")
public interface WmsLocationClient {
    @GetMapping
    Result<Object> page(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "warehouseCode", required = false) String warehouseCode,
            @RequestParam(value = "zoneCode", required = false) String zoneCode,
            @RequestParam(value = "locationTypeCode", required = false) String locationTypeCode,
            @RequestParam(value = "status", required = false) String status
    );

    @GetMapping("/{id}")
    Result<Object> detail(@PathVariable("id") Long id);
}
