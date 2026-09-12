package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.PoReconciliationDiffEntity;
import com.hxcoe.scm.repository.PoReconciliationDiffRepository;
import com.hxcoe.scm.service.PoMigrationReconciliationAdminService;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/scm/admin/po-migration", "/scm/admin/po-migration"})
public class PoMigrationAdminController {

    @Autowired
    private PoMigrationReconciliationAdminService poMigrationReconciliationAdminService;

    @Autowired
    private PoReconciliationDiffRepository poReconciliationDiffRepository;

    @PostMapping("/migrate-from-srm")
    public ApiResponse<Map<String, Object>> migrateFromSrm() {
        Map<String, Object> data = poMigrationReconciliationAdminService.migrateFromSrmToScm();
        return success("成功", data);
    }

    @PostMapping("/reconciliation/refresh")
    public ApiResponse<Map<String, Object>> refreshReconciliationDiffs() {
        Map<String, Object> data = poMigrationReconciliationAdminService.refreshSrmVsScmDiffs();
        return success("成功", data);
    }

    @GetMapping("/reconciliation/diffs/page")
    public ApiResponse<PageResult<PoReconciliationDiffEntity>> diffsPage(@RequestParam(defaultValue = "1") Integer page,
                                                                         @RequestParam(defaultValue = "20") Integer size,
                                                                         @RequestParam(defaultValue = "OPEN") String status) {
        int p = page == null || page <= 0 ? 1 : page;
        int s = size == null || size <= 0 ? 20 : size;
        String st = status == null || status.isBlank() ? "OPEN" : status.trim().toUpperCase();
        Pageable pageable = PageRequest.of(p - 1, s);
        Page<PoReconciliationDiffEntity> res = poReconciliationDiffRepository.findByStatusOrderByUpdatedTimeDesc(st, pageable);
        return success("成功", PageResult.build(res.getTotalElements(), s, p, res.getContent()));
    }

    @PostMapping("/reconciliation/diffs/{id}/resolve")
    public ApiResponse<Map<String, Object>> resolve(@PathVariable Long id) {
        return updateStatus(id, "RESOLVED");
    }

    @PostMapping("/reconciliation/diffs/{id}/ignore")
    public ApiResponse<Map<String, Object>> ignore(@PathVariable Long id) {
        return updateStatus(id, "IGNORED");
    }

    @PostMapping("/reconciliation/diffs/{id}/reopen")
    public ApiResponse<Map<String, Object>> reopen(@PathVariable Long id) {
        return updateStatus(id, "OPEN");
    }

    private ApiResponse<Map<String, Object>> updateStatus(Long id, String status) {
        PoReconciliationDiffEntity e = id == null ? null : poReconciliationDiffRepository.findById(id).orElse(null);
        if (e == null) {
            return notFound("差异记录不存在");
        }
        e.setStatus(status);
        poReconciliationDiffRepository.save(e);
        Map<String, Object> data = new HashMap<>();
        data.put("id", e.getId());
        data.put("orderNo", e.getOrderNo());
        data.put("status", e.getStatus());
        return success("成功", data);
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}

