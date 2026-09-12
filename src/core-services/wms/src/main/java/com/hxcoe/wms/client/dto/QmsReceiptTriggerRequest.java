package com.hxcoe.wms.client.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * WMS 收货完成 → QMS 触发 IQC 质检单 请求体。
 * <p>用于 WMS 收货后通知 QMS 创建来料检验单（IQC），完成质量闭环的入口段。</p>
 */
public class QmsReceiptTriggerRequest {

    /** 采购单号（QMS 质检单的来源单据号 sourceNo） */
    private String poNo;

    /** 收货单号（WMS ASN 单号，便于追溯） */
    private String receiptNo;

    /** 收货明细行（按物料维度汇总送检数量） */
    private List<QmsReceiptLine> lines;

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

    public List<QmsReceiptLine> getLines() {
        return lines;
    }

    public void setLines(List<QmsReceiptLine> lines) {
        this.lines = lines;
    }

    /**
     * 收货明细行：一个物料一条送检记录。
     */
    public static class QmsReceiptLine {
        /** 物料编码（映射到 QMS 质检单 productCode） */
        private String materialCode;
        /** 物料名称（映射到 QMS 质检单 productName） */
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
