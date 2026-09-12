package com.hxcoe.erp.dto.supplychain;

import java.util.ArrayList;
import java.util.List;

public class TransferOrderDto {
    private Long id;
    private String transferNo;
    private String sourceNo;
    private String fromWarehouseCode;
    private String toWarehouseCode;
    private String status;
    private Long outboundOrderId;
    private String outboundOrderNo;
    private String outboundStatus;
    private Long asnId;
    private String asnNo;
    private String inboundStatus;
    private String remark;
    private String createTime;
    private String updateTime;
    private List<PurchaseOrderItemDto> items = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTransferNo() { return transferNo; }
    public void setTransferNo(String transferNo) { this.transferNo = transferNo; }
    public String getSourceNo() { return sourceNo; }
    public void setSourceNo(String sourceNo) { this.sourceNo = sourceNo; }
    public String getFromWarehouseCode() { return fromWarehouseCode; }
    public void setFromWarehouseCode(String fromWarehouseCode) { this.fromWarehouseCode = fromWarehouseCode; }
    public String getToWarehouseCode() { return toWarehouseCode; }
    public void setToWarehouseCode(String toWarehouseCode) { this.toWarehouseCode = toWarehouseCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getOutboundOrderId() { return outboundOrderId; }
    public void setOutboundOrderId(Long outboundOrderId) { this.outboundOrderId = outboundOrderId; }
    public String getOutboundOrderNo() { return outboundOrderNo; }
    public void setOutboundOrderNo(String outboundOrderNo) { this.outboundOrderNo = outboundOrderNo; }
    public String getOutboundStatus() { return outboundStatus; }
    public void setOutboundStatus(String outboundStatus) { this.outboundStatus = outboundStatus; }
    public Long getAsnId() { return asnId; }
    public void setAsnId(Long asnId) { this.asnId = asnId; }
    public String getAsnNo() { return asnNo; }
    public void setAsnNo(String asnNo) { this.asnNo = asnNo; }
    public String getInboundStatus() { return inboundStatus; }
    public void setInboundStatus(String inboundStatus) { this.inboundStatus = inboundStatus; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public List<PurchaseOrderItemDto> getItems() { return items; }
    public void setItems(List<PurchaseOrderItemDto> items) { this.items = items; }
}
