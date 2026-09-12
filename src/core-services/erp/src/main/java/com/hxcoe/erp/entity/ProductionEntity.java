package com.hxcoe.erp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 生产实体类 */
@Entity
@Table(name = "erp_production")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductionEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 生产单号
     */
    @Column(name = "production_no", unique = true, nullable = false, length = 32)
    private String productionNo;

    /**
     * 产品编码
     */
    @Column(name = "product_code", nullable = false, length = 32)
    private String productCode;

    /**
     * 产品名称
     */
    @Column(name = "product_name", nullable = false, length = 128)
    private String productName;

    /**
     * 生产数量
     */
    @Column(name = "production_quantity", nullable = false)
    private Integer productionQuantity;

    /**
     * 已完成数量
     */
    @Column(name = "completed_quantity", nullable = false)
    private Integer completedQuantity;

    /**
     * 生产状态：0-草稿，1-已审核/待生产，2-生产中，3-已完成，4-已暂停，5-已取消
     */
    @Column(name = "production_status", nullable = false)
    private Integer productionStatus;

    /**
     * MES 集成状态（F1 前端配套）。
     *
     * <p>标记生产单推送 MES 的进度：
     * <ul>
     *   <li>NULL / NOT_SENT：未推送</li>
     *   <li>PENDING：已入队 Outbox，等待异步推送</li>
     *   <li>CONFIRMED：MES 已接收并创建工单</li>
     * </ul>
     */
    @Column(name = "mes_integration_status", length = 16)
    private String mesIntegrationStatus;

    /**
     * 生产车间
     */
    @Column(name = "workshop", length = 32)
    private String workshop;

    /**
     * 生产线
     */
    @Column(name = "production_line", length = 32)
    private String productionLine;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "priority")
    private Integer priority;

    /**
     * 计划开始时间
     */
    @Column(name = "plan_start_time")
    private LocalDateTime planStartTime;

    /**
     * 计划结束时间
     */
    @Column(name = "plan_end_time")
    private LocalDateTime planEndTime;

    /**
     * 实际开始时间
     */
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    /**
     * 实际结束时间
     */
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
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

    public String getProductionNo() {
        return productionNo;
    }

    public void setProductionNo(String productionNo) {
        this.productionNo = productionNo;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getProductionQuantity() {
        return productionQuantity;
    }

    public void setProductionQuantity(Integer productionQuantity) {
        this.productionQuantity = productionQuantity;
    }

    public Integer getCompletedQuantity() {
        return completedQuantity;
    }

    public void setCompletedQuantity(Integer completedQuantity) {
        this.completedQuantity = completedQuantity;
    }

    public Integer getProductionStatus() {
        return productionStatus;
    }

    public void setProductionStatus(Integer productionStatus) {
        this.productionStatus = productionStatus;
    }

    public String getMesIntegrationStatus() {
        return mesIntegrationStatus;
    }

    public void setMesIntegrationStatus(String mesIntegrationStatus) {
        this.mesIntegrationStatus = mesIntegrationStatus;
    }

    public String getWorkshop() {
        return workshop;
    }

    public void setWorkshop(String workshop) {
        this.workshop = workshop;
    }

    public String getProductionLine() {
        return productionLine;
    }

    public void setProductionLine(String productionLine) {
        this.productionLine = productionLine;
    }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public LocalDateTime getPlanStartTime() {
        return planStartTime;
    }

    public void setPlanStartTime(LocalDateTime planStartTime) {
        this.planStartTime = planStartTime;
    }

    public LocalDateTime getPlanEndTime() {
        return planEndTime;
    }

    public void setPlanEndTime(LocalDateTime planEndTime) {
        this.planEndTime = planEndTime;
    }

    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }

    public void setActualStartTime(LocalDateTime actualStartTime) {
        this.actualStartTime = actualStartTime;
    }

    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }

    public void setActualEndTime(LocalDateTime actualEndTime) {
        this.actualEndTime = actualEndTime;
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
