package com.hxcoe.les.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "les_sign_voucher")
@Data
public class LesSignVoucherEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id")
    private Long planId;

    /** ERP 关联订单号（业务键，用于签收回流 ERP） */
    @Column(name = "erp_order_no", length = 32)
    private String erpOrderNo;

    /** CRM 关联订单号（业务键，用于签收回流 CRM） */
    @Column(name = "crm_order_no", length = 32)
    private String crmOrderNo;

    /** ERP 回写状态（PENDING/SENT/FAILED） */
    @Column(name = "erp_sync_status", length = 20)
    private String erpSyncStatus;

    /** CRM 回写状态（PENDING/SENT/FAILED） */
    @Column(name = "crm_sync_status", length = 20)
    private String crmSyncStatus;

    @Column(name = "customer_sign", length = 200)
    private String customerSign;

    @Column(name = "photo_urls", columnDefinition = "TEXT")
    private String photoUrls;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "on_time_status")
    private Integer onTimeStatus;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}

