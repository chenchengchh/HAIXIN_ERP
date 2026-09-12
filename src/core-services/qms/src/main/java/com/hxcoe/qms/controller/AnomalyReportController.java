package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.AnomalyReportEntity;
import com.hxcoe.qms.service.AnomalyReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 质量异常报告管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/anomaly-reports")
public class AnomalyReportController {

    @Autowired
    private AnomalyReportService anomalyReportService;

    /**
     * 分页查询异常报告列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param reportNo 报告编号（模糊）
     * @param status 状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<AnomalyReportEntity>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String reportNo,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        return anomalyReportService.page(reportNo, status, pageable);
    }

    /**
     * 获取异常报告详情
     *
     * @param id 报告ID
     * @return 报告详情
     */
    @GetMapping("/{id}")
    public Result<AnomalyReportEntity> getById(@PathVariable Long id) {
        return anomalyReportService.getById(id);
    }

    /**
     * 创建异常报告
     *
     * @param entity 报告数据
     * @return 创建结果
     */
    @PostMapping
    public Result<AnomalyReportEntity> create(@RequestBody AnomalyReportEntity entity) {
        return anomalyReportService.create(entity);
    }

    /**
     * 更新异常报告
     *
     * @param id 报告ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<AnomalyReportEntity> update(@PathVariable Long id, @RequestBody AnomalyReportEntity entity) {
        return anomalyReportService.update(id, entity);
    }

    /**
     * 删除异常报告
     *
     * @param id 报告ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return anomalyReportService.delete(id);
    }

    /**
     * 开始调查
     *
     * @param id 报告ID
     * @return 处理结果
     */
    @PutMapping("/{id}/start-investigation")
    public Result<Void> startInvestigation(@PathVariable Long id) {
        return anomalyReportService.startInvestigation(id);
    }

    /**
     * 解决异常
     *
     * @param id 报告ID
     * @return 处理结果
     */
    @PutMapping("/{id}/resolve")
    public Result<Void> resolve(@PathVariable Long id) {
        return anomalyReportService.resolve(id);
    }

    /**
     * 关闭异常
     *
     * @param id 报告ID
     * @return 处理结果
     */
    @PutMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        return anomalyReportService.close(id);
    }
}
