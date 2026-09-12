package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.InspectionResultEntity;
import com.hxcoe.qms.service.InspectionResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 检验结果管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/inspection-results")
public class InspectionResultController {

    @Autowired
    private InspectionResultService inspectionResultService;

    /**
     * 分页查询检验结果列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param resultNo 结果编号（模糊）
     * @param taskNo 任务编号（模糊）
     * @param auditStatus 审核状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<InspectionResultEntity>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String resultNo,
            @RequestParam(required = false) String taskNo,
            @RequestParam(required = false) String auditStatus
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        return inspectionResultService.page(resultNo, taskNo, auditStatus, pageable);
    }

    /**
     * 获取检验结果详情
     *
     * @param id 结果ID
     * @return 结果详情
     */
    @GetMapping("/{id}")
    public Result<InspectionResultEntity> getById(@PathVariable Long id) {
        return inspectionResultService.getById(id);
    }

    /**
     * 创建检验结果
     *
     * @param entity 结果数据
     * @return 创建结果
     */
    @PostMapping
    public Result<InspectionResultEntity> create(@RequestBody InspectionResultEntity entity) {
        return inspectionResultService.create(entity);
    }

    /**
     * 更新检验结果
     *
     * @param id 结果ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<InspectionResultEntity> update(@PathVariable Long id, @RequestBody InspectionResultEntity entity) {
        return inspectionResultService.update(id, entity);
    }

    /**
     * 审核检验结果
     *
     * @param id 结果ID
     * @param body 审核数据
     * @return 审核结果
     */
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String auditStatus = body == null ? null : String.valueOf(body.get("auditStatus"));
        String auditRemark = body == null ? null : (body.get("auditRemark") == null ? null : String.valueOf(body.get("auditRemark")));
        String auditor = body == null ? null : (body.get("auditor") == null ? null : String.valueOf(body.get("auditor")));
        return inspectionResultService.audit(id, auditStatus, auditRemark, auditor);
    }

    /**
     * 批量审核检验结果
     *
     * @param body 批量审核数据
     * @return 处理结果
     */
    @PutMapping("/batch-audit")
    public Result<Void> batchAudit(@RequestBody Map<String, Object> body) {
        Object idsObj = body == null ? null : body.get("ids");
        List<Long> ids = idsObj instanceof List<?> list ? list.stream().map(v -> Long.valueOf(String.valueOf(v))).toList() : List.of();
        String auditStatus = body == null ? null : String.valueOf(body.get("auditStatus"));
        String auditRemark = body == null ? null : (body.get("auditRemark") == null ? null : String.valueOf(body.get("auditRemark")));
        return inspectionResultService.batchAudit(ids, auditStatus, auditRemark);
    }
}
