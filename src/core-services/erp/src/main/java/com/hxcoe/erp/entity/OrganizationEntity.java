package com.hxcoe.erp.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 缁勭粐瀹炰綋锟? */
@Entity
@Table(name = "erp_organization")
public class OrganizationEntity {

    /**
     * 鏃犳柟娉曟瀯閫犳暟锟?     */
    public OrganizationEntity() {
    }

    /**
     * 鍏ㄩ鏂规硶鏋勫缓锟?     */
    public OrganizationEntity(Long id, String organizationCode, String organizationName, String organizationType, OrganizationEntity parent, List<OrganizationEntity> children, Integer level, Integer status, String remark, LocalDateTime createdTime, LocalDateTime updatedTime, String createdBy, String updatedBy, Integer isDeleted) {
        this.id = id;
        this.organizationCode = organizationCode;
        this.organizationName = organizationName;
        this.organizationType = organizationType;
        this.parent = parent;
        this.children = children;
        this.level = level;
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
     * 缁勭粐缂栫爜
     */
    @Column(name = "organization_code", unique = true, nullable = false, length = 32)
    private String organizationCode;

    /**
     * 缁勭粐鍚嶇О
     */
    @Column(name = "organization_name", nullable = false, length = 128)
    private String organizationName;

    /**
     * 缁勭粐绫诲瀷锛歝ompany-鍏徃锛宒epartment-閮ㄩ棬锛宼eam-鍥㈤槦锛宐ranch-鍒嗘敮鏈烘瀯
     */
    @Column(name = "organization_type", nullable = false, length = 16)
    private String organizationType;

    /**
     * 鐖剁粍缁嘔D
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private OrganizationEntity parent;

    /**
     * 瀛愮粍缁囧垪琛?     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<OrganizationEntity> children;

    /**
     * 绾ф锛?-涓€绾х粍缁囷紝2-浜岀骇缁勭粐锛?-涓夌骇缁勭粐锛屼互姝ょ被鎺?     */
    @Column(name = "level", nullable = false)
    private Integer level;

    /**
     * 鐘舵€侊細0-绂佺敤锛?-鍚敤
     */
    @Column(name = "status", nullable = false)
    private Integer status;

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
     * 鍒涘缓鑰?     */
    @Column(name = "created_by", length = 64)
    private String createdBy;

    /**
     * 鏇存柊鑰?     */
    @Column(name = "updated_by", length = 64)
    private String updatedBy;

    /**
     * 閫昏緫鍒犻櫎鏍囪瘑锛?-鏈垹闄わ紝1-宸插垹闄?     */
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
     * 获取组织编码
     * @return 组织编码
     */
    public String getOrganizationCode() {
        return organizationCode;
    }

    /**
     * 设置组织编码
     * @param organizationCode 组织编码
     */
    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    /**
     * 获取组织名称
     * @return 组织名称
     */
    public String getOrganizationName() {
        return organizationName;
    }

    /**
     * 设置组织名称
     * @param organizationName 组织名称
     */
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    /**
     * 获取组织类型
     * @return 组织类型
     */
    public String getOrganizationType() {
        return organizationType;
    }

    /**
     * 设置组织类型
     * @param organizationType 组织类型
     */
    public void setOrganizationType(String organizationType) {
        this.organizationType = organizationType;
    }

    /**
     * 获取父组织
     * @return 父组织
     */
    public OrganizationEntity getParent() {
        return parent;
    }

    /**
     * 设置父组织
     * @param parent 父组织
     */
    public void setParent(OrganizationEntity parent) {
        this.parent = parent;
    }

    /**
     * 获取子组织列表
     * @return 子组织列表
     */
    public List<OrganizationEntity> getChildren() {
        return children;
    }

    /**
     * 设置子组织列表
     * @param children 子组织列表
     */
    public void setChildren(List<OrganizationEntity> children) {
        this.children = children;
    }

    /**
     * 获取层级
     * @return 层级
     */
    public Integer getLevel() {
        return level;
    }

    /**
     * 设置层级
     * @param level 层级
     */
    public void setLevel(Integer level) {
        this.level = level;
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

