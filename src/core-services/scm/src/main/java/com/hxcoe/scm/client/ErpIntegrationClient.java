package com.hxcoe.scm.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.dto.erp.PoFactRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "erp-service", path = "/api/v1/erp/integration", contextId = "scmErpIntegrationClient")
public interface ErpIntegrationClient {
    @PostMapping("/scm/po-facts")
    Result<Map<String, Object>> receivePoFacts(@RequestBody PoFactRequest req);
}

