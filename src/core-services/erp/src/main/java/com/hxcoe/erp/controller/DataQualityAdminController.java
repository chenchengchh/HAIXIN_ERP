package com.hxcoe.erp.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.service.ErpDataQualityService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/erp/admin/data-quality")
public class DataQualityAdminController {

    @Autowired
    private ErpDataQualityService erpDataQualityService;

    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refresh() {
        return ApiResponse.success("刷新成功", erpDataQualityService.refresh());
    }

    @GetMapping("/tasks")
    public ApiResponse<PageResult<Map<String, Object>>> tasks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status
    ) {
        return ApiResponse.success("查询成功", erpDataQualityService.listTasks(page, size, status));
    }

    @PostMapping("/tasks/{id}/resolve")
    public ApiResponse<Boolean> resolve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String resolution = body == null ? null : (body.get("resolution") == null ? null : String.valueOf(body.get("resolution")));
        String status = body == null ? null : (body.get("status") == null ? null : String.valueOf(body.get("status")));
        boolean ok = erpDataQualityService.resolveTask(id, resolution, status);
        return ok ? ApiResponse.success("处理成功", true) : ApiResponse.error(400, "处理失败");
    }

    @GetMapping("/metrics")
    public ApiResponse<List<Map<String, Object>>> metrics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        return ApiResponse.success("查询成功", erpDataQualityService.listMetrics(date));
    }
}

