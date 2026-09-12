package com.hxcoe.erp.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * 鍑瘉鍒嗗綍瀹炰綋锟? */
@Entity
@Table(name = "erp_voucher_item")
public class VoucherItemEntity {

    /**
     * 鏃犳柟娉曟瀯閫犳暟锟?     */
    public VoucherItemEntity() {
    }

    /**
     * 鍏ㄩ鏂规硶鏋勫缓锟?     */
    public VoucherItemEntity(Long id, VoucherEntity voucher, AccountEntity account, BigDecimal debitAmount, BigDecimal creditAmount, String itemSummary, Integer sortOrder, String remark) {
        this.id = id;
        this.voucher = voucher;
        this.account = account;
        this.debitAmount = debitAmount;
        this.creditAmount = creditAmount;
        this.itemSummary = itemSummary;
        this.sortOrder = sortOrder;
        this.remark = remark;
    }

    /**
     * 涓婚敭ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 鍑瘉ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private VoucherEntity voucher;

    /**
     * 浼氳绉戠洰鍏宠仈
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    /**
     * 鍊熸柟閲戦
     */
    @Column(name = "debit_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal debitAmount;

    /**
     * 璐锋柟閲戦
     */
    @Column(name = "credit_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal creditAmount;

    /**
     * 鎽樿
     */
    @Column(name = "item_summary", length = 255)
    private String itemSummary;

    /**
     * 鎺掑簭
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    /**
     * 澶囨敞
     */
    @Column(name = "remark", length = 255)
    private String remark;

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
     * 获取凭证
     * @return 凭证
     */
    public VoucherEntity getVoucher() {
        return voucher;
    }

    /**
     * 设置凭证
     * @param voucher 凭证
     */
    public void setVoucher(VoucherEntity voucher) {
        this.voucher = voucher;
    }

    /**
     * 获取会计科目
     * @return 会计科目
     */
    public AccountEntity getAccount() {
        return account;
    }

    /**
     * 设置会计科目
     * @param account 会计科目
     */
    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    /**
     * 获取借方金额
     * @return 借方金额
     */
    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    /**
     * 设置借方金额
     * @param debitAmount 借方金额
     */
    public void setDebitAmount(BigDecimal debitAmount) {
        this.debitAmount = debitAmount;
    }

    /**
     * 获取贷方金额
     * @return 贷方金额
     */
    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    /**
     * 设置贷方金额
     * @param creditAmount 贷方金额
     */
    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    /**
     * 获取摘要
     * @return 摘要
     */
    public String getItemSummary() {
        return itemSummary;
    }

    /**
     * 设置摘要
     * @param itemSummary 摘要
     */
    public void setItemSummary(String itemSummary) {
        this.itemSummary = itemSummary;
    }

    /**
     * 获取排序
     * @return 排序
     */
    public Integer getSortOrder() {
        return sortOrder;
    }

    /**
     * 设置排序
     * @param sortOrder 排序
     */
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * 获取备注
     * @return 备注
     */
    public String getRemark() {
        return remark;
    }

    /**
     * 设置备注
     * @param remark 备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }
}

