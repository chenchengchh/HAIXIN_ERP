package com.hxcoe.oa.integration.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.oa.integration.dto.HrDepartmentDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "hr-service", path = "/api/v1/hr/departments", contextId = "oaHrDepartmentFeignClient")
public interface HrDepartmentFeignClient {

    @GetMapping("/{id}")
    Result<HrDepartmentDTO> getById(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId
    );

    @GetMapping("/code/{departmentCode}")
    Result<HrDepartmentDTO> getByCode(
            @PathVariable("departmentCode") String departmentCode,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId
    );

    @GetMapping
    Result<List<HrDepartmentDTO>> listAll(
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId
    );
}
