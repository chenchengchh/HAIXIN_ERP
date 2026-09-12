package com.hxcoe.scm.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.dto.srm.PurchaseOrderEventRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "srm-service", path = "/api/v1/srm/integration", contextId = "scmSrmIntegrationClient")
public interface SrmIntegrationClient {
    @PostMapping("/scm/purchase-order-events")
    Result<Map<String, Object>> receivePurchaseOrderEvent(@RequestBody PurchaseOrderEventRequest req);
}

