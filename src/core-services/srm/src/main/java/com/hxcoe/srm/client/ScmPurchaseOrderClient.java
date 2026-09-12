package com.hxcoe.srm.client;

import com.hxcoe.common.result.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "scm-service", path = "/api/v1/scm/purchase-orders", contextId = "srmScmPurchaseOrderClient")
public interface ScmPurchaseOrderClient {

    @GetMapping
    Result<Object> getOrders(@RequestParam("page") Integer page, @RequestParam("size") Integer size);

    @GetMapping("/by-no/{orderNo}")
    Result<Object> getByOrderNo(@PathVariable("orderNo") String orderNo);

    @PostMapping("/by-no/{orderNo}/supplier/confirm")
    Result<Object> supplierConfirm(@PathVariable("orderNo") String orderNo, @RequestBody Map<String, Object> body);
}
