package com.hxcoe.scm.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.dto.WmsInventoryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "wms-service")
public interface WmsClient {
    @GetMapping("/wms/inventory/material/{materialCode}")
    Result<List<WmsInventoryDTO>> getInventoryByMaterialCode(@PathVariable("materialCode") String materialCode);

    @GetMapping("/wms/warehouses")
    Result<List<Map<String, Object>>> getWarehouses();

    @GetMapping("/wms/inventory/transactions")
    Result<List<Map<String, Object>>> getTransactions(@RequestParam("materialCode") String materialCode, @RequestParam("days") Integer days);
}
