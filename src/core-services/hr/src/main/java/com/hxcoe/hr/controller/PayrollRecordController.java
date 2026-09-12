package com.hxcoe.hr.controller;

import com.hxcoe.hr.entity.PayrollRecordEntity;
import com.hxcoe.hr.service.PayrollRecordService;
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
 * 薪资记录Controller
 */
@RestController
@RequestMapping("/api/v1/hr/payroll-records")
@Tag(name = "薪资记录管理", description = "薪资记录管理相关接口")
public class PayrollRecordController {

    private static final Logger logger = LoggerFactory.getLogger(PayrollRecordController.class);

    @Autowired
    private PayrollRecordService payrollRecordService;

    /**
     * 创建薪资记录
     * @param payrollRecord 薪资记录实体
     * @return 薪资记录实体
     */
    @PostMapping
    @Operation(summary = "创建薪资记录", description = "创建新的薪资记录")
    public Result<PayrollRecordEntity> createPayrollRecord(@RequestBody PayrollRecordEntity payrollRecord) {
        logger.info("创建薪资记录");
        PayrollRecordEntity createdRecord = payrollRecordService.createPayrollRecord(payrollRecord);
        return Result.success(createdRecord);
    }

    /**
     * 根据ID查询薪资记录
     * @param id 薪资记录ID
     * @return 薪资记录实体
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询薪资记录", description = "根据ID查询薪资记录")
    public Result<PayrollRecordEntity> getPayrollRecordById(@PathVariable Long id) {
        logger.info("查询薪资记录: id={}", id);
        PayrollRecordEntity record = payrollRecordService.getPayrollRecordById(id);
        return Result.success(record);
    }

    /**
     * 更新薪资记录
     * @param id 薪资记录ID
     * @param payrollRecord 薪资记录实体
     * @return 薪资记录实体
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新薪资记录", description = "根据ID更新薪资记录")
    public Result<PayrollRecordEntity> updatePayrollRecord(@PathVariable Long id, @RequestBody PayrollRecordEntity payrollRecord) {
        logger.info("更新薪资记录: id={}", id);
        PayrollRecordEntity updatedRecord = payrollRecordService.updatePayrollRecord(id, payrollRecord);
        return Result.success(updatedRecord);
    }

    /**
     * 删除薪资记录
     * @param id 薪资记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除薪资记录", description = "根据ID删除薪资记录")
    public Result<Void> deletePayrollRecord(@PathVariable Long id) {
        logger.info("删除薪资记录: id={}", id);
        payrollRecordService.deletePayrollRecord(id);
        return Result.success();
    }

    /**
     * 分页查询薪资记录
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 薪资记录分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询薪资记录", description = "分页查询薪资记录")
    public Result<org.springframework.data.domain.Page<PayrollRecordEntity>> getPayrollRecordsByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        logger.info("分页查询薪资记录: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        org.springframework.data.domain.Page<PayrollRecordEntity> payrollRecords = payrollRecordService.getPayrollRecordsByPage(pageable);
        return Result.success(payrollRecords);
    }

    /**
     * 根据月份范围查询薪资记录
     * @param startMonth 开始月份（可选）
     * @param endMonth 结束月份（可选）
     * @return 薪资记录列表
     */
    @GetMapping
    @Operation(summary = "根据月份查询薪资记录", description = "根据月份范围查询薪资记录，参数为空时返回所有记录")
    public Result<List<PayrollRecordEntity>> getPayrollRecords(
            @RequestParam(name = "startMonth", required = false) String startMonth,
            @RequestParam(name = "endMonth", required = false) String endMonth) {
        logger.info("根据月份查询薪资记录: startMonth={}, endMonth={}", startMonth, endMonth);
        List<PayrollRecordEntity> payrollRecords;
        if (startMonth == null || startMonth.isBlank() || endMonth == null || endMonth.isBlank()) {
            // 参数为空时返回所有记录
            payrollRecords = payrollRecordService.getAllPayrollRecords();
        } else {
            payrollRecords = payrollRecordService.getPayrollRecordsByMonthRange(startMonth, endMonth);
        }
        return Result.success(payrollRecords);
    }

    /**
     * 根据员工ID查询薪资记录
     * @param employeeId 员工ID
     * @return 薪资记录列表
     */
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "根据员工查询薪资记录", description = "根据员工ID查询薪资记录")
    public Result<List<PayrollRecordEntity>> getPayrollRecordsByEmployeeId(@PathVariable Long employeeId) {
        logger.info("根据员工查询薪资记录: employeeId={}", employeeId);
        List<PayrollRecordEntity> payrollRecords = payrollRecordService.getPayrollRecordsByEmployeeId(employeeId);
        return Result.success(payrollRecords);
    }
}
