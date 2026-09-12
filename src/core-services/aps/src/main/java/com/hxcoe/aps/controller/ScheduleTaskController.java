package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.ScheduleTaskEntity;
import com.hxcoe.aps.service.ScheduleTaskService;
import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aps/schedule-tasks")
@Tag(name = "排程任务管理", description = "排程任务管理相关接口")
public class ScheduleTaskController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleTaskController.class);

    @Autowired
    private ScheduleTaskService scheduleTaskService;

    @PostMapping
    @Operation(summary = "创建排程任务", description = "创建新的排程任务")
    public Result<ScheduleTaskEntity> createTask(@RequestBody ScheduleTaskEntity task) {
        try {
            logger.info("创建排程任务");
            ScheduleTaskEntity createdTask = scheduleTaskService.createTask(task);
            return Result.success(createdTask);
        } catch (Exception e) {
            logger.error("创建排程任务失败", e);
            return Result.error("创建排程任务失败: " + e.getMessage());
        }
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "查询排程任务", description = "根据ID查询排程任务")
    public Result<ScheduleTaskEntity> getTaskById(
            @Parameter(description = "任务ID", required = true) @PathVariable Long id) {
        try {
            logger.info("查询排程任务: id={}", id);
            ScheduleTaskEntity task = scheduleTaskService.getTaskById(id);
            return Result.success(task);
        } catch (Exception e) {
            logger.error("查询排程任务失败", e);
            return Result.error("查询排程任务失败: " + e.getMessage());
        }
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "获取所有排程任务", description = "获取所有排程任务列表")
    public Result<List<ScheduleTaskEntity>> getAllTasks() {
        try {
            logger.info("获取所有排程任务");
            List<ScheduleTaskEntity> tasks = scheduleTaskService.getAllTasks();
            return Result.success(tasks);
        } catch (Exception e) {
            logger.error("获取所有排程任务失败", e);
            return Result.error("获取所有排程任务失败: " + e.getMessage());
        }
    }

    @GetMapping(value = "/page", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "分页获取排程任务", description = "分页查询排程任务列表")
    public Result<PageResult<ScheduleTaskEntity>> getTasksByPage(
            @Parameter(description = "页码，默认1", required = false) @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认10", required = false) @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("分页获取排程任务: page={}, size={}", page, size);
            Pageable pageable = PageRequest.of(page - 1, size);
            var pageResult = scheduleTaskService.getTasksByPage(pageable);
            PageResult<ScheduleTaskEntity> result = PageResult.build(
                    pageResult.getTotalElements(),
                    size,
                    page,
                    pageResult.getContent()
            );
            return Result.success(result);
        } catch (Exception e) {
            logger.error("分页获取排程任务失败", e);
            return Result.error("分页获取排程任务失败: " + e.getMessage());
        }
    }

    @GetMapping(value = "/schedule/{scheduleId}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "根据排程ID获取任务", description = "根据排程ID分页获取排程任务列表")
    public Result<PageResult<ScheduleTaskEntity>> getTasksByScheduleId(
            @Parameter(description = "排程ID", required = true) @PathVariable Long scheduleId,
            @Parameter(description = "页码，默认1", required = false) @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认10", required = false) @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("根据排程ID获取任务: scheduleId={}, page={}, size={}", scheduleId, page, size);
            Pageable pageable = PageRequest.of(page - 1, size);
            var pageResult = scheduleTaskService.getTasksByScheduleId(scheduleId, pageable);
            PageResult<ScheduleTaskEntity> result = PageResult.build(
                    pageResult.getTotalElements(),
                    size,
                    page,
                    pageResult.getContent()
            );
            return Result.success(result);
        } catch (Exception e) {
            logger.error("根据排程ID获取任务失败", e);
            return Result.error("根据排程ID获取任务失败: " + e.getMessage());
        }
    }

    @GetMapping(value = "/resource/{resourceId}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "根据资源ID获取任务", description = "根据资源ID分页获取排程任务列表")
    public Result<PageResult<ScheduleTaskEntity>> getTasksByResourceId(
            @Parameter(description = "资源ID", required = true) @PathVariable Long resourceId,
            @Parameter(description = "页码，默认1", required = false) @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量，默认10", required = false) @RequestParam(defaultValue = "10") int size) {
        try {
            logger.info("根据资源ID获取任务: resourceId={}, page={}, size={}", resourceId, page, size);
            Pageable pageable = PageRequest.of(page - 1, size);
            var pageResult = scheduleTaskService.getTasksByResourceId(resourceId, pageable);
            PageResult<ScheduleTaskEntity> result = PageResult.build(
                    pageResult.getTotalElements(),
                    size,
                    page,
                    pageResult.getContent()
            );
            return Result.success(result);
        } catch (Exception e) {
            logger.error("根据资源ID获取任务失败", e);
            return Result.error("根据资源ID获取任务失败: " + e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "更新排程任务", description = "根据ID更新排程任务")
    public Result<ScheduleTaskEntity> updateTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long id,
            @RequestBody ScheduleTaskEntity task) {
        try {
            logger.info("更新排程任务: id={}", id);
            ScheduleTaskEntity updatedTask = scheduleTaskService.updateTask(id, task);
            return Result.success(updatedTask);
        } catch (Exception e) {
            logger.error("更新排程任务失败", e);
            return Result.error("更新排程任务失败: " + e.getMessage());
        }
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=utf-8")
    @Operation(summary = "删除排程任务", description = "根据ID删除排程任务")
    public Result<Void> deleteTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long id) {
        try {
            logger.info("删除排程任务: id={}", id);
            scheduleTaskService.deleteTask(id);
            return Result.success();
        } catch (Exception e) {
            logger.error("删除排程任务失败", e);
            return Result.error("删除排程任务失败: " + e.getMessage());
        }
    }
}
