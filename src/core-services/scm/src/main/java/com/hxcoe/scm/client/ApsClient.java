package com.hxcoe.scm.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.dto.aps.ProductionPlanDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "aps-service")
public interface ApsClient {
    @PostMapping("/api/v1/aps/production-plans")
    Result<ProductionPlanDTO> createProductionPlan(@RequestBody ProductionPlanDTO plan);
}
