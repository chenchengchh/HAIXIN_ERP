package com.hxcoe.mes.controller;

import com.hxcoe.common.dto.mes.MesReportingCreateDTO;
import com.hxcoe.common.dto.mes.MesReportingUpdateDTO;
import com.hxcoe.mes.entity.ProductionReportEntity;
import com.hxcoe.mes.dto.ProductionReportStatusUpdateRequest;
import com.hxcoe.mes.service.ProductionReportService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 生产报工控制器
 */
@RestController
@RequestMapping({"/mes/reporting", "/mes/v1/reporting", "/api/v1/mes/reporting", "/api/mes/reporting"})
public class ProductionReportController {

    @Autowired
    private ProductionReportService productionReportService;

    /**
     * 创建生产报工记录
     *
     * @param productionReportEntity 生产报工实体
     * @return 创建结果
     */
    @PostMapping
    public Result<ProductionReportEntity> createProductionReport(@RequestBody ProductionReportEntity productionReportEntity) {
        ProductionReportEntity result = productionReportService.createProductionReport(productionReportEntity);
        return Result.success("生产报工记录创建成功", result);
    }

    /**
     * 根据ID查询生产报工记录
     *
     * @param id 主键ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    public Result<ProductionReportEntity> getProductionReportById(@PathVariable("id") Long id) {
        ProductionReportEntity result = productionReportService.getProductionReportById(id);
        if (result != null) {
            return Result.success("生产报工记录查询成功", result);
        } else {
            return Result.fail("生产报工记录不存在");
        }
    }

    /**
     * 根据报工单号查询生产报工记录
     *
     * @param reportNo 报工单号
     * @return 查询结果
     */
    @GetMapping("/no/{reportNo}")
    public Result<ProductionReportEntity> getProductionReportByNo(@PathVariable("reportNo") String reportNo) {
        ProductionReportEntity result = productionReportService.getProductionReportByNo(reportNo);
        if (result != null) {
            return Result.success("生产报工记录查询成功", result);
        } else {
            return Result.fail("生产报工记录不存在");
        }
    }

    /**
     * 更新生产报工记录
     *
     * @param productionReportEntity 生产报工实体
     * @return 更新结果
     */
    @PutMapping
    public Result<ProductionReportEntity> updateProductionReport(@RequestBody ProductionReportEntity productionReportEntity) {
        ProductionReportEntity result = productionReportService.updateProductionReport(productionReportEntity);
        return Result.success("生产报工记录更新成功", result);
    }

    /**
     * 手动创建生产报工（B2 路径对齐：POST /api/v1/mes/reporting/manual）。
     *
     * <p>对应 ERP 侧 {@code MesReportingClient#manual} 契约，接收强类型 DTO。
     * 与 {@link #createProductionReport} 共存，后者保留给前端直接以实体创建。
     *
     * @param dto 报工创建 DTO
     * @return 创建结果
     */
    @PostMapping("/manual")
    public Result<ProductionReportEntity> manualCreateReport(@RequestBody MesReportingCreateDTO dto) {
        ProductionReportEntity result = productionReportService.createManualReport(dto);
        return Result.success("生产报工记录创建成功", result);
    }

    /**
     * 按 ID 更新生产报工（B2 路径对齐：PUT /api/v1/mes/reporting/{id}）。
     *
     * <p>对应 ERP 侧 {@code MesReportingClient#update} 契约，接收强类型 DTO（增量更新）。
     * 与 {@link #updateProductionReport} 共存，后者保留给前端直接以实体全量更新。
     *
     * @param id  报工ID
     * @param dto 报工更新 DTO
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<ProductionReportEntity> updateReportById(@PathVariable("id") Long id,
                                                           @RequestBody MesReportingUpdateDTO dto) {
        ProductionReportEntity result = productionReportService.updateReportById(id, dto);
        if (result != null) {
            return Result.success("生产报工记录更新成功", result);
        } else {
            return Result.fail("生产报工记录不存在");
        }
    }

    /**
     * 删除生产报工记录
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteProductionReport(@PathVariable("id") Long id) {
        boolean result = productionReportService.deleteProductionReport(id);
        if (result) {
            return Result.success("生产报工记录删除成功", result);
        } else {
            return Result.fail("生产报工记录不存在");
        }
    }

    /**
     * 分页查询生产报工记录
     *
     * @param page 当前页码
     * @param size 每页条数
     * @param reportNo 报工单号
     * @param workOrderNo 工单号
     * @param operatorName 操作员姓名
     * @param status 报工状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<ProductionReportEntity>> getProductionReportList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            String reportNo,
            String workOrderNo,
            String operatorName,
            String status,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        PageResult<ProductionReportEntity> result = productionReportService.getProductionReportList(page, size, reportNo, workOrderNo, operatorName, status, startDate, endDate);
        return Result.success("生产报工记录列表查询成功", result);
    }

    /**
     * 根据报工状态查询生产报工记录
     *
     * @param status 报工状态
     * @return 生产报工实体列表
     */
    @GetMapping("/status/{status}")
    public Result<Object> getProductionReportByStatus(@PathVariable("status") String status) {
        return Result.success("生产报工记录查询成功", productionReportService.getProductionReportByStatus(status));
    }

    /**
     * 验证生产报工
     *
     * @param id 生产报工ID
     * @return 操作结果
     */
    @PostMapping("/{id}/verify")
    public Result<Boolean> verifyProductionReport(@PathVariable("id") Long id) {
        boolean result = productionReportService.verifyProductionReport(id);
        if (result) {
            return Result.success("生产报工验证成功", result);
        } else {
            return Result.fail("生产报工验证失败，报工记录不存在");
        }
    }

    /**
     * 批准生产报工
     *
     * @param id 生产报工ID
     * @return 操作结果
     */
    @PostMapping("/{id}/approve")
    public Result<Boolean> approveProductionReport(@PathVariable("id") Long id) {
        boolean result = productionReportService.approveProductionReport(id);
        if (result) {
            return Result.success("生产报工批准成功", result);
        } else {
            return Result.fail("生产报工批准失败，报工记录不存在");
        }
    }

    /**
     * 更新生产报工状态
     *
     * @param id 生产报工ID
     * @param status 新状态
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<ProductionReportEntity> updateProductionReportStatus(
            @PathVariable("id") Long id,
            @RequestBody(required = false) ProductionReportStatusUpdateRequest body,
            @RequestParam(value = "status", required = false) String status) {
        String nextStatus = body != null && body.getStatus() != null ? body.getStatus() : status;
        ProductionReportEntity result = productionReportService.updateProductionReportStatus(id, nextStatus);
        if (result != null) {
            return Result.success("生产报工状态更新成功", result);
        } else {
            return Result.fail("生产报工状态更新失败，报工记录不存在");
        }
    }

    /**
     * 测试生产报工接口
     *
     * @return 测试结果
     */
    @GetMapping("/test")
    public Result<String> testProductionReport() {
        return Result.success("生产报工接口测试成功");
    }
}
