package com.hxcoe.erp.service;

import com.hxcoe.erp.model.OrderRelationModel;
import com.hxcoe.common.result.PageResult;

import java.util.List;

/**
 * 订单关联服务接口
 * 用于管理销售订单、采购订单、生产订单之间的关联关系
 */
public interface OrderRelationService {
    
    /**
     * 创建订单关联关系
     * @param orderRelationModel 订单关联模型
     * @return 创建后的订单关联模型
     */
    OrderRelationModel createOrderRelation(OrderRelationModel orderRelationModel);
    
    /**
     * 根据ID获取订单关联关系
     * @param id 订单关联ID
     * @return 订单关联模型
     */
    OrderRelationModel getOrderRelationById(String id);
    
    /**
     * 根据销售订单ID获取订单关联关系列表
     * @param salesOrderId 销售订单ID
     * @return 订单关联模型列表
     */
    List<OrderRelationModel> getOrderRelationsBySalesOrderId(String salesOrderId);
    
    /**
     * 根据采购订单ID获取订单关联关系列表
     * @param purchaseOrderId 采购订单ID
     * @return 订单关联模型列表
     */
    List<OrderRelationModel> getOrderRelationsByPurchaseOrderId(String purchaseOrderId);
    
    /**
     * 根据生产订单ID获取订单关联关系列表
     * @param productionOrderId 生产订单ID
     * @return 订单关联模型列表
     */
    List<OrderRelationModel> getOrderRelationsByProductionOrderId(String productionOrderId);
    
    /**
     * 根据物料ID获取订单关联关系列表
     * @param materialId 物料ID
     * @return 订单关联模型列表
     */
    List<OrderRelationModel> getOrderRelationsByMaterialId(String materialId);
    
    /**
     * 更新订单关联关系
     * @param orderRelationModel 订单关联模型
     * @return 更新后的订单关联模型
     */
    OrderRelationModel updateOrderRelation(OrderRelationModel orderRelationModel);
    
    /**
     * 删除订单关联关系
     * @param id 订单关联ID
     * @return 是否删除成功
     */
    boolean deleteOrderRelation(String id);
    
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
    PageResult<OrderRelationModel> queryOrderRelations(Integer page, Integer size, String salesOrderId, String purchaseOrderId, String productionOrderId, String materialId, String status);
}
