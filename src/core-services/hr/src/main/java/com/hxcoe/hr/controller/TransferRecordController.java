package com.hxcoe.hr.controller;

import com.hxcoe.hr.entity.TransferRecordEntity;
import com.hxcoe.hr.service.TransferRecordService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 潬岗记录Controller
 */
@RestController
@RequestMapping("/api/v1/hr/transfer-records")
@Tag(name = "转岗记录管理", description = "转岗记录管理相关接口")
public class TransferRecordController {

    private static final Logger logger = LoggerFactory.getLogger(TransferRecordController.class);

    @Autowired
    private TransferRecordService transferRecordService;

    /**
     * 创建转岗记录
     * @param transferRecord 转岗记录实体
     * @return 转岗记录实体
     */
    @PostMapping
    @Operation(summary = "创建转岗记录", description = "创建新的转岗记录")
    public Result<TransferRecordEntity> createTransferRecord(@RequestBody TransferRecordEntity transferRecord) {
        logger.info("创建转岗记录");
        TransferRecordEntity createdRecord = transferRecordService.createTransferRecord(transferRecord);
        return Result.success(createdRecord);
    }

    /**
     * 根据ID查询转岗记录
     * @param id 转岗记录ID
     * @return 转岗记录实体
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询转岗记录", description = "根据ID查询转岗记录")
    public Result<TransferRecordEntity> getTransferRecordById(@PathVariable Long id) {
        logger.info("查询转岗记录: id={}", id);
        TransferRecordEntity record = transferRecordService.getTransferRecordById(id);
        return Result.success(record);
    }

    /**
     * 更新转岗记录
     * @param id 转岗记录ID
     * @param transferRecord 转岗记录实体
     * @return 转岗记录实体
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新转岗记录", description = "根据ID更新转岗记录")
    public Result<TransferRecordEntity> updateTransferRecord(@PathVariable Long id, @RequestBody TransferRecordEntity transferRecord) {
        logger.info("更新转岗记录: id={}", id);
        TransferRecordEntity updatedRecord = transferRecordService.updateTransferRecord(id, transferRecord);
        return Result.success(updatedRecord);
    }

    /**
     * 删除转岗记录
     * @param id 转岗记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除转岗记录", description = "根据ID删除转岗记录")
    public Result<Void> deleteTransferRecord(@PathVariable Long id) {
        logger.info("删除转岗记录: id={}", id);
        transferRecordService.deleteTransferRecord(id);
        return Result.success();
    }

    /**
     * 查询所有转岗记录
     * @return 转岗记录列表
     */
    @GetMapping
    @Operation(summary = "查询所有转岗记录", description = "获取所有转岗记录列表")
    public Result<List<TransferRecordEntity>> getAllTransferRecords() {
        logger.info("查询所有转岗记录");
        List<TransferRecordEntity> transferRecords = transferRecordService.getAllTransferRecords();
        return Result.success(transferRecords);
    }

    /**
     * 分页查询转岗记录
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 转岗记录分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询转岗记录", description = "分页查询转岗记录")
    public Result<org.springframework.data.domain.Page<TransferRecordEntity>> getTransferRecordsByPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        logger.info("分页查询转岗记录: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        org.springframework.data.domain.Page<TransferRecordEntity> transferRecords = transferRecordService.getTransferRecordsByPage(pageable);
        return Result.success(transferRecords);
    }

    /**
     * 根据员工ID查询转岗记录
     * @param employeeId 员工ID
     * @return 转岗记录列表
     */
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "根据员工查询转岗记录", description = "根据员工ID查询转岗记录")
    public Result<List<TransferRecordEntity>> getTransferRecordsByEmployeeId(@PathVariable Long employeeId) {
        logger.info("根据员工查询转岗记录: employeeId={}", employeeId);
        List<TransferRecordEntity> transferRecords = transferRecordService.getTransferRecordsByEmployeeId(employeeId);
        return Result.success(transferRecords);
    }

    /**
     * 根据状态查询转岗记录
     * @param status 状态
     * @return 转岗记录列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询转岗记录", description = "根据状态查询转岗记录")
    public Result<List<TransferRecordEntity>> getTransferRecordsByStatus(@PathVariable Integer status) {
        logger.info("根据状态查询转岗记录: status={}", status);
        List<TransferRecordEntity> transferRecords = transferRecordService.getTransferRecordsByStatus(status);
        return Result.success(transferRecords);
    }
}
