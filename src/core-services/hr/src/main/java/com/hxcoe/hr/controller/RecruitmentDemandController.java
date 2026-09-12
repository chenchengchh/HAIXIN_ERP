package com.hxcoe.hr.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.hr.entity.RecruitmentDemandEntity;
import com.hxcoe.hr.service.RecruitmentDemandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 招聘需求控制器
 */
@RestController
@RequestMapping("/api/v1/hr/recruitment-demands")
@Tag(name = "招聘需求管理", description = "招聘需求管理相关接口")
public class RecruitmentDemandController {

    private static final Logger logger = LoggerFactory.getLogger(RecruitmentDemandController.class);

    @Autowired
    private RecruitmentDemandService recruitmentDemandService;

    /**
     * 创建招聘需求
     *
     * @param recruitmentDemand 招聘需求实体
     * @return 创建结果
     */
    @PostMapping
    @Operation(summary = "创建招聘需求", description = "创建新的招聘需求")
    public Result<RecruitmentDemandEntity> createRecruitmentDemand(@RequestBody RecruitmentDemandEntity recruitmentDemand) {
        logger.info("创建招聘需求");
        RecruitmentDemandEntity createdRecruitmentDemand = recruitmentDemandService.createRecruitmentDemand(recruitmentDemand);
        return Result.success(createdRecruitmentDemand);
    }

    /**
     * 更新招聘需求信息
     *
     * @param id 招聘需求ID
     * @param recruitmentDemand 招聘需求实体
     * @return 更新结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新招聘需求", description = "根据ID更新招聘需求")
    public Result<RecruitmentDemandEntity> updateRecruitmentDemand(@PathVariable Long id, @RequestBody RecruitmentDemandEntity recruitmentDemand) {
        logger.info("更新招聘需求: id={}", id);
        RecruitmentDemandEntity updatedRecruitmentDemand = recruitmentDemandService.updateRecruitmentDemand(id, recruitmentDemand);
        return Result.success(updatedRecruitmentDemand);
    }

    /**
     * 根据ID删除招聘需求
     *
     * @param id 招聘需求ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除招聘需求", description = "根据ID删除招聘需求")
    public Result<Void> deleteRecruitmentDemand(@PathVariable Long id) {
        logger.info("删除招聘需求: id={}", id);
        recruitmentDemandService.deleteRecruitmentDemand(id);
        return Result.success();
    }

    /**
     * 根据ID查询招聘需求
     *
     * @param id 招聘需求ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询招聘需求", description = "根据ID查询招聘需求")
    public Result<RecruitmentDemandEntity> getRecruitmentDemandById(@PathVariable Long id) {
        logger.info("查询招聘需求: id={}", id);
        return recruitmentDemandService.getRecruitmentDemandById(id)
                .map(Result::success)
                .orElse(Result.fail("招聘需求不存在"));
    }

    /**
     * 查询所有招聘需求
     *
     * @return 查询结果
     */
    @GetMapping
    @Operation(summary = "查询所有招聘需求", description = "获取所有招聘需求列表")
    public Result<List<RecruitmentDemandEntity>> getAllRecruitmentDemands() {
        logger.info("查询所有招聘需求");
        List<RecruitmentDemandEntity> recruitmentDemands = recruitmentDemandService.getAllRecruitmentDemands();
        return Result.success(recruitmentDemands);
    }

    /**
     * 分页查询招聘需求
     *
     * @param pageable 分页参数
     * @return 分页查询结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询招聘需求", description = "分页查询招聘需求")
    public Result<PageResult<RecruitmentDemandEntity>> getRecruitmentDemandsByPage(@PageableDefault(size = 10) Pageable pageable) {
        logger.info("分页查询招聘需求: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        org.springframework.data.domain.Page<RecruitmentDemandEntity> page = recruitmentDemandService.getRecruitmentDemandsByPage(pageable);
        PageResult<RecruitmentDemandEntity> pageResult = PageResult.build(
                page.getTotalElements(),
                page.getSize(),
                page.getNumber() + 1, // Pageable的page从0开始，前端从1开始
                page.getContent()
        );
        return Result.success(pageResult);
    }

    /**
     * 根据部门ID查询招聘需求
     *
     * @param departmentId 部门ID
     * @return 查询结果
     */
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "根据部门查询招聘需求", description = "根据部门ID查询招聘需求")
    public Result<List<RecruitmentDemandEntity>> getRecruitmentDemandsByDepartment(@PathVariable Long departmentId) {
        logger.info("根据部门查询招聘需求: departmentId={}", departmentId);
        List<RecruitmentDemandEntity> recruitmentDemands = recruitmentDemandService.getRecruitmentDemandsByDepartment(departmentId);
        return Result.success(recruitmentDemands);
    }

    /**
     * 根据状态查询招聘需求
     *
     * @param status 状态
     * @return 查询结果
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询招聘需求", description = "根据状态查询招聘需求")
    public Result<List<RecruitmentDemandEntity>> getRecruitmentDemandsByStatus(@PathVariable String status) {
        logger.info("根据状态查询招聘需求: status={}", status);
        List<RecruitmentDemandEntity> recruitmentDemands = recruitmentDemandService.getRecruitmentDemandsByStatus(status);
        return Result.success(recruitmentDemands);
    }

    /**
     * 搜索招聘需求
     *
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    @GetMapping("/search")
    @Operation(summary = "搜索招聘需求", description = "根据关键词搜索招聘需求")
    public Result<List<RecruitmentDemandEntity>> searchRecruitmentDemands(@RequestParam String keyword) {
        logger.info("搜索招聘需求: keyword={}", keyword);
        List<RecruitmentDemandEntity> recruitmentDemands = recruitmentDemandService.searchRecruitmentDemands(keyword);
        return Result.success(recruitmentDemands);
    }

    /**
     * 批量删除招聘需求
     *
     * @param ids 招聘需求ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除招聘需求", description = "批量删除招聘需求")
    public Result<Void> batchDeleteRecruitmentDemands(@RequestBody List<Long> ids) {
        logger.info("批量删除招聘需求: count={}", ids.size());
        recruitmentDemandService.batchDeleteRecruitmentDemands(ids);
        return Result.success();
    }
}
