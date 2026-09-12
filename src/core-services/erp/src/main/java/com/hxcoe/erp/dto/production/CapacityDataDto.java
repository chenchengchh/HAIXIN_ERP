package com.hxcoe.erp.dto.production;

import java.math.BigDecimal;

public class CapacityDataDto {
    private String departmentName;
    private String workCenterName;
    private String resourceName;
    private BigDecimal availableCapacity;
    private BigDecimal usedCapacity;
    private BigDecimal utilizationRate;
    private String period;
    private String periodDate;
    private String remark;

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getWorkCenterName() { return workCenterName; }
    public void setWorkCenterName(String workCenterName) { this.workCenterName = workCenterName; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public BigDecimal getAvailableCapacity() { return availableCapacity; }
    public void setAvailableCapacity(BigDecimal availableCapacity) { this.availableCapacity = availableCapacity; }
    public BigDecimal getUsedCapacity() { return usedCapacity; }
    public void setUsedCapacity(BigDecimal usedCapacity) { this.usedCapacity = usedCapacity; }
    public BigDecimal getUtilizationRate() { return utilizationRate; }
    public void setUtilizationRate(BigDecimal utilizationRate) { this.utilizationRate = utilizationRate; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getPeriodDate() { return periodDate; }
    public void setPeriodDate(String periodDate) { this.periodDate = periodDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
