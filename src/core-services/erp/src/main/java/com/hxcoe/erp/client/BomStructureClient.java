package com.hxcoe.erp.client;

import com.hxcoe.common.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "bom-service", path = "/api/v1/bom/structure", contextId = "erpBomStructureClient")
public interface BomStructureClient {

    @PostMapping
    ApiResponse<Object> create(@RequestBody Map<String, Object> body);

    @PutMapping("/{id}")
    ApiResponse<Object> update(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable("id") Long id);

    @GetMapping("/header/{id}")
    ApiResponse<Object> getHeaderById(@PathVariable("id") Long id);

    @GetMapping("/material/{materialId}")
    ApiResponse<Object> getByMaterialId(@PathVariable("materialId") Long materialId);

    @GetMapping("/tree/{productId}")
    ApiResponse<Object> getTree(@PathVariable("productId") Long productId, @RequestParam(name = "version", required = false) String version);
}
