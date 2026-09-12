package com.hxcoe.erp.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 鎴愭湰鏍哥畻瀹炰綋绫? */
@Entity
@Table(name = "erp_cost_calculation")
public class CostCalculationEntity {

    /**
     * 鏃犳柟娉曟瀯閫犳暟锟?     */
    public CostCalculationEntity() {
    }

    /**
     * 鍏ㄩ鏂规硶鏋勫缓锟?     */
    public CostCalculationEntity(Long id, String calculationNo, String calculationType, Long objectId, String objectName, BigDecimal calculationAmount, LocalDateTime calculationDate, String status, String remark, LocalDateTime createdTime, LocalDateTime updatedTime, String createdBy, String updatedBy, Integer isDeleted) {
        this.id = id;
        this.calculationNo = calculationNo;
        this.calculationType = calculationType;
        this.objectId = objectId;
        this.objectName = objectName;
        this.calculationAmount = calculationAmount;
        this.calculationDate = calculationDate;
        this.status = status;
        this.remark = remark;
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
     * 鏍哥畻鍗曞彿
     */
    @Column(name = "calculation_no", unique = true, nullable = false, length = 32)
    private String calculationNo;

    /**
     * 鏍哥畻绫诲瀷锛歱roduct-浜у搧鎴愭湰锛宲roject-椤圭洰鎴愭湰锛宲eriod-鏈熼棿鎴愭湰
     */
    @Column(name = "calculation_type", nullable = false, length = 16)
    private String calculationType;

    /**
     * 鏍哥畻瀵硅薄ID
     */
    @Column(name = "object_id", nullable = false)
    private Long objectId;

    /**
     * 鏍哥畻瀵硅薄鍚嶇О
     */
    @Column(name = "object_name", nullable = false, length = 64)
    private String objectName;

    /**
     * 鏍哥畻閲戦
     */
    @Column(name = "calculation_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal calculationAmount;

    /**
     * 鏍哥畻鏃ユ湡
     */
    @Column(name = "calculation_date", nullable = false)
    private LocalDateTime calculationDate;

    /**
     * 鐘舵€侊細pending-寰呭鐞嗭紝completed-宸插畬鎴?     */
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    /**
     * 澶囨敞
     */
    @Column(name = "remark", length = 255)
    private String remark;

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
     * 鍒涘缓浜?     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /**
     * 鏇存柊浜?     */
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    /**
     * 閫昏緫鍒犻櫎鏍囪瘑锛?-鏈垹闄わ紝1-宸插垹闄?     */
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
     * 获取计算单号
     * @return 计算单号
     */
    public String getCalculationNo() {
        return calculationNo;
    }

    /**
     * 设置计算单号
     * @param calculationNo 计算单号
     */
    public void setCalculationNo(String calculationNo) {
        this.calculationNo = calculationNo;
    }

    /**
     * 获取计算类型
     * @return 计算类型
     */
    public String getCalculationType() {
        return calculationType;
    }

    /**
     * 设置计算类型
     * @param calculationType 计算类型
     */
    public void setCalculationType(String calculationType) {
        this.calculationType = calculationType;
    }

    /**
     * 获取计算对象ID
     * @return 计算对象ID
     */
    public Long getObjectId() {
        return objectId;
    }

    /**
     * 设置计算对象ID
     * @param objectId 计算对象ID
     */
    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    /**
     * 获取计算对象名称
     * @return 计算对象名称
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * 设置计算对象名称
     * @param objectName 计算对象名称
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * 获取计算金额
     * @return 计算金额
     */
    public BigDecimal getCalculationAmount() {
        return calculationAmount;
    }

    /**
     * 设置计算金额
     * @param calculationAmount 计算金额
     */
    public void setCalculationAmount(BigDecimal calculationAmount) {
        this.calculationAmount = calculationAmount;
    }

    /**
     * 获取计算日期
     * @return 计算日期
     */
    public LocalDateTime getCalculationDate() {
        return calculationDate;
    }

    /**
     * 设置计算日期
     * @param calculationDate 计算日期
     */
    public void setCalculationDate(LocalDateTime calculationDate) {
        this.calculationDate = calculationDate;
    }

    /**
     * 获取状态
     * @return 状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态
     * @param status 状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取备注
     * @return 备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置备注
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
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

