package com.hxcoe.hr.client;

import com.hxcoe.common.dto.hr.EmployeeEventDTO;
import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * HR → OA 员工事件 Feign 客户端。
 * <p>HR 员工入职/调动/离职事件推送 OA，同步账号启用/停用状态。</p>
 */
@FeignClient(name = "oa-service", path = "/api/v1/oa/integration", contextId = "hrOaEventClient")
public interface OaHrEventClient {

    /**
     * 推送员工事件到 OA。
     *
     * @param event 员工事件
     * @return OA 处理结果
     */
    @PostMapping("/hr/employee-event")
    Result<Map<String, Object>> employeeEvent(@RequestBody EmployeeEventDTO event);
}
