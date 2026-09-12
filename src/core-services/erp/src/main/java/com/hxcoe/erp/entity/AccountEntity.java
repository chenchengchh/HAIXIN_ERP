package com.hxcoe.erp.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会计科目实体类 */
@Entity
@Table(name = "erp_account")
public class AccountEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 科目编码
     */
    @Column(name = "account_code", unique = true, nullable = false, length = 32)
    private String accountCode;

    /**
     * 科目名称
     */
    @Column(name = "account_name", nullable = false, length = 64)
    private String accountName;

    /**
     * 科目类型：asset-资产，liability-负债，equity-所有者权益，income-收入，expense-费用
     */
    @Column(name = "account_type", nullable = false, length = 16)
    private String accountType;

    /**
     * 余额方向：debit-借方，credit-贷方
     */
    @Column(name = "balance_direction", nullable = false, length = 8)
    private String balanceDirection;

    /**
     * 上级ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private AccountEntity parent;

    /**
     * 子级列表
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<AccountEntity> children;

    /**
     * 级别：1-一级科目，2-二级科目，3-三级科目，以此类推
     */
    @Column(name = "level", nullable = false)
    private Integer level;

    /**
     * 是否末级科目：0-否，1-是
     */
    @Column(name = "is_leaf", nullable = false)
    private Integer isLeaf;

    /**
     * 状态：0-停用，1-启用
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 备注
     */
    @Column(name = "remark", length = 255)
    private String remark;

    /**
     * 创建时间
     */
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted;

    // 手动添加构造方法
    public AccountEntity() {
    }

    public AccountEntity(Long id, String accountCode, String accountName, String accountType, String balanceDirection, AccountEntity parent, List<AccountEntity> children, Integer level, Integer isLeaf, Integer status, String remark, LocalDateTime createdTime, LocalDateTime updatedTime, String createdBy, String updatedBy, Integer isDeleted) {
        this.id = id;
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.accountType = accountType;
        this.balanceDirection = balanceDirection;
        this.parent = parent;
        this.children = children;
        this.level = level;
        this.isLeaf = isLeaf;
        this.status = status;
        this.remark = remark;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
        this.isDeleted = isDeleted;
    }

    // 手动添加getter和setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getBalanceDirection() {
        return balanceDirection;
    }

    public void setBalanceDirection(String balanceDirection) {
        this.balanceDirection = balanceDirection;
    }

    public AccountEntity getParent() {
        return parent;
    }

    public void setParent(AccountEntity parent) {
        this.parent = parent;
    }

    public List<AccountEntity> getChildren() {
        return children;
    }

    public void setChildren(List<AccountEntity> children) {
        this.children = children;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getIsLeaf() {
        return isLeaf;
    }

    public void setIsLeaf(Integer isLeaf) {
        this.isLeaf = isLeaf;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }
}