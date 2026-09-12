package com.hxcoe.erp.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wms-service", path = "/api/v1/wms/stock/count-jobs", contextId = "erpWmsStockCountClient")
public interface WmsStockCountClient {
    @PostMapping
    Result<Object> create(@RequestBody Object body);
}

