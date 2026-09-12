package com.hxcoe.hr.controller;

import com.hxcoe.hr.entity.SalaryAdjustmentEntity;
import com.hxcoe.hr.service.SalaryAdjustmentService;
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
 * 调薪记录Controller
 */
@RestController
@RequestMapping("/api/v1/hr/salary-adjustments")
@Tag(name = "调薪记录管理", description = "调薪记录管理相关接口")
public class SalaryAdjustmentController {

    private static final Logger logger = LoggerFactory.getLogger(SalaryAdjustmentController.class);

    @Autowired
    private SalaryAdjustmentService salaryAdjustmentService;

    /**
     * 创建调薪记录
     * @param salaryAdjustment 调薪记录实体
     * @return 调薪记录实体
     */
    @PostMapping
    @Operation(summary = "创建调薪记录", description = "创建新的调薪记录")
    public Result<SalaryAdjustmentEntity> createSalaryAdjustment(@RequestBody SalaryAdjustmentEntity salaryAdjustment) {
        logger.info("创建调薪记录");
        SalaryAdjustmentEntity createdRecord = salaryAdjustmentService.createSalaryAdjustment(salaryAdjustment);
        return Result.success(createdRecord);
    }

    /**
     * 根据ID查询调薪记录
     * @param id 调薪记录ID
     * @return 调薪记录实体
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询调薪记录", description = "根据ID查询调薪记录")
    public Result<SalaryAdjustmentEntity> getSalaryAdjustmentById(@PathVariable Long id) {
        logger.info("查询调薪记录: id={}", id);
        SalaryAdjustmentEntity record = salaryAdjustmentService.getSalaryAdjustmentById(id);
        return Result.success(record);
    }

    /**
     * 更新调薪记录
     * @param id 调薪记录ID
     * @param salaryAdjustment 调薪记录实体
     * @return 调薪记录实体
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新调薪记录", description = "根据ID更新调薪记录")
    public Result<SalaryAdjustmentEntity> updateSalaryAdjustment(@PathVariable Long id, @RequestBody SalaryAdjustmentEntity salaryAdjustment) {
        logger.info("更新调薪记录: id={}", id);
        SalaryAdjustmentEntity updatedRecord = salaryAdjustmentService.updateSalaryAdjustment(id, salaryAdjustment);
        return Result.success(updatedRecord);
    }

    /**
     * 删除调薪记录
     * @param id 调薪记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除调薪记录", description = "根据ID删除调薪记录")
    public Result<Void> deleteSalaryAdjustment(@PathVariable Long id) {
        logger.info("删除调薪记录: id={}", id);
        salaryAdjustmentService.deleteSalaryAdjustment(id);
        return Result.success();
    }

    /**
     * 查询所有调薪记录
     * @return 调薪记录列表
     */
    @GetMapping
    @Operation(summary = "查询所有调薪记录", description = "获取所有调薪记录列表")
    public Result<List<SalaryAdjustmentEntity>> getAllSalaryAdjustments() {
        logger.info("查询所有调薪记录");
        List<SalaryAdjustmentEntity> salaryAdjustments = salaryAdjustmentService.getAllSalaryAdjustments();
        return Result.success(salaryAdjustments);
    }

    /**
     * 分页查询调薪记录
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 调薪记录分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询调薪记录", description = "分页查询调薪记录")
    public Result<org.springframework.data.domain.Page<SalaryAdjustmentEntity>> getSalaryAdjustmentsByPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        logger.info("分页查询调薪记录: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        org.springframework.data.domain.Page<SalaryAdjustmentEntity> salaryAdjustments = salaryAdjustmentService.getSalaryAdjustmentsByPage(pageable);
        return Result.success(salaryAdjustments);
    }

    /**
     * 根据员工ID查询调薪记录
     * @param employeeId 员工ID
     * @return 调薪记录列表
     */
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "根据员工查询调薪记录", description = "根据员工ID查询调薪记录")
    public Result<List<SalaryAdjustmentEntity>> getSalaryAdjustmentsByEmployeeId(@PathVariable Long employeeId) {
        logger.info("根据员工查询调薪记录: employeeId={}", employeeId);
        List<SalaryAdjustmentEntity> salaryAdjustments = salaryAdjustmentService.getSalaryAdjustmentsByEmployeeId(employeeId);
        return Result.success(salaryAdjustments);
    }

    /**
     * 根据状态查询调薪记录
     * @param status 状态
     * @return 调薪记录列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询调薪记录", description = "根据状态查询调薪记录")
    public Result<List<SalaryAdjustmentEntity>> getSalaryAdjustmentsByStatus(@PathVariable Integer status) {
        logger.info("根据状态查询调薪记录: status={}", status);
        List<SalaryAdjustmentEntity> salaryAdjustments = salaryAdjustmentService.getSalaryAdjustmentsByStatus(status);
        return Result.success(salaryAdjustments);
    }
}
