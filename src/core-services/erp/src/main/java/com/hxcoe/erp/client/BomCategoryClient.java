package com.hxcoe.erp.client;

import com.hxcoe.common.api.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "bom-service", path = "/api/v1/bom/category", contextId = "erpBomCategoryClient")
public interface BomCategoryClient {

    @GetMapping("/tree")
    ApiResponse<Object> tree();
}
