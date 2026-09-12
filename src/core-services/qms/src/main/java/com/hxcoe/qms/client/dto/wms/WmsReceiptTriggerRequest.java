package com.hxcoe.qms.client.dto.wms;

import java.math.BigDecimal;
import java.util.List;

/**
 * WMS 收货完成 → QMS 触发 IQC 请求体（QMS 消费端视角）。
 * <p>字段与 {@code com.hxcoe.wms.client.dto.QmsReceiptTriggerRequest} 保持一致，
 * 独立声明以避免 QMS 反向依赖 WMS 模块。</p>
 */
public class WmsReceiptTriggerRequest {

    /** 采购单号（QMS 质检单来源单据号） */
    private String poNo;

    /** 收货单号（WMS ASN 单号） */
    private String receiptNo;

    /** 收货明细行 */
    private List<WmsReceiptLine> lines;

    public String getPoNo() {
        return poNo;
    }

    public void setPoNo(String poNo) {
        this.poNo = poNo;
    }

    public String getReceiptNo() {
        return receiptNo;
    }

    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
    }

    public List<WmsReceiptLine> getLines() {
        return lines;
    }

    public void setLines(List<WmsReceiptLine> lines) {
        this.lines = lines;
    }

    /** 收货明细行 */
    public static class WmsReceiptLine {
        /** 物料编码 */
        private String materialCode;
        /** 物料名称 */
        private String materialName;
        /** 批次号 */
        private String batchNo;
        /** 送检数量 */
        private BigDecimal qty;

        public String getMaterialCode() {
            return materialCode;
        }

        public void setMaterialCode(String materialCode) {
            this.materialCode = materialCode;
        }

        public String getMaterialName() {
            return materialName;
        }

        public void setMaterialName(String materialName) {
            this.materialName = materialName;
        }

        public String getBatchNo() {
            return batchNo;
        }

        public void setBatchNo(String batchNo) {
            this.batchNo = batchNo;
        }

        public BigDecimal getQty() {
            return qty;
        }

        public void setQty(BigDecimal qty) {
            this.qty = qty;
        }
    }
}
