package com.hxcoe.wms.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.wms.client.dto.ScmOutboundShippedRequest;
import com.hxcoe.wms.client.dto.ScmReceiptCompletedRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "scm-service", path = "/api/v1/scm/integration", contextId = "wmsScmIntegrationClient")
public interface ScmIntegrationClient {
    @PostMapping("/wms/receipt-completed")
    Result<Map<String, Object>> receiptCompleted(@RequestBody ScmReceiptCompletedRequest req);

    @PostMapping("/wms/outbound-shipped")
    Result<Map<String, Object>> outboundShipped(@RequestBody ScmOutboundShippedRequest req);
}

