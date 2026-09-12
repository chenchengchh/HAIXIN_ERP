package com.hxcoe.srm.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.srm.dto.integration.SupplierEventRequest;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "scm-service", path = "/api/v1/scm/integration", contextId = "srmScmSupplierEventClient")
public interface ScmSupplierEventClient {

    @PostMapping("/srm/supplier-events")
    Result<Map<String, Object>> receiveSupplierEvent(@RequestBody SupplierEventRequest request);
}
