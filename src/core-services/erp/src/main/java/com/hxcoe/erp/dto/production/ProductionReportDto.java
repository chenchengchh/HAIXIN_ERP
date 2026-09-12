package com.hxcoe.erp.dto.production;

import java.math.BigDecimal;

public class ProductionReportDto {
    private Long id;
    private String reportNo;
    private String workshopOrderNo;
    private String productionOrderNo;
    private String productCode;
    private String productName;
    private String reportDate;
    private BigDecimal reportedQty;
    private BigDecimal qualifiedQty;
    private BigDecimal defectiveQty;
    private String operator;
    private String status;
    private String createTime;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReportNo() { return reportNo; }
    public void setReportNo(String reportNo) { this.reportNo = reportNo; }
    public String getWorkshopOrderNo() { return workshopOrderNo; }
    public void setWorkshopOrderNo(String workshopOrderNo) { this.workshopOrderNo = workshopOrderNo; }
    public String getProductionOrderNo() { return productionOrderNo; }
    public void setProductionOrderNo(String productionOrderNo) { this.productionOrderNo = productionOrderNo; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getReportDate() { return reportDate; }
    public void setReportDate(String reportDate) { this.reportDate = reportDate; }
    public BigDecimal getReportedQty() { return reportedQty; }
    public void setReportedQty(BigDecimal reportedQty) { this.reportedQty = reportedQty; }
    public BigDecimal getQualifiedQty() { return qualifiedQty; }
    public void setQualifiedQty(BigDecimal qualifiedQty) { this.qualifiedQty = qualifiedQty; }
    public BigDecimal getDefectiveQty() { return defectiveQty; }
    public void setDefectiveQty(BigDecimal defectiveQty) { this.defectiveQty = defectiveQty; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

