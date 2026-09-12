package com.hxcoe.erp.dto.production;

import java.math.BigDecimal;

public class ProductionOrderDto {
    private Long id;
    private String orderNo;
    private String source;
    private String productId;
    private String productCode;
    private String productName;
    private String specification;
    private String unit;
    private BigDecimal plannedQty;
    private BigDecimal actualQty;
    private String startDate;
    private String endDate;
    private String workshop;
    private String productionLine;
    private String status;
    /** MES 集成状态（F1 前端配套：NOT_SENT/PENDING/CONFIRMED） */
    private String mesIntegrationStatus;
    private String priority;
    private String remark;
    private String creator;
    private String createTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getPlannedQty() { return plannedQty; }
    public void setPlannedQty(BigDecimal plannedQty) { this.plannedQty = plannedQty; }
    public BigDecimal getActualQty() { return actualQty; }
    public void setActualQty(BigDecimal actualQty) { this.actualQty = actualQty; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getWorkshop() { return workshop; }
    public void setWorkshop(String workshop) { this.workshop = workshop; }
    public String getProductionLine() { return productionLine; }
    public void setProductionLine(String productionLine) { this.productionLine = productionLine; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMesIntegrationStatus() { return mesIntegrationStatus; }
    public void setMesIntegrationStatus(String mesIntegrationStatus) { this.mesIntegrationStatus = mesIntegrationStatus; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
