package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.CapaEntity;
import com.hxcoe.qms.service.CapaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * CAPA管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/capas")
public class CapaController {

    @Autowired
    private CapaService capaService;

    /**
     * 分页查询CAPA列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param reportNo 报告编号（模糊）
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<CapaEntity>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String reportNo
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        return capaService.page(reportNo, pageable);
    }

    /**
     * 获取CAPA详情
     *
     * @param id 措施ID
     * @return 详情
     */
    @GetMapping("/{id}")
    public Result<CapaEntity> getById(@PathVariable Long id) {
        return capaService.getById(id);
    }

    /**
     * 创建CAPA
     *
     * @param entity 措施数据
     * @return 创建结果
     */
    @PostMapping
    public Result<CapaEntity> create(@RequestBody CapaEntity entity) {
        return capaService.create(entity);
    }

    /**
     * 更新CAPA
     *
     * @param id 措施ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<CapaEntity> update(@PathVariable Long id, @RequestBody CapaEntity entity) {
        return capaService.update(id, entity);
    }

    /**
     * 审核CAPA
     *
     * @param id 措施ID
     * @param body 审核数据
     * @return 处理结果
     */
    @PutMapping("/{id}/review")
    public Result<Void> review(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String reviewer = body == null ? null : (body.get("reviewer") == null ? null : String.valueOf(body.get("reviewer")));
        String reviewResult = body == null ? null : (body.get("reviewResult") == null ? null : String.valueOf(body.get("reviewResult")));
        return capaService.review(id, reviewer, reviewResult);
    }

    /**
     * 验证CAPA
     *
     * @param id 措施ID
     * @param body 验证数据
     * @return 处理结果
     */
    @PutMapping("/{id}/verify")
    public Result<Void> verify(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String verifyResult = body == null ? null : (body.get("verifyResult") == null ? null : String.valueOf(body.get("verifyResult")));
        String verifyStatus = body == null ? null : (body.get("verifyStatus") == null ? null : String.valueOf(body.get("verifyStatus")));
        return capaService.verify(id, verifyResult, verifyStatus);
    }
}
