package com.hxcoe.hr.client;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.dto.hr.EmployeeEventDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * HR → ERP 员工事件 Feign 客户端。
 * <p>HR 员工入职/调动/离职事件推送 ERP，同步员工主数据镜像。</p>
 */
@FeignClient(name = "erp-service", path = "/api/v1/erp/integration", contextId = "hrErpEventClient")
public interface ErpHrEventClient {

    /**
     * 推送员工事件到 ERP。
     *
     * @param event 员工事件
     * @return ERP 处理结果
     */
    @PostMapping("/hr/employee-event")
    ApiResponse<Map<String, Object>> employeeEvent(@RequestBody EmployeeEventDTO event);
}
