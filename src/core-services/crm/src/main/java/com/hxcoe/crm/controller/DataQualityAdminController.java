package com.hxcoe.crm.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.crm.service.CrmDataQualityService;
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
@RequestMapping("/api/v1/crm/admin/data-quality")
public class DataQualityAdminController {

    @Autowired
    private CrmDataQualityService crmDataQualityService;

    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh() {
        return Result.success("刷新成功", crmDataQualityService.refresh());
    }

    @GetMapping("/tasks")
    public Result<PageResult<Map<String, Object>>> tasks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status
    ) {
        return Result.success("查询成功", crmDataQualityService.listTasks(page, size, status));
    }

    @PostMapping("/tasks/{id}/resolve")
    public Result<Boolean> resolve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String resolution = body == null ? null : (body.get("resolution") == null ? null : String.valueOf(body.get("resolution")));
        String status = body == null ? null : (body.get("status") == null ? null : String.valueOf(body.get("status")));
        boolean ok = crmDataQualityService.resolveTask(id, resolution, status);
        return ok ? Result.success("处理成功", true) : Result.fail("处理失败");
    }

    @GetMapping("/metrics")
    public Result<List<Map<String, Object>>> metrics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        return Result.success("查询成功", crmDataQualityService.listMetrics(date));
    }
}

