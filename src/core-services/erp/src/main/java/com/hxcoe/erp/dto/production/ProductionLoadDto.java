package com.hxcoe.erp.dto.production;

import java.math.BigDecimal;

public class ProductionLoadDto {
    private Long id;
    private String workshop;
    private String productionLine;
    private String date;
    private String departmentName;
    private String workCenterName;
    private String productName;
    private BigDecimal plannedLoad;
    private BigDecimal actualLoad;
    private BigDecimal availableCapacity;
    private BigDecimal loadRate;
    private String loadStatus;
    private String period;
    private String periodDate;
    private BigDecimal capacityUtilization;
    private String remark;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWorkshop() { return workshop; }
    public void setWorkshop(String workshop) { this.workshop = workshop; }
    public String getProductionLine() { return productionLine; }
    public void setProductionLine(String productionLine) { this.productionLine = productionLine; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getWorkCenterName() { return workCenterName; }
    public void setWorkCenterName(String workCenterName) { this.workCenterName = workCenterName; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getPlannedLoad() { return plannedLoad; }
    public void setPlannedLoad(BigDecimal plannedLoad) { this.plannedLoad = plannedLoad; }
    public BigDecimal getActualLoad() { return actualLoad; }
    public void setActualLoad(BigDecimal actualLoad) { this.actualLoad = actualLoad; }
    public BigDecimal getAvailableCapacity() { return availableCapacity; }
    public void setAvailableCapacity(BigDecimal availableCapacity) { this.availableCapacity = availableCapacity; }
    public BigDecimal getLoadRate() { return loadRate; }
    public void setLoadRate(BigDecimal loadRate) { this.loadRate = loadRate; }
    public String getLoadStatus() { return loadStatus; }
    public void setLoadStatus(String loadStatus) { this.loadStatus = loadStatus; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getPeriodDate() { return periodDate; }
    public void setPeriodDate(String periodDate) { this.periodDate = periodDate; }
    public BigDecimal getCapacityUtilization() { return capacityUtilization; }
    public void setCapacityUtilization(BigDecimal capacityUtilization) { this.capacityUtilization = capacityUtilization; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
