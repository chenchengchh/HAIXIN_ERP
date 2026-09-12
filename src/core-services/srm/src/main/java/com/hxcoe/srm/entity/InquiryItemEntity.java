package com.hxcoe.srm.entity;

import lombok.Data;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "srm_inquiry_item")
public class InquiryItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id")
    @JsonIgnore
    private InquiryEntity inquiry;

    private String materialCode;
    private String materialName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal targetPrice;
}
