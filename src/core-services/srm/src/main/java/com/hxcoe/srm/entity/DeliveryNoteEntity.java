package com.hxcoe.srm.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "srm_delivery_note")
public class DeliveryNoteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_note_no", unique = true, nullable = false)
    private String deliveryNoteNo;
    
    private String purchaseOrderNo;
    
    private Long supplierId;
    private String supplierName;
    
    private String logisticsCompany;
    private String trackingNumber;
    
    private LocalDateTime deliveryDate;
    private String status; // SHIPPED, RECEIVED

    @OneToMany(mappedBy = "deliveryNote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DeliveryNoteItemEntity> items;

    private LocalDateTime createdTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }
}
