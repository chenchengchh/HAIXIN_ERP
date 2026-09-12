package com.hxcoe.erp.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "wms-service", path = "/api/v1/wms/inventory", contextId = "erpWmsInventoryClient")
public interface WmsInventoryClient {
    @GetMapping("/list")
    Result<Object> list(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam(value = "warehouseCode", required = false) String warehouseCode,
            @RequestParam(value = "locationCode", required = false) String locationCode,
            @RequestParam(value = "materialCode", required = false) String materialCode,
            @RequestParam(value = "materialName", required = false) String materialName,
            @RequestParam(value = "status", required = false) String status
    );
}

