package com.hxcoe.erp.dto.supplychain;

import java.math.BigDecimal;

public class MrpResultDto {
    private String materialCode;
    private String materialName;
    private String specification;
    private String materialType;
    private BigDecimal currentQty;
    private BigDecimal safetyStock;
    private BigDecimal demandQty;
    private BigDecimal supplyQty;
    private String suggestionType;
    private BigDecimal suggestionQty;
    private String suggestionDate;
    private String remark;

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }
    public String getMaterialType() { return materialType; }
    public void setMaterialType(String materialType) { this.materialType = materialType; }
    public BigDecimal getCurrentQty() { return currentQty; }
    public void setCurrentQty(BigDecimal currentQty) { this.currentQty = currentQty; }
    public BigDecimal getSafetyStock() { return safetyStock; }
    public void setSafetyStock(BigDecimal safetyStock) { this.safetyStock = safetyStock; }
    public BigDecimal getDemandQty() { return demandQty; }
    public void setDemandQty(BigDecimal demandQty) { this.demandQty = demandQty; }
    public BigDecimal getSupplyQty() { return supplyQty; }
    public void setSupplyQty(BigDecimal supplyQty) { this.supplyQty = supplyQty; }
    public String getSuggestionType() { return suggestionType; }
    public void setSuggestionType(String suggestionType) { this.suggestionType = suggestionType; }
    public BigDecimal getSuggestionQty() { return suggestionQty; }
    public void setSuggestionQty(BigDecimal suggestionQty) { this.suggestionQty = suggestionQty; }
    public String getSuggestionDate() { return suggestionDate; }
    public void setSuggestionDate(String suggestionDate) { this.suggestionDate = suggestionDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}

