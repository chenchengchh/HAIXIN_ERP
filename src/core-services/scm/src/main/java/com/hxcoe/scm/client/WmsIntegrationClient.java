package com.hxcoe.scm.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.dto.srm.PurchaseOrderEventRequest;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "wms-service", url = "${scm.integration.wms-base-url:http://wms:8086}", path = "/api/v1/wms/integration", contextId = "scmWmsIntegrationClient")
public interface WmsIntegrationClient {

    @PostMapping("/scm/purchase-order-events")
    Result<Map<String, Object>> receiveScmPoEvent(@RequestBody PurchaseOrderEventRequest req);
}
