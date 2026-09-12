package com.hxcoe.qms.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.qms.client.dto.scm.IqcCompletedRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "scm-service", path = "/api/v1/scm/integration", contextId = "qmsScmIntegrationClient")
public interface ScmIntegrationClient {
    @PostMapping("/qms/iqc-completed")
    Result<Map<String, Object>> iqcCompleted(@RequestBody IqcCompletedRequest req);
}

