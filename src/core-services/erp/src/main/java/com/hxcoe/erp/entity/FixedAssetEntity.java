package com.hxcoe.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Fixed Asset Entity
 */
@Entity
@Table(name = "erp_fixed_assets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FixedAssetEntity {

    /**
     * Primary Key ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Asset Number
     */
    @Column(name = "asset_no", unique = true, nullable = false, length = 32)
    private String assetNo;

    /**
     * Asset Name
     */
    @Column(name = "asset_name", nullable = false, length = 128)
    private String assetName;

    /**
     * Asset Category
     */
    @Column(name = "asset_category", nullable = false, length = 64)
    private String assetCategory;

    /**
     * Specification
     */
    @Column(name = "specification", length = 128)
    private String specification;

    /**
     * Purchase Date
     */
    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    /**
     * Original Value
     */
    @Column(name = "original_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal originalValue;

    /**
     * Expected Usage Years
     */
    @Column(name = "expected_usage_years", nullable = false)
    private Integer expectedUsageYears;

    /**
     * Expected Residual Rate
     */
    @Column(name = "expected_residual_rate", precision = 5, scale = 4)
    private BigDecimal expectedResidualRate;

    /**
     * Residual Value
     */
    @Column(name = "residual_value", precision = 18, scale = 2)
    private BigDecimal residualValue;

    /**
     * Monthly Depreciation
     */
    @Column(name = "monthly_depreciation", precision = 18, scale = 2)
    private BigDecimal monthlyDepreciation;

    /**
     * Accumulated Depreciation
     */
    @Column(name = "accumulated_depreciation", precision = 18, scale = 2)
    private BigDecimal accumulatedDepreciation;

    /**
     * Net Value
     */
    @Column(name = "net_value", precision = 18, scale = 2)
    private BigDecimal netValue;

    /**
     * Asset Status: 1-In Use, 2-Idle, 3-Scrapped, 4-Disposed
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * Using Department
     */
    @Column(name = "using_department", length = 64)
    private String usingDepartment;

    /**
     * Custodian
     */
    @Column(name = "custodian", length = 64)
    private String custodian;

    /**
     * Location
     */
    @Column(name = "location", length = 128)
    private String location;

    /**
     * Remark
     */
    @Column(name = "remark", length = 255)
    private String remark;

    /**
     * Last Depreciation Date
     */
    @Column(name = "last_depreciation_date")
    private LocalDateTime lastDepreciationDate;

    /**
     * Create Time
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * Update Time
     */
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    /**
     * Created By
     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /**
     * Updated By
     */
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    /**
     * Logical Delete Flag: 0-Not Deleted, 1-Deleted
     */
    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted;

    // 手动添加getter和setter方法，解决Lombok编译问题
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAssetNo() {
        return assetNo;
    }

    public void setAssetNo(String assetNo) {
        this.assetNo = assetNo;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetCategory() {
        return assetCategory;
    }

    public void setAssetCategory(String assetCategory) {
        this.assetCategory = assetCategory;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public BigDecimal getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(BigDecimal originalValue) {
        this.originalValue = originalValue;
    }

    public Integer getExpectedUsageYears() {
        return expectedUsageYears;
    }

    public void setExpectedUsageYears(Integer expectedUsageYears) {
        this.expectedUsageYears = expectedUsageYears;
    }

    public BigDecimal getExpectedResidualRate() {
        return expectedResidualRate;
    }

    public void setExpectedResidualRate(BigDecimal expectedResidualRate) {
        this.expectedResidualRate = expectedResidualRate;
    }

    public BigDecimal getResidualValue() {
        return residualValue;
    }

    public void setResidualValue(BigDecimal residualValue) {
        this.residualValue = residualValue;
    }

    public BigDecimal getMonthlyDepreciation() {
        return monthlyDepreciation;
    }

    public void setMonthlyDepreciation(BigDecimal monthlyDepreciation) {
        this.monthlyDepreciation = monthlyDepreciation;
    }

    public BigDecimal getAccumulatedDepreciation() {
        return accumulatedDepreciation;
    }

    public void setAccumulatedDepreciation(BigDecimal accumulatedDepreciation) {
        this.accumulatedDepreciation = accumulatedDepreciation;
    }

    public BigDecimal getNetValue() {
        return netValue;
    }

    public void setNetValue(BigDecimal netValue) {
        this.netValue = netValue;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUsingDepartment() {
        return usingDepartment;
    }

    public void setUsingDepartment(String usingDepartment) {
        this.usingDepartment = usingDepartment;
    }

    public String getCustodian() {
        return custodian;
    }

    public void setCustodian(String custodian) {
        this.custodian = custodian;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getLastDepreciationDate() {
        return lastDepreciationDate;
    }

    public void setLastDepreciationDate(LocalDateTime lastDepreciationDate) {
        this.lastDepreciationDate = lastDepreciationDate;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}