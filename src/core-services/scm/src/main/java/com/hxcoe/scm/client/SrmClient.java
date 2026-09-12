package com.hxcoe.scm.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.dto.srm.PurchaseRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "srm-service")
public interface SrmClient {
    @PostMapping("/api/v1/srm/purchase-requests")
    Result<PurchaseRequestDTO> createPurchaseRequest(@RequestBody PurchaseRequestDTO request);

    @GetMapping("/api/v1/srm/suppliers")
    Result<Map<String, Object>> getSuppliers(@RequestParam(value = "page", required = false) Integer page,
                                             @RequestParam(value = "size", required = false) Integer size);

    @GetMapping("/api/v1/srm/purchase-orders")
    Result<Map<String, Object>> getPurchaseOrders(@RequestParam(value = "page", required = false) Integer page, 
                                                  @RequestParam(value = "size", required = false) Integer size);

    @GetMapping("/api/v1/srm/purchase-orders/{id}")
    Result<Map<String, Object>> getPurchaseOrderById(@PathVariable("id") Long id);
}
