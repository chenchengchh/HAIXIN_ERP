package com.hxcoe.srm.entity;

import lombok.Data;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "srm_quotation_item")
public class QuotationItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quotation_id")
    @JsonIgnore
    private QuotationEntity quotation;

    private Long inquiryItemId;
    
    private BigDecimal price;
    private BigDecimal quantity;
    
    private LocalDateTime deliveryDate;
}
