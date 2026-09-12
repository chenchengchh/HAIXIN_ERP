package com.hxcoe.oa.integration.client;

import com.hxcoe.common.result.Result;
import com.hxcoe.oa.integration.dto.HrEmployeeDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "hr-service", path = "/api/v1/hr/employees", contextId = "oaHrEmployeeFeignClient")
public interface HrEmployeeFeignClient {

    @GetMapping("/{id}")
    Result<HrEmployeeDTO> getById(
            @PathVariable("id") Long id,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId
    );

    @GetMapping("/code/{employeeCode}")
    Result<HrEmployeeDTO> getByCode(
            @PathVariable("employeeCode") String employeeCode,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId
    );

    @GetMapping
    Result<List<HrEmployeeDTO>> listAll(
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId
    );

    @GetMapping("/department/{departmentId}")
    Result<List<HrEmployeeDTO>> listByDepartmentId(
            @PathVariable("departmentId") Long departmentId,
            @RequestHeader(value = "X-Trace-Id", required = false) String traceId
    );
}
