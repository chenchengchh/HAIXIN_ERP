package com.hxcoe.hr.controller;

import com.hxcoe.hr.entity.AttendanceRuleEntity;
import com.hxcoe.hr.service.AttendanceRuleService;
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
 * 考勤规则Controller
 */
@RestController
@RequestMapping("/api/v1/hr/attendance-rules")
@Tag(name = "考勤规则管理", description = "考勤规则相关接口")
public class AttendanceRuleController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceRuleController.class);

    @Autowired
    private AttendanceRuleService attendanceRuleService;

    /**
     * 创建考勤规则
     * @param rule 考勤规则实体
     * @return 考勤规则实体
     */
    @PostMapping
    @Operation(summary = "创建考勤规则", description = "创建新的考勤规则")
    public Result<AttendanceRuleEntity> createRule(@RequestBody AttendanceRuleEntity rule) {
        logger.info("创建考勤规则");
        AttendanceRuleEntity createdRule = attendanceRuleService.createRule(rule);
        return Result.success(createdRule);
    }

    /**
     * 根据ID查询考勤规则
     * @param id 考勤规则ID
     * @return 考勤规则实体
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询考勤规则", description = "根据ID查询考勤规则")
    public Result<AttendanceRuleEntity> getRuleById(@PathVariable Long id) {
        logger.info("查询考勤规则: id={}", id);
        AttendanceRuleEntity rule = attendanceRuleService.getRuleById(id);
        return Result.success(rule);
    }

    /**
     * 更新考勤规则
     * @param id 考勤规则ID
     * @param rule 考勤规则实体
     * @return 考勤规则实体
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新考勤规则", description = "根据ID更新考勤规则")
    public Result<AttendanceRuleEntity> updateRule(@PathVariable Long id, @RequestBody AttendanceRuleEntity rule) {
        logger.info("更新考勤规则: id={}", id);
        AttendanceRuleEntity updatedRule = attendanceRuleService.updateRule(id, rule);
        return Result.success(updatedRule);
    }

    /**
     * 删除考勤规则
     * @param id 考勤规则ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除考勤规则", description = "根据ID删除考勤规则")
    public Result<Void> deleteRule(@PathVariable Long id) {
        logger.info("删除考勤规则: id={}", id);
        attendanceRuleService.deleteRule(id);
        return Result.success();
    }

    /**
     * 查询全部考勤规则
     * @return 考勤规则列表
     */
    @GetMapping
    @Operation(summary = "查询全部考勤规则", description = "查询全部考勤规则")
    public Result<List<AttendanceRuleEntity>> getAllRules() {
        logger.info("查询全部考勤规则");
        List<AttendanceRuleEntity> rules = attendanceRuleService.getAllRules();
        return Result.success(rules);
    }

    /**
     * 分页查询考勤规则
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 考勤规则分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询考勤规则", description = "分页查询考勤规则")
    public Result<Page<AttendanceRuleEntity>> getRulesByPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        logger.info("分页查询考勤规则: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<AttendanceRuleEntity> rules = attendanceRuleService.getRulesByPage(pageable);
        return Result.success(rules);
    }
}
