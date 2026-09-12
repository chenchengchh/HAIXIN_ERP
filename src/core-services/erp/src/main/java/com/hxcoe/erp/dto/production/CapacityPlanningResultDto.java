package com.hxcoe.erp.dto.production;

import java.math.BigDecimal;

public class CapacityPlanningResultDto {
    private String departmentName;
    private String workCenterName;
    private String resourceName;
    private BigDecimal plannedLoad;
    private BigDecimal availableCapacity;
    private BigDecimal capacityGap;
    private String suggestionType;
    private String suggestionContent;
    private String remark;

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getWorkCenterName() { return workCenterName; }
    public void setWorkCenterName(String workCenterName) { this.workCenterName = workCenterName; }
    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
    public BigDecimal getPlannedLoad() { return plannedLoad; }
    public void setPlannedLoad(BigDecimal plannedLoad) { this.plannedLoad = plannedLoad; }
    public BigDecimal getAvailableCapacity() { return availableCapacity; }
    public void setAvailableCapacity(BigDecimal availableCapacity) { this.availableCapacity = availableCapacity; }
    public BigDecimal getCapacityGap() { return capacityGap; }
    public void setCapacityGap(BigDecimal capacityGap) { this.capacityGap = capacityGap; }
    public String getSuggestionType() { return suggestionType; }
    public void setSuggestionType(String suggestionType) { this.suggestionType = suggestionType; }
    public String getSuggestionContent() { return suggestionContent; }
    public void setSuggestionContent(String suggestionContent) { this.suggestionContent = suggestionContent; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
