package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.MrpPlanEntity;
import com.hxcoe.scm.entity.MrpResultEntity;
import com.hxcoe.scm.service.MrpEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * MRP运算控制器
 */
@RestController
@RequestMapping({"/api/v1/scm/mrp", "/api/scm/mrp"})
public class MrpController {

    @Autowired
    private MrpEngineService mrpEngineService;

    /**
     * 运行MRP
     */
    @PostMapping("/run")
    public ApiResponse<MrpPlanEntity> runMrp(@RequestBody Map<String, Object> params) {
        MrpPlanEntity plan = mrpEngineService.runMrp(params);
        return success("MRP运算已启动", plan);
    }

    /**
     * 获取运算历史
     */
    @GetMapping("/history")
    public ApiResponse<PageResult<MrpPlanEntity>> getMrpHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "runDate"));
        Page<MrpPlanEntity> pageData = mrpEngineService.getMrpHistory(pageable);
        
        return success("MRP历史查询成功", PageResult.build(
            pageData.getTotalElements(), 
            pageData.getSize(), 
            pageData.getNumber() + 1, 
            pageData.getContent()
        ));
    }

    /**
     * 获取运算结果详情
     */
    @GetMapping("/results/{planId}")
    public ApiResponse<List<MrpResultEntity>> getMrpResults(@PathVariable Long planId) {
        List<MrpResultEntity> results = mrpEngineService.getMrpResults(planId);
        return success("MRP结果查询成功", results);
    }

    @GetMapping("/results")
    public ApiResponse<PageResult<MrpResultEntity>> queryMrpResults(
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String materialKeyword,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<MrpResultEntity> pageData = mrpEngineService.queryMrpResults(planId, status, materialKeyword, type, pageable);
        return success("MRP结果分页查询成功", PageResult.build(
                pageData.getTotalElements(),
                pageData.getSize(),
                pageData.getNumber() + 1,
                pageData.getContent()
        ));
    }

    @PutMapping("/results/{id}/confirm")
    public ApiResponse<MrpResultEntity> confirmResult(@PathVariable Long id, @RequestParam(required = false) String operator) {
        try {
            MrpResultEntity updated = mrpEngineService.confirmMrpResult(id, operator == null ? "SYSTEM" : operator);
            if (updated == null) {
                return notFound("结果不存在");
            }
            return success("确认成功", updated);
        } catch (IllegalStateException e) {
            return badRequest(e.getMessage());
        }
    }

    @PutMapping("/results/{id}/reject")
    public ApiResponse<MrpResultEntity> rejectResult(@PathVariable Long id, @RequestParam String reason, @RequestParam(required = false) String operator) {
        try {
            MrpResultEntity updated = mrpEngineService.rejectMrpResult(id, reason, operator == null ? "SYSTEM" : operator);
            if (updated == null) {
                return notFound("结果不存在");
            }
            return success("拒绝成功", updated);
        } catch (IllegalStateException e) {
            return badRequest(e.getMessage());
        }
    }

    @PostMapping("/results/batch/confirm")
    public ApiResponse<List<MrpResultEntity>> batchConfirm(@RequestBody BatchIdsRequest body, @RequestParam(required = false) String operator) {
        List<MrpResultEntity> updated = mrpEngineService.batchConfirmMrpResults(body == null ? null : body.getIds(), operator == null ? "SYSTEM" : operator);
        return success("批量确认成功", updated);
    }

    @PostMapping("/results/batch/reject")
    public ApiResponse<List<MrpResultEntity>> batchReject(@RequestBody BatchRejectRequest body, @RequestParam(required = false) String operator) {
        List<MrpResultEntity> updated = mrpEngineService.batchRejectMrpResults(body == null ? null : body.getIds(), body == null ? null : body.getReason(), operator == null ? "SYSTEM" : operator);
        return success("批量拒绝成功", updated);
    }

    /**
     * 释放/下达MRP计划
     */
    @PostMapping("/release/{planId}")
    public ApiResponse<Void> releasePlan(@PathVariable Long planId) {
        mrpEngineService.releasePlan(planId);
        return success("计划释放成功", null);
    }

    /**
     * 删除MRP计划（连同关联的运算结果）
     */
    @DeleteMapping("/history/{planId}")
    public ApiResponse<Boolean> deleteMrpPlan(@PathVariable Long planId) {
        boolean ok = mrpEngineService.deleteMrpPlan(planId);
        if (ok) {
            return success("删除成功", true);
        }
        return notFound("MRP计划不存在");
    }

    public static class BatchIdsRequest {
        private List<Long> ids;

        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }
    }

    public static class BatchRejectRequest {
        private List<Long> ids;
        private String reason;

        public List<Long> getIds() {
            return ids;
        }

        public void setIds(List<Long> ids) {
            this.ids = ids;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.error(400, message);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}
