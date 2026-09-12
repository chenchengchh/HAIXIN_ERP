package com.hxcoe.erp.model;

import java.math.BigDecimal;

/**
 * 总账数据传输对象
 */
public class GeneralLedgerDTO {
    private String accountCode;
    private String accountName;
    private String accountType;
    private BigDecimal beginningBalance;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private BigDecimal endingBalance;
    private String status;

    public GeneralLedgerDTO() {
    }

    public GeneralLedgerDTO(String accountCode, String accountName, String accountType, BigDecimal beginningBalance, BigDecimal debitAmount, BigDecimal creditAmount, BigDecimal endingBalance, String status) {
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.accountType = accountType;
        this.beginningBalance = beginningBalance;
        this.debitAmount = debitAmount;
        this.creditAmount = creditAmount;
        this.endingBalance = endingBalance;
        this.status = status;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public BigDecimal getBeginningBalance() {
        return beginningBalance;
    }

    public void setBeginningBalance(BigDecimal beginningBalance) {
        this.beginningBalance = beginningBalance;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(BigDecimal debitAmount) {
        this.debitAmount = debitAmount;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public BigDecimal getEndingBalance() {
        return endingBalance;
    }

    public void setEndingBalance(BigDecimal endingBalance) {
        this.endingBalance = endingBalance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
