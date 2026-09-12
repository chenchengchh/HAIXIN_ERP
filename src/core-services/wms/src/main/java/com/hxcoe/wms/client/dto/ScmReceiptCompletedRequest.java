package com.hxcoe.wms.client.dto;

import java.math.BigDecimal;
import java.util.List;

public class ScmReceiptCompletedRequest {
    private String poNo;
    private String receiptNo;
    private List<ScmReceiptLine> lines;

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

    public List<ScmReceiptLine> getLines() {
        return lines;
    }

    public void setLines(List<ScmReceiptLine> lines) {
        this.lines = lines;
    }

    public static class ScmReceiptLine {
        private String materialCode;
        private BigDecimal qty;

        public String getMaterialCode() {
            return materialCode;
        }

        public void setMaterialCode(String materialCode) {
            this.materialCode = materialCode;
        }

        public BigDecimal getQty() {
            return qty;
        }

        public void setQty(BigDecimal qty) {
            this.qty = qty;
        }
    }
}

