package com.hxcoe.scm.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.dto.srm.PurchaseOrderEventRequest;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-service", url = "${scm.integration.erp-base-url:http://erp:8081}", path = "/api/v1/erp/integration", contextId = "scmErpPoIntegrationClient")
public interface ErpPurchaseOrderIntegrationClient {

    @PostMapping("/scm/purchase-order-events")
    Result<Map<String, Object>> receiveScmPoEvent(@RequestBody PurchaseOrderEventRequest req);
}
