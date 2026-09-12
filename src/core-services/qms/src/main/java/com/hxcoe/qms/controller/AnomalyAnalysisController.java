package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.AnomalyAnalysisEntity;
import com.hxcoe.qms.service.AnomalyAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 质量异常分析管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/anomaly-analyses")
public class AnomalyAnalysisController {

    @Autowired
    private AnomalyAnalysisService anomalyAnalysisService;

    /**
     * 分页查询异常分析列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param reportNo 报告编号（模糊）
     * @param analysisStatus 分析状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<AnomalyAnalysisEntity>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String reportNo,
            @RequestParam(required = false) String analysisStatus
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        return anomalyAnalysisService.page(reportNo, analysisStatus, pageable);
    }

    /**
     * 获取异常分析详情
     *
     * @param id 分析ID
     * @return 分析详情
     */
    @GetMapping("/{id}")
    public Result<AnomalyAnalysisEntity> getById(@PathVariable Long id) {
        return anomalyAnalysisService.getById(id);
    }

    /**
     * 创建异常分析
     *
     * @param entity 分析数据
     * @return 创建结果
     */
    @PostMapping
    public Result<AnomalyAnalysisEntity> create(@RequestBody AnomalyAnalysisEntity entity) {
        return anomalyAnalysisService.create(entity);
    }

    /**
     * 更新异常分析
     *
     * @param id 分析ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<AnomalyAnalysisEntity> update(@PathVariable Long id, @RequestBody AnomalyAnalysisEntity entity) {
        return anomalyAnalysisService.update(id, entity);
    }

    /**
     * 完成异常分析
     *
     * @param id 分析ID
     * @return 处理结果
     */
    @PutMapping("/{id}/complete")
    public Result<Void> complete(@PathVariable Long id) {
        return anomalyAnalysisService.complete(id);
    }
}
