package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.InspectionTaskEntity;
import com.hxcoe.qms.service.InspectionTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 检验任务管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/inspection-tasks")
public class InspectionTaskController {

    @Autowired
    private InspectionTaskService inspectionTaskService;

    /**
     * 分页查询检验任务列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param taskNo 任务编号（模糊）
     * @param status 状态
     * @param planId 计划ID
     * @param materialCode 物料编码
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<InspectionTaskEntity>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String taskNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) String materialCode
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        return inspectionTaskService.page(taskNo, status, planId, materialCode, pageable);
    }

    /**
     * 获取检验任务详情
     *
     * @param id 任务ID
     * @return 任务详情
     */
    @GetMapping("/{id}")
    public Result<InspectionTaskEntity> getById(@PathVariable Long id) {
        return inspectionTaskService.getById(id);
    }

    /**
     * 创建检验任务
     *
     * @param entity 任务数据
     * @return 创建结果
     */
    @PostMapping
    public Result<InspectionTaskEntity> create(@RequestBody InspectionTaskEntity entity) {
        return inspectionTaskService.create(entity);
    }

    /**
     * 更新检验任务
     *
     * @param id 任务ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<InspectionTaskEntity> update(@PathVariable Long id, @RequestBody InspectionTaskEntity entity) {
        return inspectionTaskService.update(id, entity);
    }

    /**
     * 删除检验任务
     *
     * @param id 任务ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return inspectionTaskService.delete(id);
    }

    /**
     * 分配检验任务
     *
     * @param id 任务ID
     * @param body 分配数据
     * @return 分配结果
     */
    @PutMapping("/{id}/assign")
    public Result<Void> assign(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object assignee = body == null ? null : body.get("assignee");
        return inspectionTaskService.assign(id, assignee == null ? null : String.valueOf(assignee));
    }

    /**
     * 取消检验任务
     *
     * @param id 任务ID
     * @return 取消结果
     */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        return inspectionTaskService.cancel(id);
    }

    /**
     * 自动分配检验任务
     *
     * @param params 分配参数
     * @return 处理结果
     */
    @PostMapping("/auto-assign")
    public Result<Map<String, Object>> autoAssign(@RequestBody(required = false) Map<String, Object> params) {
        return inspectionTaskService.autoAssign(params);
    }
}
