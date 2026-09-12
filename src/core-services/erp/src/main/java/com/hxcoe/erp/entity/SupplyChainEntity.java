package com.hxcoe.erp.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 渚涘簲閾惧疄浣撶被
 */
@Entity
@Table(name = "erp_supply_chain")
public class SupplyChainEntity {

    /**
     * 鏃犳柟娉曟瀯閫犳暟锟?     */
    public SupplyChainEntity() {
    }

    /**
     * 鍏ㄩ鏂规硶鏋勫缓锟?     */
    public SupplyChainEntity(Long id, String scNo, Integer businessType, String materialCode, String materialName, BigDecimal quantity, String unit, BigDecimal unitPrice, BigDecimal totalAmount, String warehouseCode, Integer status, LocalDateTime createdTime, LocalDateTime updatedTime, String createdBy, String updatedBy, Integer isDeleted) {
        this.id = id;
        this.scNo = scNo;
        this.businessType = businessType;
        this.materialCode = materialCode;
        this.materialName = materialName;
        this.quantity = quantity;
        this.unit = unit;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.warehouseCode = warehouseCode;
        this.status = status;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.isDeleted = isDeleted;
    }

    /**
     * 涓婚敭ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 渚涘簲閾惧崟锟?     */
    @Column(name = "sc_no", unique = true, nullable = false, length = 32)
    private String scNo;

    /**
     * 涓氬姟绫诲瀷锟?-閲囪喘锟?-閿€鍞紝3-搴撳瓨
     */
    @Column(name = "business_type", nullable = false)
    private Integer businessType;

    /**
     * 鐗╂枡缂栫爜
     */
    @Column(name = "material_code", nullable = false, length = 32)
    private String materialCode;

    /**
     * 鐗╂枡鍚嶇О
     */
    @Column(name = "material_name", nullable = false, length = 128)
    private String materialName;

    /**
     * 鏁伴噺
     */
    @Column(name = "quantity", nullable = false, precision = 18, scale = 6)
    private BigDecimal quantity;

    /**
     * 鍗曚綅
     */
    @Column(name = "unit", nullable = false, length = 16)
    private String unit;

    /**
     * 鍗曚环
     */
    @Column(name = "unit_price", precision = 18, scale = 6)
    private BigDecimal unitPrice;

    /**
     * 鎬婚噾锟?     */
    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    /**
     * 浠撳簱缂栫爜
     */
    @Column(name = "warehouse_code", nullable = false, length = 32)
    private String warehouseCode;

    /**
     * 鐘舵€侊細1-寰呭鐞嗭紝2-宸插鐞嗭紝3-宸插畬锟?     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 鍒涘缓鏃堕棿
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 鏇存柊鏃堕棿
     */
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    /**
     * 鍒涘缓锟?     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /**
     * 鏇存柊锟?     */
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    /**
     * 閫昏緫鍒犻櫎鏍囪瘑锟?-鏈垹闄わ紝1-宸插垹锟?     */
    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted;

    /**
     * 获取主键ID
     * @return 主键ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键ID
     * @param id 主键ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取供应链单号
     * @return 供应链单号
     */
    public String getScNo() {
        return scNo;
    }

    /**
     * 设置供应链单号
     * @param scNo 供应链单号
     */
    public void setScNo(String scNo) {
        this.scNo = scNo;
    }

    /**
     * 获取业务类型
     * @return 业务类型
     */
    public Integer getBusinessType() {
        return businessType;
    }

    /**
     * 设置业务类型
     * @param businessType 业务类型
     */
    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
    }

    /**
     * 获取物料编码
     * @return 物料编码
     */
    public String getMaterialCode() {
        return materialCode;
    }

    /**
     * 设置物料编码
     * @param materialCode 物料编码
     */
    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    /**
     * 获取物料名称
     * @return 物料名称
     */
    public String getMaterialName() {
        return materialName;
    }

    /**
     * 设置物料名称
     * @param materialName 物料名称
     */
    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    /**
     * 获取数量
     * @return 数量
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * 设置数量
     * @param quantity 数量
     */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    /**
     * 获取单位
     * @return 单位
     */
    public String getUnit() {
        return unit;
    }

    /**
     * 设置单位
     * @param unit 单位
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * 获取单价
     * @return 单价
     */
    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    /**
     * 设置单价
     * @param unitPrice 单价
     */
    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    /**
     * 获取总金额
     * @return 总金额
     */
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    /**
     * 设置总金额
     * @param totalAmount 总金额
     */
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    /**
     * 获取仓库编码
     * @return 仓库编码
     */
    public String getWarehouseCode() {
        return warehouseCode;
    }

    /**
     * 设置仓库编码
     * @param warehouseCode 仓库编码
     */
    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    /**
     * 获取状态
     * @return 状态
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置状态
     * @param status 状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取创建时间
     * @return 创建时间
     */
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    /**
     * 设置创建时间
     * @param createdTime 创建时间
     */
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * 获取更新时间
     * @return 更新时间
     */
    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    /**
     * 设置更新时间
     * @param updatedTime 更新时间
     */
    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * 获取创建者
     * @return 创建者
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * 设置创建者
     * @param createdBy 创建者
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * 获取更新者
     * @return 更新者
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * 设置更新者
     * @param updatedBy 更新者
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * 获取是否删除
     * @return 是否删除
     */
    public Integer getIsDeleted() {
        return isDeleted;
    }

    /**
     * 设置是否删除
     * @param isDeleted 是否删除
     */
    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}

