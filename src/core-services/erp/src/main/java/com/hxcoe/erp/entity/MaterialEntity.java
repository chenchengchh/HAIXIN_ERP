package com.hxcoe.erp.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物料实体类
 */
@Entity
@Table(name = "erp_material")
public class MaterialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "material_code", unique = true, nullable = false, length = 32)
    private String materialCode;

    @Column(name = "material_name", nullable = false, length = 128)
    private String materialName;

    @Column(name = "material_type", length = 32)
    private String materialType;

    @Column(name = "specification", length = 128)
    private String specification;

    @Column(name = "unit", length = 16)
    private String unit;

    @Column(name = "safety_stock")
    private BigDecimal safetyStock;

    @Column(name = "min_stock")
    private BigDecimal minStock;

    @Column(name = "max_stock")
    private BigDecimal maxStock;

    @Column(name = "lead_time")
    private Integer leadTime;

    @Column(name = "default_supplier", length = 128)
    private String defaultSupplier;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @Column(name = "approval_status", length = 16)
    private String approvalStatus;

    @Column(name = "status", nullable = false)
    private Integer status; // 0-inactive, 1-active

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted;

    public MaterialEntity() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getSafetyStock() { return safetyStock; }
    public void setSafetyStock(BigDecimal safetyStock) { this.safetyStock = safetyStock; }
    public BigDecimal getMinStock() { return minStock; }
    public void setMinStock(BigDecimal minStock) { this.minStock = minStock; }
    public BigDecimal getMaxStock() { return maxStock; }
    public void setMaxStock(BigDecimal maxStock) { this.maxStock = maxStock; }
    public Integer getLeadTime() { return leadTime; }
    public void setLeadTime(Integer leadTime) { this.leadTime = leadTime; }
    public String getDefaultSupplier() { return defaultSupplier; }
    public void setDefaultSupplier(String defaultSupplier) { this.defaultSupplier = defaultSupplier; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
