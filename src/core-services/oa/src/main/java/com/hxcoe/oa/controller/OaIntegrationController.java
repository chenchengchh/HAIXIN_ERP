package com.hxcoe.oa.controller;

import com.hxcoe.common.dto.hr.EmployeeEventDTO;
import com.hxcoe.common.result.Result;
import com.hxcoe.oa.iam.service.OaHrEventSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * OA 集成入口控制器。
 * <p>接收上游服务（HR）的员工事件，同步 OA 账号启停状态。</p>
 */
@RestController
@RequestMapping("/api/v1/oa/integration")
public class OaIntegrationController {

    @Autowired
    private OaHrEventSyncService oaHrEventSyncService;

    /**
     * 接收 HR 员工事件，同步 OA 账号状态。
     * <p>P2-C: HR→OA 员工事件落地入口。入职启用账号、离职停用账号、调动记录日志。</p>
     *
     * @param req HR 员工事件
     * @return 处理结果
     */
    @PostMapping("/hr/employee-event")
    public Result<Map<String, Object>> receiveHrEmployeeEvent(@RequestBody EmployeeEventDTO req) {
        if (req == null || req.getEmployee() == null) {
            return Result.fail("员工载荷不能为空");
        }
        if (req.getEventType() == null || req.getEventType().isBlank()) {
            return Result.fail("eventType不能为空");
        }
        boolean ok = oaHrEventSyncService.applyEmployeeEvent(req);
        if (!ok) {
            return Result.fail("员工事件处理失败");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", req.getEventId());
        data.put("idempotencyKey", req.getIdempotencyKey());
        data.put("eventType", req.getEventType());
        data.put("employeeNo", req.getEmployee().getEmployeeNo());
        return Result.success("成功", data);
    }
}
