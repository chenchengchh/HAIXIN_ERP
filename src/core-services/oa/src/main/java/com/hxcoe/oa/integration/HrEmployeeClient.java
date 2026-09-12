package com.hxcoe.oa.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.Result;
import com.hxcoe.oa.integration.client.HrEmployeeFeignClient;
import com.hxcoe.oa.integration.dto.HrEmployeeDTO;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class HrEmployeeClient {

    @Autowired
    private HrEmployeeFeignClient hrEmployeeFeignClient;

    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> getEmployeeById(Long employeeId, String traceId) {
        if (employeeId == null) {
            return null;
        }
        HrEmployeeDTO employee = unwrap(
                hrEmployeeFeignClient.getById(employeeId, normalizeTraceId(traceId))
        );
        if (employee == null) {
            return null;
        }
        return objectMapper.convertValue(employee, new TypeReference<Map<String, Object>>() {});
    }

    public List<Map<String, Object>> listEmployees(String traceId) {
        List<HrEmployeeDTO> employees = unwrap(
                hrEmployeeFeignClient.listAll(normalizeTraceId(traceId))
        );
        if (employees == null || employees.isEmpty()) {
            return Collections.emptyList();
        }
        return objectMapper.convertValue(employees, new TypeReference<List<Map<String, Object>>>() {});
    }

    private <T> T unwrap(Result<T> result) {
        if (result == null || !ResponseStatusAdapter.isSuccess(result.getCode())) {
            return null;
        }
        return result.getData();
    }

    private String normalizeTraceId(String traceId) {
        return StringUtils.hasText(traceId) ? traceId : null;
    }
}
