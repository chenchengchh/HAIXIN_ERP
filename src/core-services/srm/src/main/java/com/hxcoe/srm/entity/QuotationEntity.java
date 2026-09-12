package com.hxcoe.srm.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Data
@Entity
@Table(name = "srm_quotation")
public class QuotationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long inquiryId;
    
    private Long supplierId;
    private String supplierName;
    
    private BigDecimal totalAmount;
    private String currency;
    
    private LocalDateTime quoteTime;
    
    // 是否推荐中标
    private Boolean isRecommend;
    
    // SUBMITTED, ACCEPTED, REJECTED
    private String status;
    
    @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuotationItemEntity> items;

    private LocalDateTime createdTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }
}
