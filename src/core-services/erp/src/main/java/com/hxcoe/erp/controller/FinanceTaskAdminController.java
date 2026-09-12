package com.hxcoe.erp.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.erp.entity.FinanceTaskEntity;
import com.hxcoe.erp.repository.FinanceTaskRepository;
import com.hxcoe.erp.service.FinanceTaskProcessorService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/erp/admin/finance-tasks", "/erp/admin/finance-tasks"})
public class FinanceTaskAdminController {

    @Autowired
    private FinanceTaskProcessorService financeTaskProcessorService;

    @Autowired
    private FinanceTaskRepository financeTaskRepository;

    @PostMapping("/process-batch")
    public ApiResponse<Map<String, Object>> processBatch() {
        int processed = financeTaskProcessorService.tryProcessPendingBatch();
        Map<String, Object> data = new HashMap<>();
        data.put("processed", processed);
        return ApiResponse.success("成功", data);
    }

    @PostMapping("/process/{id}")
    public ApiResponse<Map<String, Object>> processOne(@PathVariable("id") Long id) {
        boolean ok = financeTaskProcessorService.tryProcessOne(id);
        Map<String, Object> data = new HashMap<>();
        data.put("taskId", id);
        data.put("processed", ok);
        return ApiResponse.success("成功", data);
    }

    @GetMapping("/{id}")
    public ApiResponse<FinanceTaskEntity> getById(@PathVariable("id") Long id) {
        FinanceTaskEntity task = financeTaskRepository.findById(id).orElse(null);
        if (task == null) {
            return ApiResponse.error(404, "未找到");
        }
        return ApiResponse.success("成功", task);
    }
}
