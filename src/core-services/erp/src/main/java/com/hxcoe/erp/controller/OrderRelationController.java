package com.hxcoe.erp.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.erp.model.OrderRelationModel;
import com.hxcoe.erp.service.OrderRelationService;
import com.hxcoe.common.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单关联控制器
 * 用于管理销售订单、采购订单、生产订单之间的关联关系
 */
@RestController
@RequestMapping("/api/v1/erp/order-relation")
public class OrderRelationController {
    
    @Autowired
    private OrderRelationService orderRelationService;
    
    /**
     * 创建订单关联关系
     * @param orderRelationModel 订单关联模型
     * @return 创建后的订单关联模型
     */
    @PostMapping
    public ApiResponse<OrderRelationModel> createOrderRelation(@RequestBody OrderRelationModel orderRelationModel) {
        OrderRelationModel result = orderRelationService.createOrderRelation(orderRelationModel);
        return ApiResponse.success("创建订单关联成功", result);
    }
    
    /**
     * 根据ID获取订单关联关系
     * @param id 订单关联ID
     * @return 订单关联模型
     */
    @GetMapping("/{id}")
    public ApiResponse<OrderRelationModel> getOrderRelationById(@PathVariable String id) {
        OrderRelationModel result = orderRelationService.getOrderRelationById(id);
        if (result == null) {
            return ApiResponse.error(404, "订单关联不存在");
        }
        return ApiResponse.success("获取订单关联成功", result);
    }
    
    /**
     * 根据销售订单ID获取订单关联关系列表
     * @param salesOrderId 销售订单ID
     * @return 订单关联模型列表
     */
    @GetMapping("/sales-order/{salesOrderId}")
    public ApiResponse<List<OrderRelationModel>> getOrderRelationsBySalesOrderId(@PathVariable String salesOrderId) {
        List<OrderRelationModel> result = orderRelationService.getOrderRelationsBySalesOrderId(salesOrderId);
        return ApiResponse.success("获取销售订单关联列表成功", result);
    }
    
    /**
     * 根据采购订单ID获取订单关联关系列表
     * @param purchaseOrderId 采购订单ID
     * @return 订单关联模型列表
     */
    @GetMapping("/purchase-order/{purchaseOrderId}")
    public ApiResponse<List<OrderRelationModel>> getOrderRelationsByPurchaseOrderId(@PathVariable String purchaseOrderId) {
        List<OrderRelationModel> result = orderRelationService.getOrderRelationsByPurchaseOrderId(purchaseOrderId);
        return ApiResponse.success("获取采购订单关联列表成功", result);
    }
    
    /**
     * 根据生产订单ID获取订单关联关系列表
     * @param productionOrderId 生产订单ID
     * @return 订单关联模型列表
     */
    @GetMapping("/production-order/{productionOrderId}")
    public ApiResponse<List<OrderRelationModel>> getOrderRelationsByProductionOrderId(@PathVariable String productionOrderId) {
        List<OrderRelationModel> result = orderRelationService.getOrderRelationsByProductionOrderId(productionOrderId);
        return ApiResponse.success("获取生产订单关联列表成功", result);
    }
    
    /**
     * 根据物料ID获取订单关联关系列表
     * @param materialId 物料ID
     * @return 订单关联模型列表
     */
    @GetMapping("/material/{materialId}")
    public ApiResponse<List<OrderRelationModel>> getOrderRelationsByMaterialId(@PathVariable String materialId) {
        List<OrderRelationModel> result = orderRelationService.getOrderRelationsByMaterialId(materialId);
        return ApiResponse.success("获取物料订单关联列表成功", result);
    }
    
    /**
     * 更新订单关联关系
     * @param id 订单关联ID
     * @param orderRelationModel 订单关联模型
     * @return 更新后的订单关联模型
     */
    @PutMapping("/{id}")
    public ApiResponse<OrderRelationModel> updateOrderRelation(@PathVariable String id, @RequestBody OrderRelationModel orderRelationModel) {
        OrderRelationModel result = orderRelationService.updateOrderRelation(orderRelationModel);
        return ApiResponse.success("更新订单关联成功", result);
    }
    
    /**
     * 删除订单关联关系
     * @param id 订单关联ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> deleteOrderRelation(@PathVariable String id) {
        boolean result = orderRelationService.deleteOrderRelation(id);
        return ApiResponse.success("删除订单关联成功", result);
    }
    
    /**
     * 查询订单关联关系列表
     * @param page 页码
     * @param size 每页数量
     * @param salesOrderId 销售订单ID
     * @param purchaseOrderId 采购订单ID
     * @param productionOrderId 生产订单ID
     * @param materialId 物料ID
     * @param status 状态
     * @return 分页结果
     */
    @GetMapping
    public ApiResponse<PageResult<OrderRelationModel>> queryOrderRelations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String salesOrderId,
            @RequestParam(required = false) String purchaseOrderId,
            @RequestParam(required = false) String productionOrderId,
            @RequestParam(required = false) String materialId,
            @RequestParam(required = false) String status) {
        PageResult<OrderRelationModel> result = orderRelationService.queryOrderRelations(page, size, salesOrderId, purchaseOrderId, productionOrderId, materialId, status);
        return ApiResponse.success("查询订单关联列表成功", result);
    }
}
