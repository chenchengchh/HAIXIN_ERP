package com.hxcoe.hr.controller;

import com.hxcoe.hr.entity.ResignationRequestEntity;
import com.hxcoe.hr.service.ResignationRequestService;
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
 * 离职申请Controller
 */
@RestController
@RequestMapping("/api/v1/hr/resignation-requests")
@Tag(name = "离职申请管理", description = "离职申请管理相关接口")
public class ResignationRequestController {

    private static final Logger logger = LoggerFactory.getLogger(ResignationRequestController.class);

    @Autowired
    private ResignationRequestService resignationRequestService;

    /**
     * 创建离职申请
     * @param resignationRequest 离职申请实体
     * @return 离职申请实体
     */
    @PostMapping
    @Operation(summary = "创建离职申请", description = "创建新的离职申请")
    public Result<ResignationRequestEntity> createResignationRequest(@RequestBody ResignationRequestEntity resignationRequest) {
        logger.info("创建离职申请");
        ResignationRequestEntity createdRequest = resignationRequestService.createResignationRequest(resignationRequest);
        return Result.success(createdRequest);
    }

    /**
     * 根据ID查询离职申请
     * @param id 离职申请ID
     * @return 离职申请实体
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询离职申请", description = "根据ID查询离职申请")
    public Result<ResignationRequestEntity> getResignationRequestById(@PathVariable Long id) {
        logger.info("查询离职申请: id={}", id);
        ResignationRequestEntity request = resignationRequestService.getResignationRequestById(id);
        return Result.success(request);
    }

    /**
     * 更新离职申请
     * @param id 离职申请ID
     * @param resignationRequest 离职申请实体
     * @return 离职申请实体
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新离职申请", description = "根据ID更新离职申请")
    public Result<ResignationRequestEntity> updateResignationRequest(@PathVariable Long id, @RequestBody ResignationRequestEntity resignationRequest) {
        logger.info("更新离职申请: id={}", id);
        ResignationRequestEntity updatedRequest = resignationRequestService.updateResignationRequest(id, resignationRequest);
        return Result.success(updatedRequest);
    }

    /**
     * 删除离职申请
     * @param id 离职申请ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除离职申请", description = "根据ID删除离职申请")
    public Result<Void> deleteResignationRequest(@PathVariable Long id) {
        logger.info("删除离职申请: id={}", id);
        resignationRequestService.deleteResignationRequest(id);
        return Result.success();
    }

    /**
     * 查询所有离职申请
     * @return 离职申请列表
     */
    @GetMapping
    @Operation(summary = "查询所有离职申请", description = "获取所有离职申请列表")
    public Result<List<ResignationRequestEntity>> getAllResignationRequests() {
        logger.info("查询所有离职申请");
        List<ResignationRequestEntity> resignationRequests = resignationRequestService.getAllResignationRequests();
        return Result.success(resignationRequests);
    }

    /**
     * 分页查询离职申请
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 离职申请分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询离职申请", description = "分页查询离职申请")
    public Result<org.springframework.data.domain.Page<ResignationRequestEntity>> getResignationRequestsByPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        logger.info("分页查询离职申请: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        org.springframework.data.domain.Page<ResignationRequestEntity> resignationRequests = resignationRequestService.getResignationRequestsByPage(pageable);
        return Result.success(resignationRequests);
    }

    /**
     * 根据员工ID查询离职申请
     * @param employeeId 员工ID
     * @return 离职申请列表
     */
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "根据员工查询离职申请", description = "根据员工ID查询离职申请")
    public Result<List<ResignationRequestEntity>> getResignationRequestsByEmployeeId(@PathVariable Long employeeId) {
        logger.info("根据员工查询离职申请: employeeId={}", employeeId);
        List<ResignationRequestEntity> resignationRequests = resignationRequestService.getResignationRequestsByEmployeeId(employeeId);
        return Result.success(resignationRequests);
    }

    /**
     * 根据状态查询离职申请
     * @param status 状态
     * @return 离职申请列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态查询离职申请", description = "根据状态查询离职申请")
    public Result<List<ResignationRequestEntity>> getResignationRequestsByStatus(@PathVariable Integer status) {
        logger.info("根据状态查询离职申请: status={}", status);
        List<ResignationRequestEntity> resignationRequests = resignationRequestService.getResignationRequestsByStatus(status);
        return Result.success(resignationRequests);
    }
}
