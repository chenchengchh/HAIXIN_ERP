package com.hxcoe.erp.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 鍑瘉瀹炰綋锟? */
@Entity
@Table(name = "erp_voucher")
public class VoucherEntity {

    /**
     * 鏃犳柟娉曟瀯閫犳暟锟?     */
    public VoucherEntity() {
    }

    /**
     * 鍏ㄩ鏂规硶鏋勫缓锟?     */
    public VoucherEntity(Long id, String voucherNo, LocalDateTime voucherDate, String voucherType, String summary, String status, BigDecimal debitTotal, BigDecimal creditTotal, Integer attachmentCount, LocalDateTime createdTime, LocalDateTime updatedTime, String createdBy, String updatedBy, Integer isDeleted, List<VoucherItemEntity> items) {
        this.id = id;
        this.voucherNo = voucherNo;
        this.voucherDate = voucherDate;
        this.voucherType = voucherType;
        this.summary = summary;
        this.status = status;
        this.debitTotal = debitTotal;
        this.creditTotal = creditTotal;
        this.attachmentCount = attachmentCount;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.isDeleted = isDeleted;
        this.items = items;
    }

    /**
     * 涓婚敭ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 鍑瘉缂栧彿
     */
    @Column(name = "voucher_no", unique = true, nullable = false, length = 32)
    private String voucherNo;

    /**
     * 鍑瘉鏃ユ湡
     */
    @Column(name = "voucher_date", nullable = false)
    private LocalDateTime voucherDate;

    /**
     * 鍑瘉绫诲瀷锛歫ournal-璁拌处鍑瘉锛宺eceipt-鏀舵鍑瘉锛宲ayment-浠樻鍑瘉锛宼ransfer-杞处鍑瘉
     */
    @Column(name = "voucher_type", nullable = false, length = 16)
    private String voucherType;

    /**
     * 鎽樿
     */
    @Column(name = "summary", length = 255)
    private String summary;

    /**
     * 鐘舵€侊細draft-鑽夌锛宻ubmitted-宸叉彁浜わ紝approved-宸插鏍革紝posted-宸茶繃璐︼紝rejected-宸叉嫆锟?     */
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    /**
     * 鍊熸柟鍚堣
     */
    @Column(name = "debit_total", nullable = false, precision = 18, scale = 2)
    private java.math.BigDecimal debitTotal;

    /**
     * 璐锋柟鍚堣
     */
    @Column(name = "credit_total", nullable = false, precision = 18, scale = 2)
    private java.math.BigDecimal creditTotal;

    /**
     * 闄勪欢鏁伴噺
     */
    @Column(name = "attachment_count")
    private Integer attachmentCount;

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
     * 鍑瘉鍒嗗綍鍒楄〃
     */
    @OneToMany(mappedBy = "voucher", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<VoucherItemEntity> items;

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
     * 获取凭证编号
     * @return 凭证编号
     */
    public String getVoucherNo() {
        return voucherNo;
    }

    /**
     * 设置凭证编号
     * @param voucherNo 凭证编号
     */
    public void setVoucherNo(String voucherNo) {
        this.voucherNo = voucherNo;
    }

    /**
     * 获取凭证日期
     * @return 凭证日期
     */
    public LocalDateTime getVoucherDate() {
        return voucherDate;
    }

    /**
     * 设置凭证日期
     * @param voucherDate 凭证日期
     */
    public void setVoucherDate(LocalDateTime voucherDate) {
        this.voucherDate = voucherDate;
    }

    /**
     * 获取凭证类型
     * @return 凭证类型
     */
    public String getVoucherType() {
        return voucherType;
    }

    /**
     * 设置凭证类型
     * @param voucherType 凭证类型
     */
    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    /**
     * 获取摘要
     * @return 摘要
     */
    public String getSummary() {
        return summary;
    }

    /**
     * 设置摘要
     * @param summary 摘要
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * 获取状态
     * @return 状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态
     * @param status 状态
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * 获取借方合计
     * @return 借方合计
     */
    public BigDecimal getDebitTotal() {
        return debitTotal;
    }

    /**
     * 设置借方合计
     * @param debitTotal 借方合计
     */
    public void setDebitTotal(BigDecimal debitTotal) {
        this.debitTotal = debitTotal;
    }

    /**
     * 获取贷方合计
     * @return 贷方合计
     */
    public BigDecimal getCreditTotal() {
        return creditTotal;
    }

    /**
     * 设置贷方合计
     * @param creditTotal 贷方合计
     */
    public void setCreditTotal(BigDecimal creditTotal) {
        this.creditTotal = creditTotal;
    }

    /**
     * 获取附件数量
     * @return 附件数量
     */
    public Integer getAttachmentCount() {
        return attachmentCount;
    }

    /**
     * 设置附件数量
     * @param attachmentCount 附件数量
     */
    public void setAttachmentCount(Integer attachmentCount) {
        this.attachmentCount = attachmentCount;
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

    /**
     * 获取凭证分录列表
     * @return 凭证分录列表
     */
    public List<VoucherItemEntity> getItems() {
        return items;
    }

    /**
     * 设置凭证分录列表
     * @param items 凭证分录列表
     */
    public void setItems(List<VoucherItemEntity> items) {
        this.items = items;
    }
}

