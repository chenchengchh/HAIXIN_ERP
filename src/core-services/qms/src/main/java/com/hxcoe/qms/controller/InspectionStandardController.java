package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.dto.inspection.InspectionStandardDTO;
import com.hxcoe.qms.service.InspectionStandardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

/**
 * 检验标准管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/inspection-standards")
public class InspectionStandardController {

    @Autowired
    private InspectionStandardService inspectionStandardService;

    /**
     * 分页查询检验标准列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param standardNo 标准编号（模糊）
     * @param materialName 物料名称（模糊）
     * @param status 状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<InspectionStandardDTO>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String standardNo,
            @RequestParam(required = false) String materialName,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        return inspectionStandardService.page(standardNo, materialName, status, pageable);
    }

    /**
     * 获取检验标准详情
     *
     * @param id 标准ID
     * @return 标准详情
     */
    @GetMapping("/{id}")
    public Result<InspectionStandardDTO> getById(@PathVariable Long id) {
        return inspectionStandardService.getById(id);
    }

    /**
     * 根据标准编号获取检验标准
     *
     * @param standardNo 标准编号
     * @return 标准详情
     */
    @GetMapping("/no/{standardNo}")
    public Result<InspectionStandardDTO> getByStandardNo(@PathVariable String standardNo) {
        return inspectionStandardService.getByStandardNo(standardNo);
    }

    /**
     * 创建检验标准
     *
     * @param dto 检验标准
     * @return 创建结果
     */
    @PostMapping
    public Result<InspectionStandardDTO> create(@RequestBody InspectionStandardDTO dto) {
        return inspectionStandardService.create(dto);
    }

    /**
     * 更新检验标准
     *
     * @param id 标准ID
     * @param dto 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<InspectionStandardDTO> update(@PathVariable Long id, @RequestBody InspectionStandardDTO dto) {
        return inspectionStandardService.update(id, dto);
    }

    /**
     * 删除检验标准
     *
     * @param id 标准ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return inspectionStandardService.delete(id);
    }

    /**
     * 启用检验标准
     *
     * @param id 标准ID
     * @return 处理结果
     */
    @PutMapping("/{id}/activate")
    public Result<Void> activate(@PathVariable Long id) {
        return inspectionStandardService.activate(id);
    }

    /**
     * 停用检验标准
     *
     * @param id 标准ID
     * @return 处理结果
     */
    @PutMapping("/{id}/deactivate")
    public Result<Void> deactivate(@PathVariable Long id) {
        return inspectionStandardService.deactivate(id);
    }
}
