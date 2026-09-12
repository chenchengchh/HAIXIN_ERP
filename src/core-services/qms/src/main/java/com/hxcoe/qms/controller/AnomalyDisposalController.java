package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.AnomalyDisposalEntity;
import com.hxcoe.qms.service.AnomalyDisposalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 质量异常处置管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/anomaly-disposals")
public class AnomalyDisposalController {

    @Autowired
    private AnomalyDisposalService anomalyDisposalService;

    /**
     * 分页查询异常处置列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param reportNo 报告编号（模糊）
     * @param disposalStatus 处置状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<AnomalyDisposalEntity>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String reportNo,
            @RequestParam(required = false) String disposalStatus
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        return anomalyDisposalService.page(reportNo, disposalStatus, pageable);
    }

    /**
     * 获取异常处置详情
     *
     * @param id 处置ID
     * @return 处置详情
     */
    @GetMapping("/{id}")
    public Result<AnomalyDisposalEntity> getById(@PathVariable Long id) {
        return anomalyDisposalService.getById(id);
    }

    /**
     * 创建异常处置
     *
     * @param entity 处置数据
     * @return 创建结果
     */
    @PostMapping
    public Result<AnomalyDisposalEntity> create(@RequestBody AnomalyDisposalEntity entity) {
        return anomalyDisposalService.create(entity);
    }

    /**
     * 更新异常处置
     *
     * @param id 处置ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<AnomalyDisposalEntity> update(@PathVariable Long id, @RequestBody AnomalyDisposalEntity entity) {
        return anomalyDisposalService.update(id, entity);
    }

    /**
     * 验证异常处置
     *
     * @param id 处置ID
     * @param body 验证数据
     * @return 处理结果
     */
    @PutMapping("/{id}/verify")
    public Result<Void> verify(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String verifier = body == null ? null : (body.get("verifier") == null ? null : String.valueOf(body.get("verifier")));
        String verificationResult = body == null ? null : (body.get("verificationResult") == null ? null : String.valueOf(body.get("verificationResult")));
        return anomalyDisposalService.verify(id, verifier, verificationResult);
    }
}
