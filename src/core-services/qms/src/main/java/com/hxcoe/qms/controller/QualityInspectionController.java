package com.hxcoe.qms.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.qms.entity.QualityInspectionEntity;
import com.hxcoe.qms.service.QualityInspectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/qms/inspections")
public class QualityInspectionController {

    @Autowired
    private QualityInspectionService inspectionService;

    /**
     * 创建质量检验单
     *
     * @param inspection 检验单数据
     * @return 创建结果
     */
    @PostMapping
    public Result<QualityInspectionEntity> createInspection(@RequestBody QualityInspectionEntity inspection) {
        return inspectionService.createInspection(inspection);
    }

    /**
     * 更新质量检验单
     *
     * @param id 检验单ID
     * @param inspection 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<QualityInspectionEntity> updateInspection(@PathVariable Long id, @RequestBody QualityInspectionEntity inspection) {
        return inspectionService.updateInspection(id, inspection);
    }

    /**
     * 删除质量检验单
     *
     * @param id 检验单ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteInspection(@PathVariable Long id) {
        return inspectionService.deleteInspection(id);
    }

    /**
     * 获取质量检验单详情
     *
     * @param id 检验单ID
     * @return 详情
     */
    @GetMapping("/{id}")
    public Result<QualityInspectionEntity> getInspectionById(@PathVariable Long id) {
        return inspectionService.getInspectionById(id);
    }

    /**
     * 分页查询质量检验单
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页结果
     */
    @GetMapping("/page")
    public Result<PageResult<QualityInspectionEntity>> getInspectionsByPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return inspectionService.getInspectionsByPage(pageable);
    }
}
