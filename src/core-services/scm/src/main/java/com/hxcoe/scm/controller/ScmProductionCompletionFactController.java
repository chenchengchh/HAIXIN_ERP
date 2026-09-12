package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.scm.entity.ScmProductionCompletionFactEntity;
import com.hxcoe.scm.service.MesCompletionFactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * SCM 生产完工事实查询控制器（B6 闭环前端查询入口）。
 *
 * <p>独立于 {@link ScmIntegrationController}（集成入口，负责接收外部事件），
 * 本控制器仅负责对已落库的完工事实提供只读分页查询，供前端 F2（SCM 完工事实页）调用。
 *
 * <p>路径遵循 RESTful 规范：{@code GET /api/v1/scm/completion-facts}，
 * 支持按 erpProductionNo、workOrderNo 模糊筛选，按 scmOrderStatus 精确筛选，
 * 以及按完工时间范围筛选，结果按完工时间倒序返回。
 *
 * <p>分页约定：前端 page 从 1 开始，size 默认 10；后端转换为 Spring Data 的 0 基页码。
 */
@RestController
@RequestMapping({"/api/v1/scm/completion-facts", "/scm/completion-facts"})
public class ScmProductionCompletionFactController {

    @Autowired
    private MesCompletionFactService mesCompletionFactService;

    /**
     * 分页查询生产完工事实。
     *
     * <p>所有筛选条件均为可选；未传任何条件时返回全量分页（按完工时间倒序）。
     *
     * @param erpProductionNo   ERP 生产单号（模糊匹配，可空）
     * @param workOrderNo       MES 工单号（模糊匹配，可空）
     * @param scmOrderStatus    SCM 侧订单状态（精确匹配，可空，如 COMPLETED）
     * @param completedTimeFrom 完工时间下限（含，ISO-8601，可空）
     * @param completedTimeTo   完工时间上限（含，ISO-8601，可空）
     * @param page              页码（从 1 开始，默认 1）
     * @param size              每页条数（默认 10）
     * @return 完工事实分页结果
     */
    @GetMapping
    public ApiResponse<PageResult<ScmProductionCompletionFactEntity>> listCompletionFacts(
            @RequestParam(required = false) String erpProductionNo,
            @RequestParam(required = false) String workOrderNo,
            @RequestParam(required = false) String scmOrderStatus,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime completedTimeFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime completedTimeTo,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        // 前端 page 从 1 开始，Spring Data Pageable 从 0 开始，需 -1 转换
        int safePage = page == null || page < 1 ? 0 : page - 1;
        int safeSize = size == null || size < 1 ? 10 : size;
        // 完工时间倒序，便于查看最新数据
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "completedTime"));

        Page<ScmProductionCompletionFactEntity> pageResult = mesCompletionFactService.findCompletionFacts(
                erpProductionNo, workOrderNo, scmOrderStatus, completedTimeFrom, completedTimeTo, pageable);

        // Pageable 的 page 从 0 开始，前端从 1 开始，需 +1 转换
        PageResult<ScmProductionCompletionFactEntity> result = PageResult.build(
                pageResult.getTotalElements(),
                pageResult.getSize(),
                pageResult.getNumber() + 1,
                pageResult.getContent());

        return ApiResponse.success("成功", result);
    }
}
