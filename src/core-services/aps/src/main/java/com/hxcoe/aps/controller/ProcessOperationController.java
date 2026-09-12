package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.ProcessOperationEntity;
import com.hxcoe.aps.repository.ProcessOperationRepository;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aps/process-operations")
@Tag(name = "工艺操作管理", description = "工艺操作管理相关接口")
public class ProcessOperationController {

    private static final Logger logger = LoggerFactory.getLogger(ProcessOperationController.class);

    @Autowired
    private ProcessOperationRepository processOperationRepository;

    @GetMapping
    @Operation(summary = "获取所有工艺操作", description = "获取所有工艺操作列表")
    public Result<List<ProcessOperationEntity>> getProcessOperations() {
        logger.info("获取所有工艺操作");
        List<ProcessOperationEntity> operations = processOperationRepository.findAll();
        return Result.success(operations);
    }

    @GetMapping("/route/{routeId}")
    @Operation(summary = "根据路线ID查询操作", description = "根据工艺路线ID获取工艺操作")
    public Result<List<ProcessOperationEntity>> getProcessOperationsByRouteId(@PathVariable Long routeId) {
        logger.info("根据路线ID查询操作: routeId={}", routeId);
        List<ProcessOperationEntity> operations = processOperationRepository.findByRouteId(routeId);
        return Result.success(operations);
    }

    @PostMapping
    @Operation(summary = "创建工艺操作", description = "创建新的工艺操作")
    public Result<ProcessOperationEntity> createProcessOperation(@RequestBody ProcessOperationEntity operation) {
        logger.info("创建工艺操作");
        ProcessOperationEntity createdOperation = processOperationRepository.save(operation);
        return Result.success(createdOperation);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新工艺操作", description = "根据ID更新工艺操作")
    public Result<ProcessOperationEntity> updateProcessOperation(
            @PathVariable Long id,
            @RequestBody ProcessOperationEntity operation) {
        logger.info("更新工艺操作: id={}", id);
        operation.setId(id);
        ProcessOperationEntity updatedOperation = processOperationRepository.save(operation);
        return Result.success(updatedOperation);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除工艺操作", description = "根据ID删除工艺操作")
    public Result<Void> deleteProcessOperation(@PathVariable Long id) {
        logger.info("删除工艺操作: id={}", id);
        processOperationRepository.deleteById(id);
        return Result.success();
    }
}
