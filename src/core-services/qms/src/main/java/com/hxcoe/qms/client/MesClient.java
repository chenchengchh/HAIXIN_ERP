package com.hxcoe.qms.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.qms.client.dto.mes.InspectionResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "mes-service")
public interface MesClient {
    @PostMapping("/api/mes/work-orders/inspection-result")
    Result<Void> submitInspectionResult(@RequestBody InspectionResultDTO resultDTO);
}
