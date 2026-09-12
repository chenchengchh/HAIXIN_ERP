package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.SchedulingConstraintEntity;
import com.hxcoe.aps.repository.SchedulingConstraintRepository;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aps/scheduling-constraints")
@Tag(name = "排程约束管理", description = "排程约束管理相关接口")
public class SchedulingConstraintController {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingConstraintController.class);

    @Autowired
    private SchedulingConstraintRepository schedulingConstraintRepository;

    @GetMapping
    @Operation(summary = "获取所有排程约束", description = "获取所有排程约束列表")
    public Result<List<SchedulingConstraintEntity>> getSchedulingConstraints() {
        logger.info("获取所有排程约束");
        List<SchedulingConstraintEntity> constraints = schedulingConstraintRepository.findAll();
        return Result.success(constraints);
    }

    @GetMapping("/type/{constraintType}")
    @Operation(summary = "根据类型查询约束", description = "根据约束类型获取排程约束")
    public Result<List<SchedulingConstraintEntity>> getSchedulingConstraintsByType(@PathVariable String constraintType) {
        logger.info("根据类型查询约束: constraintType={}", constraintType);
        List<SchedulingConstraintEntity> constraints = schedulingConstraintRepository.findByConstraintType(constraintType);
        return Result.success(constraints);
    }

    @PostMapping
    @Operation(summary = "创建排程约束", description = "创建新的排程约束")
    public Result<SchedulingConstraintEntity> createSchedulingConstraint(@RequestBody SchedulingConstraintEntity constraint) {
        logger.info("创建排程约束");
        SchedulingConstraintEntity createdConstraint = schedulingConstraintRepository.save(constraint);
        return Result.success(createdConstraint);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新排程约束", description = "根据ID更新排程约束")
    public Result<SchedulingConstraintEntity> updateSchedulingConstraint(
            @PathVariable Long id,
            @RequestBody SchedulingConstraintEntity constraint) {
        logger.info("更新排程约束: id={}", id);
        constraint.setId(id);
        SchedulingConstraintEntity updatedConstraint = schedulingConstraintRepository.save(constraint);
        return Result.success(updatedConstraint);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除排程约束", description = "根据ID删除排程约束")
    public Result<Void> deleteSchedulingConstraint(@PathVariable Long id) {
        logger.info("删除排程约束: id={}", id);
        schedulingConstraintRepository.deleteById(id);
        return Result.success();
    }
}
