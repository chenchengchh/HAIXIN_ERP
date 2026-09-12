package com.hxcoe.wms.entity;

import lombok.Data;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "wms_asn")
public class AsnEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asn_no", unique = true, nullable = false)
    private String asnNo; // ASN编号

    @JsonAlias({"refOrderNo", "poNo"})
    private String deliveryNoteNo; // 关联送货单号
    
    private String supplierCode;
    private String supplierName;

    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    
    // CREATED, RECEIVED, PARTIAL_RECEIVED, CLOSED
    private String status;
    
    @JsonAlias({"expectedArrivalTime"})
    private LocalDateTime expectedArrivalDate;

    @JsonAlias({"actualArrivalTime"})
    private LocalDateTime actualArrivalDate;

    @OneToMany(mappedBy = "asn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AsnItemEntity> items;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        updatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}
