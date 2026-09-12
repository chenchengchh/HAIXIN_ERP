package com.hxcoe.erp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 供应商实体类
 */
@Entity
@Table(name = "erp_supplier")
public class SupplierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supplier_code", unique = true, nullable = false, length = 32)
    private String supplierCode;

    @Column(name = "supplier_name", nullable = false, length = 128)
    private String supplierName;

    @Column(name = "supplier_type", length = 32)
    private String supplierType;

    @Column(name = "rating", length = 8)
    private String rating;

    @Column(name = "contact_person", length = 64)
    private String contactPerson;

    @Column(name = "contact_phone", length = 32)
    private String contactPhone;

    @Column(name = "email", length = 128)
    private String email;

    @Column(name = "fax", length = 64)
    private String fax;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "payment_method", length = 64)
    private String paymentMethod;

    @Column(name = "bank_account", length = 64)
    private String bankAccount;

    @Column(name = "bank_name", length = 128)
    private String bankName;

    @Column(name = "status", nullable = false)
    private Integer status; // 0-inactive, 1-active

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", nullable = false)
    private LocalDateTime updatedTime;

    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted;

    public SupplierEntity() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSupplierCode() { return supplierCode; }
    public void setSupplierCode(String supplierCode) { this.supplierCode = supplierCode; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getSupplierType() { return supplierType; }
    public void setSupplierType(String supplierType) { this.supplierType = supplierType; }
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFax() { return fax; }
    public void setFax(String fax) { this.fax = fax; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getBankAccount() { return bankAccount; }
    public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
