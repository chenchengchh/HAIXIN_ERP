package com.hxcoe.mes.controller;

import com.hxcoe.mes.entity.ProductionExecutionEntity;
import com.hxcoe.mes.dto.ProductionQuantityUpdateRequest;
import com.hxcoe.mes.service.ProductionExecutionService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 生产执行控制器
 */
@RestController
@RequestMapping({"/mes/production", "/mes/v1/production", "/api/v1/mes/production", "/api/mes/production"})
public class ProductionExecutionController {

    @Autowired
    private ProductionExecutionService productionExecutionService;

    /**
     * 创建生产执行记录
     *
     * @param productionExecutionEntity 生产执行实体
     * @return 创建结果
     */
    @PostMapping
    public Result<ProductionExecutionEntity> createProductionExecution(@RequestBody ProductionExecutionEntity productionExecutionEntity) {
        ProductionExecutionEntity result = productionExecutionService.createProductionExecution(productionExecutionEntity);
        return Result.success("生产执行记录创建成功", result);
    }

    /**
     * 根据ID查询生产执行记录
     *
     * @param id 主键ID
     * @return 查询结果
     */
    @GetMapping("/{id}")
    public Result<ProductionExecutionEntity> getProductionExecutionById(@PathVariable("id") Long id) {
        ProductionExecutionEntity result = productionExecutionService.getProductionExecutionById(id);
        if (result != null) {
            return Result.success("生产执行记录查询成功", result);
        } else {
            return Result.fail("生产执行记录不存在");
        }
    }

    /**
     * 根据生产执行单号查询生产执行记录
     *
     * @param executionNo 生产执行单号
     * @return 查询结果
     */
    @GetMapping("/no/{executionNo}")
    public Result<ProductionExecutionEntity> getProductionExecutionByNo(@PathVariable("executionNo") String executionNo) {
        ProductionExecutionEntity result = productionExecutionService.getProductionExecutionByNo(executionNo);
        if (result != null) {
            return Result.success("生产执行记录查询成功", result);
        } else {
            return Result.fail("生产执行记录不存在");
        }
    }

    /**
     * 更新生产执行记录
     *
     * @param productionExecutionEntity 生产执行实体
     * @return 更新结果
     */
    @PutMapping
    public Result<ProductionExecutionEntity> updateProductionExecution(@RequestBody ProductionExecutionEntity productionExecutionEntity) {
        ProductionExecutionEntity result = productionExecutionService.updateProductionExecution(productionExecutionEntity);
        return Result.success("生产执行记录更新成功", result);
    }

    /**
     * 删除生产执行记录
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> deleteProductionExecution(@PathVariable("id") Long id) {
        boolean result = productionExecutionService.deleteProductionExecution(id);
        if (result) {
            return Result.success("生产执行记录删除成功", result);
        } else {
            return Result.fail("生产执行记录不存在");
        }
    }

    /**
     * 分页查询生产执行记录
     *
     * @param page 当前页码
     * @param size 每页条数
     * @param executionNo 生产执行单号
     * @param productionOrderNo 生产订单号
     * @param productCode 产品编码
     * @param productionLine 生产线
     * @param executionStatus 生产状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<ProductionExecutionEntity>> getProductionExecutionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            String executionNo,
            String productionOrderNo,
            String productCode,
            String productionLine,
            Integer executionStatus,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        PageResult<ProductionExecutionEntity> result = productionExecutionService.getProductionExecutionList(page, size, executionNo, productionOrderNo, productCode, productionLine, executionStatus, startDate, endDate);
        return Result.success("生产执行记录列表查询成功", result);
    }

    /**
     * 根据生产状态查询生产执行记录
     *
     * @param executionStatus 生产状态
     * @return 生产执行实体列表
     */
    @GetMapping("/status/{executionStatus}")
    public Result<Object> getProductionExecutionByStatus(@PathVariable("executionStatus") Integer executionStatus) {
        return Result.success("生产执行记录查询成功", productionExecutionService.getProductionExecutionByStatus(executionStatus));
    }

    /**
     * 开始生产执行
     *
     * @param id 生产执行ID
     * @return 操作结果
     */
    @PostMapping("/{id}/start")
    public Result<Boolean> startProductionExecution(@PathVariable("id") Long id) {
        boolean result = productionExecutionService.startProductionExecution(id);
        if (result) {
            return Result.success("生产执行开始成功", result);
        } else {
            return Result.fail("生产执行开始失败，状态不允许");
        }
    }

    /**
     * 暂停生产执行
     *
     * @param id 生产执行ID
     * @return 操作结果
     */
    @PostMapping("/{id}/pause")
    public Result<Boolean> pauseProductionExecution(@PathVariable("id") Long id) {
        boolean result = productionExecutionService.pauseProductionExecution(id);
        if (result) {
            return Result.success("生产执行暂停成功", result);
        } else {
            return Result.fail("生产执行暂停失败，状态不允许");
        }
    }

    /**
     * 恢复生产执行
     *
     * @param id 生产执行ID
     * @return 操作结果
     */
    @PostMapping("/{id}/resume")
    public Result<Boolean> resumeProductionExecution(@PathVariable("id") Long id) {
        boolean result = productionExecutionService.resumeProductionExecution(id);
        if (result) {
            return Result.success("生产执行恢复成功", result);
        } else {
            return Result.fail("生产执行恢复失败，状态不允许");
        }
    }

    /**
     * 完成生产执行
     *
     * @param id 生产执行ID
     * @return 操作结果
     */
    @PostMapping("/{id}/complete")
    public Result<Boolean> completeProductionExecution(@PathVariable("id") Long id) {
        boolean result = productionExecutionService.completeProductionExecution(id);
        if (result) {
            return Result.success("生产执行完成成功", result);
        } else {
            return Result.fail("生产执行完成失败，状态不允许");
        }
    }

    /**
     * 取消生产执行
     *
     * @param id 生产执行ID
     * @return 操作结果
     */
    @PostMapping("/{id}/cancel")
    public Result<Boolean> cancelProductionExecution(@PathVariable("id") Long id) {
        boolean result = productionExecutionService.cancelProductionExecution(id);
        if (result) {
            return Result.success("生产执行取消成功", result);
        } else {
            return Result.fail("生产执行取消失败，状态不允许");
        }
    }

    /**
     * 更新生产数量
     *
     * @param id 生产执行ID
     * @param actualQuantity 实际数量
     * @param qualifiedQuantity 合格数量
     * @param unqualifiedQuantity 不合格数量
     * @return 更新结果
     */
    @PutMapping("/{id}/quantity")
    public Result<Boolean> updateProductionQuantity(
            @PathVariable("id") Long id,
            @RequestBody(required = false) ProductionQuantityUpdateRequest body,
            @RequestParam(value = "actualQuantity", required = false) Integer actualQuantity,
            @RequestParam(value = "qualifiedQuantity", required = false) Integer qualifiedQuantity,
            @RequestParam(value = "unqualifiedQuantity", required = false) Integer unqualifiedQuantity) {
        Integer nextActualQuantity = body != null && body.getActualQuantity() != null ? body.getActualQuantity() : actualQuantity;
        Integer nextQualifiedQuantity = body != null && body.getQualifiedQuantity() != null ? body.getQualifiedQuantity() : qualifiedQuantity;
        Integer nextUnqualifiedQuantity = body != null && body.getUnqualifiedQuantity() != null ? body.getUnqualifiedQuantity() : unqualifiedQuantity;

        boolean result = productionExecutionService.updateProductionQuantity(id, nextActualQuantity, nextQualifiedQuantity, nextUnqualifiedQuantity);
        if (result) {
            return Result.success("生产数量更新成功", result);
        } else {
            return Result.fail("生产数量更新失败，生产执行记录不存在");
        }
    }

    /**
     * 测试生产执行接口
     *
     * @return 测试结果
     */
    @GetMapping("/test")
    public Result<String> testProductionExecution() {
        return Result.success("生产执行接口测试成功");
    }
}
