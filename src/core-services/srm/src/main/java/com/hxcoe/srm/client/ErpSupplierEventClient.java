package com.hxcoe.srm.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.srm.dto.integration.SupplierEventRequest;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-service", path = "/api/v1/erp/integration", contextId = "srmErpSupplierEventClient")
public interface ErpSupplierEventClient {

    @PostMapping("/srm/supplier-events")
    Result<Map<String, Object>> receiveSupplierEvent(@RequestBody SupplierEventRequest request);
}
