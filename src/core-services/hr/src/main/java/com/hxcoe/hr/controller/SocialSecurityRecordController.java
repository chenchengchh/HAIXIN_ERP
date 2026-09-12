package com.hxcoe.hr.controller;

import com.hxcoe.hr.entity.SocialSecurityRecordEntity;
import com.hxcoe.hr.service.SocialSecurityRecordService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社保公积金记录Controller
 */
@RestController
@RequestMapping("/api/v1/hr/social-security-records")
@Tag(name = "社保公积金管理", description = "社保公积金记录相关接口")
public class SocialSecurityRecordController {

    private static final Logger logger = LoggerFactory.getLogger(SocialSecurityRecordController.class);

    @Autowired
    private SocialSecurityRecordService socialSecurityRecordService;

    /**
     * 创建社保公积金记录
     * @param record 社保公积金记录实体
     * @return 社保公积金记录实体
     */
    @PostMapping
    @Operation(summary = "创建社保公积金记录", description = "创建新的社保公积金记录")
    public Result<SocialSecurityRecordEntity> createRecord(@RequestBody SocialSecurityRecordEntity record) {
        logger.info("创建社保公积金记录");
        SocialSecurityRecordEntity createdRecord = socialSecurityRecordService.createRecord(record);
        return Result.success(createdRecord);
    }

    /**
     * 根据ID查询社保公积金记录
     * @param id 社保公积金记录ID
     * @return 社保公积金记录实体
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询社保公积金记录", description = "根据ID查询社保公积金记录")
    public Result<SocialSecurityRecordEntity> getRecordById(@PathVariable Long id) {
        logger.info("查询社保公积金记录: id={}", id);
        SocialSecurityRecordEntity record = socialSecurityRecordService.getRecordById(id);
        return Result.success(record);
    }

    /**
     * 更新社保公积金记录
     * @param id 社保公积金记录ID
     * @param record 社保公积金记录实体
     * @return 社保公积金记录实体
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新社保公积金记录", description = "根据ID更新社保公积金记录")
    public Result<SocialSecurityRecordEntity> updateRecord(@PathVariable Long id, @RequestBody SocialSecurityRecordEntity record) {
        logger.info("更新社保公积金记录: id={}", id);
        SocialSecurityRecordEntity updatedRecord = socialSecurityRecordService.updateRecord(id, record);
        return Result.success(updatedRecord);
    }

    /**
     * 删除社保公积金记录
     * @param id 社保公积金记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除社保公积金记录", description = "根据ID删除社保公积金记录")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        logger.info("删除社保公积金记录: id={}", id);
        socialSecurityRecordService.deleteRecord(id);
        return Result.success();
    }

    /**
     * 查询全部社保公积金记录
     * @return 社保公积金记录列表
     */
    @GetMapping
    @Operation(summary = "查询全部社保公积金记录", description = "查询全部社保公积金记录")
    public Result<List<SocialSecurityRecordEntity>> getAllRecords() {
        logger.info("查询全部社保公积金记录");
        List<SocialSecurityRecordEntity> records = socialSecurityRecordService.getAllRecords();
        return Result.success(records);
    }

    /**
     * 分页查询社保公积金记录
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 社保公积金记录分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询社保公积金记录", description = "分页查询社保公积金记录")
    public Result<Page<SocialSecurityRecordEntity>> getRecordsByPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        logger.info("分页查询社保公积金记录: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<SocialSecurityRecordEntity> records = socialSecurityRecordService.getRecordsByPage(pageable);
        return Result.success(records);
    }

    /**
     * 按月份计算生成社保公积金记录
     * @param month 缴费月份（YYYY-MM）
     * @return 生成记录条数
     */
    @PostMapping("/calculate")
    @Operation(summary = "计算社保公积金", description = "按月份为所有在职员工计算并生成社保公积金记录")
    public Result<Integer> calculateByMonth(@RequestParam(name = "month") String month) {
        logger.info("按月份计算社保公积金: month={}", month);
        int count = socialSecurityRecordService.calculateByMonth(month);
        return Result.success(count);
    }

    /**
     * 按月份申报社保公积金记录
     * @param month 缴费月份（YYYY-MM）
     * @return 更新记录条数
     */
    @PostMapping("/declare")
    @Operation(summary = "申报社保公积金", description = "将指定月份未申报的社保公积金记录置为已申报")
    public Result<Integer> declareByMonth(@RequestParam(name = "month") String month) {
        logger.info("按月份申报社保公积金: month={}", month);
        int count = socialSecurityRecordService.declareByMonth(month);
        return Result.success(count);
    }
}
