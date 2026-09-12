package com.hxcoe.erp.model;

/**
 * 订单关联模型
 * 用于表示销售订单、采购订单、生产订单之间的关联关系
 */
public class OrderRelationModel {
    
    /**
     * 销售订单ID
     */
    private String salesOrderId;
    
    /**
     * 采购订单ID
     */
    private String purchaseOrderId;
    
    /**
     * 生产订单ID
     */
    private String productionOrderId;
    
    /**
     * 物料ID
     */
    private String materialId;
    
    /**
     * 数量
     */
    private Integer quantity;
    
    /**
     * 关联状态
     * - ACTIVE: 活跃
     * - INACTIVE: 非活跃
     * - DELETED: 已删除
     */
    private String status;
    
    /**
     * 关联类型
     * - DIRECT: 直接关联
     * - INDIRECT: 间接关联
     */
    private String relationType;

    /**
     * 获取销售订单ID
     * @return 销售订单ID
     */
    public String getSalesOrderId() {
        return salesOrderId;
    }

    /**
     * 设置销售订单ID
     * @param salesOrderId 销售订单ID
     */
    public void setSalesOrderId(String salesOrderId) {
        this.salesOrderId = salesOrderId;
    }

    /**
     * 获取采购订单ID
     * @return 采购订单ID
     */
    public String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    /**
     * 设置采购订单ID
     * @param purchaseOrderId 采购订单ID
     */
    public void setPurchaseOrderId(String purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    /**
     * 获取生产订单ID
     * @return 生产订单ID
     */
    public String getProductionOrderId() {
        return productionOrderId;
    }

    /**
     * 设置生产订单ID
     * @param productionOrderId 生产订单ID
     */
    public void setProductionOrderId(String productionOrderId) {
        this.productionOrderId = productionOrderId;
    }

    /**
     * 获取物料ID
     * @return 物料ID
     */
    public String getMaterialId() {
        return materialId;
    }

    /**
     * 设置物料ID
     * @param materialId 物料ID
     */
    public void setMaterialId(String materialId) {
        this.materialId = materialId;
    }

    /**
     * 获取数量
     * @return 数量
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * 设置数量
     * @param quantity 数量
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * 获取关联状态
     * @return 关联状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置关联状态
     * @param status 关联状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取关联类型
     * @return 关联类型
     */
    public String getRelationType() {
        return relationType;
    }

    /**
     * 设置关联类型
     * @param relationType 关联类型
     */
    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }
}
