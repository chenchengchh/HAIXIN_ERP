package com.hxcoe.wms.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.wms.client.dto.CrmOutboundShippedRequest;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "crm-service", path = "/api/v1/crm/integration", contextId = "wmsCrmIntegrationClient")
public interface CrmIntegrationClient {
    @PostMapping("/wms/outbound-shipped")
    Result<Map<String, Object>> outboundShipped(@RequestBody CrmOutboundShippedRequest req);
}
