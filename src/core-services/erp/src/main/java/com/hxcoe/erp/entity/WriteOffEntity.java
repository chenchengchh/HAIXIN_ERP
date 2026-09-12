package com.hxcoe.erp.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 鏍搁攢瀹炰綋锟? */
@Entity
@Table(name = "erp_write_off")
public class WriteOffEntity {

    /**
     * 鏃犳柟娉曟瀯閫犳暟锟?     */
    public WriteOffEntity() {
    }

    /**
     * 鍏ㄩ鏂规硶鏋勫缓锟?     */
    public WriteOffEntity(Long id, String writeOffNo, String originalDocType, Long originalDocId, String writeOffType, BigDecimal writeOffAmount, LocalDateTime writeOffDate, String status, String remark, LocalDateTime createdTime, LocalDateTime updatedTime, String createdBy, String updatedBy, Integer isDeleted) {
        this.id = id;
        this.writeOffNo = writeOffNo;
        this.originalDocType = originalDocType;
        this.originalDocId = originalDocId;
        this.writeOffType = writeOffType;
        this.writeOffAmount = writeOffAmount;
        this.writeOffDate = writeOffDate;
        this.status = status;
        this.remark = remark;
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
     * 鏍搁攢鍗曞彿
     */
    @Column(name = "write_off_no", unique = true, nullable = false, length = 32)
    private String writeOffNo;

    /**
     * 鍘熷崟鎹被鍨嬶細invoice-鍙戠エ锛宺eceivable-搴旀敹鍗曪紝payable-搴斾粯锟?     */
    @Column(name = "original_doc_type", nullable = false, length = 16)
    private String originalDocType;

    /**
     * 鍘熷崟鎹甀D
     */
    @Column(name = "original_doc_id", nullable = false)
    private Long originalDocId;

    /**
     * 鏍搁攢绫诲瀷锛歱artial-閮ㄥ垎鏍搁攢锛宖ull-鍏ㄩ鏍搁攢
     */
    @Column(name = "write_off_type", nullable = false, length = 16)
    private String writeOffType;

    /**
     * 鏍搁攢閲戦
     */
    @Column(name = "write_off_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal writeOffAmount;

    /**
     * 鏍搁攢鏃ユ湡
     */
    @Column(name = "write_off_date", nullable = false)
    private LocalDateTime writeOffDate;

    /**
     * 鐘舵€侊細pending-寰呭鐞嗭紝completed-宸插畬锟?     */
    @Column(name = "status", nullable = false, length = 16)
    private String status;

    /**
     * 澶囨敞
     */
    @Column(name = "remark", length = 255)
    private String remark;

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
     * 获取核销单号
     * @return 核销单号
     */
    public String getWriteOffNo() {
        return writeOffNo;
    }

    /**
     * 设置核销单号
     * @param writeOffNo 核销单号
     */
    public void setWriteOffNo(String writeOffNo) {
        this.writeOffNo = writeOffNo;
    }

    /**
     * 获取原单类型
     * @return 原单类型
     */
    public String getOriginalDocType() {
        return originalDocType;
    }

    /**
     * 设置原单类型
     * @param originalDocType 原单类型
     */
    public void setOriginalDocType(String originalDocType) {
        this.originalDocType = originalDocType;
    }

    /**
     * 获取原单ID
     * @return 原单ID
     */
    public Long getOriginalDocId() {
        return originalDocId;
    }

    /**
     * 设置原单ID
     * @param originalDocId 原单ID
     */
    public void setOriginalDocId(Long originalDocId) {
        this.originalDocId = originalDocId;
    }

    /**
     * 获取核销类型
     * @return 核销类型
     */
    public String getWriteOffType() {
        return writeOffType;
    }

    /**
     * 设置核销类型
     * @param writeOffType 核销类型
     */
    public void setWriteOffType(String writeOffType) {
        this.writeOffType = writeOffType;
    }

    /**
     * 获取核销金额
     * @return 核销金额
     */
    public BigDecimal getWriteOffAmount() {
        return writeOffAmount;
    }

    /**
     * 设置核销金额
     * @param writeOffAmount 核销金额
     */
    public void setWriteOffAmount(BigDecimal writeOffAmount) {
        this.writeOffAmount = writeOffAmount;
    }

    /**
     * 获取核销日期
     * @return 核销日期
     */
    public LocalDateTime getWriteOffDate() {
        return writeOffDate;
    }

    /**
     * 设置核销日期
     * @param writeOffDate 核销日期
     */
    public void setWriteOffDate(LocalDateTime writeOffDate) {
        this.writeOffDate = writeOffDate;
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

