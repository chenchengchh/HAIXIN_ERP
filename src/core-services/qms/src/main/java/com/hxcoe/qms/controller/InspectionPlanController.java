package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.InspectionPlanEntity;
import com.hxcoe.qms.service.InspectionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 检验计划管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/inspection-plans")
public class InspectionPlanController {

    @Autowired
    private InspectionPlanService inspectionPlanService;

    /**
     * 分页查询检验计划列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param planNo 计划编号（模糊）
     * @param planName 计划名称（模糊）
     * @param materialName 物料名称（模糊）
     * @param planType 计划类型
     * @param status 状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<InspectionPlanEntity>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String planNo,
            @RequestParam(required = false) String planName,
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        return inspectionPlanService.page(planNo, planName, materialName, planType, status, pageable);
    }

    /**
     * 获取检验计划详情
     *
     * @param id 计划ID
     * @return 计划详情
     */
    @GetMapping("/{id}")
    public Result<InspectionPlanEntity> getById(@PathVariable Long id) {
        return inspectionPlanService.getById(id);
    }

    /**
     * 创建检验计划
     *
     * @param entity 计划数据
     * @return 创建结果
     */
    @PostMapping
    public Result<InspectionPlanEntity> create(@RequestBody InspectionPlanEntity entity) {
        return inspectionPlanService.create(entity);
    }

    /**
     * 更新检验计划
     *
     * @param id 计划ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<InspectionPlanEntity> update(@PathVariable Long id, @RequestBody InspectionPlanEntity entity) {
        return inspectionPlanService.update(id, entity);
    }

    /**
     * 删除检验计划
     *
     * @param id 计划ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return inspectionPlanService.delete(id);
    }

    /**
     * 激活检验计划
     *
     * @param id 计划ID
     * @return 处理结果
     */
    @PutMapping("/{id}/activate")
    public Result<Void> activate(@PathVariable Long id) {
        return inspectionPlanService.activate(id);
    }

    /**
     * 失效检验计划
     *
     * @param id 计划ID
     * @return 处理结果
     */
    @PutMapping("/{id}/invalidate")
    public Result<Void> invalidate(@PathVariable Long id) {
        return inspectionPlanService.invalidate(id);
    }
}
