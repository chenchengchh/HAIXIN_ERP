package com.hxcoe.erp.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 璐㈠姟瀹炰綋锟? */
@Entity
@Table(name = "erp_finance")
public class FinanceEntity {

    /**
     * 鏃犳柟娉曟瀯閫犳暟锟?     */
    public FinanceEntity() {
    }

    /**
     * 鍏ㄩ鏂规硶鏋勫缓锟?     */
    public FinanceEntity(Long id, String financeNo, Integer transactionType, BigDecimal amount, String currency, BigDecimal taxRate, LocalDateTime transactionDate, String description, Integer status, LocalDateTime createdTime, LocalDateTime updatedTime, String createdBy, String updatedBy, Integer isDeleted) {
        this.id = id;
        this.financeNo = financeNo;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.taxRate = taxRate;
        this.transactionDate = transactionDate;
        this.description = description;
        this.status = status;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.isDeleted = isDeleted;
    }

    /**
     * 涓婚敭ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 璐㈠姟鍗曞彿
     */
    @Column(name = "finance_no", unique = true, nullable = false, length = 32)
    private String financeNo;

    /**
     * 浜ゆ槗绫诲瀷锟?-鏀跺叆锟?-鏀嚭
     */
    @Column(name = "transaction_type", nullable = false)
    private Integer transactionType;

    /**
     * 閲戦
     */
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    /**
     * 甯佺
     */
    @Column(name = "currency", nullable = false, length = 8)
    private String currency;

    /**
     * 绋庣巼
     */
    @Column(name = "tax_rate", precision = 5, scale = 4)
    private BigDecimal taxRate;

    /**
     * 浜ゆ槗鏃ユ湡
     */
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    /**
     * 鎻忚堪
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * 鐘舵€侊細1-寰呭鐞嗭紝2-宸插鐞嗭紝3-宸插畬锟?     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 鍒涘缓鏃堕棿
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 鏇存柊鏃堕棿
     */
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    /**
     * 鍒涘缓锟?     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /**
     * 鏇存柊锟?     */
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    /**
     * 閫昏緫鍒犻櫎鏍囪瘑锟?-鏈垹闄わ紝1-宸插垹锟?     */
    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted;

    /**
     * 获取主键ID
     * @return 主键ID
     */
    public Long getId() {
        return id;
    }

    /**
     * 设置主键ID
     * @param id 主键ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * 获取财务单号
     * @return 财务单号
     */
    public String getFinanceNo() {
        return financeNo;
    }

    /**
     * 设置财务单号
     * @param financeNo 财务单号
     */
    public void setFinanceNo(String financeNo) {
        this.financeNo = financeNo;
    }

    /**
     * 获取交易类型
     * @return 交易类型
     */
    public Integer getTransactionType() {
        return transactionType;
    }

    /**
     * 设置交易类型
     * @param transactionType 交易类型
     */
    public void setTransactionType(Integer transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * 获取金额
     * @return 金额
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * 设置金额
     * @param amount 金额
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * 获取币种
     * @return 币种
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * 设置币种
     * @param currency 币种
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * 获取税率
     * @return 税率
     */
    public BigDecimal getTaxRate() {
        return taxRate;
    }

    /**
     * 设置税率
     * @param taxRate 税率
     */
    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    /**
     * 获取交易日期
     * @return 交易日期
     */
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    /**
     * 设置交易日期
     * @param transactionDate 交易日期
     */
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    /**
     * 获取描述
     * @return 描述
     */
    public String getDescription() {
        return description;
    }

    /**
     * 设置描述
     * @param description 描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取状态
     * @return 状态
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * 设置状态
     * @param status 状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }

    /**
     * 获取创建时间
     * @return 创建时间
     */
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    /**
     * 设置创建时间
     * @param createdTime 创建时间
     */
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * 获取更新时间
     * @return 更新时间
     */
    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    /**
     * 设置更新时间
     * @param updatedTime 更新时间
     */
    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    /**
     * 获取创建者
     * @return 创建者
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * 设置创建者
     * @param createdBy 创建者
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * 获取更新者
     * @return 更新者
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * 设置更新者
     * @param updatedBy 更新者
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    /**
     * 获取是否删除
     * @return 是否删除
     */
    public Integer getIsDeleted() {
        return isDeleted;
    }

    /**
     * 设置是否删除
     * @param isDeleted 是否删除
     */
    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}

