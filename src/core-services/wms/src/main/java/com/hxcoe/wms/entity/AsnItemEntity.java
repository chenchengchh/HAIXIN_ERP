package com.hxcoe.wms.entity;

import lombok.Data;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "wms_asn_item")
public class AsnItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asn_id")
    @JsonIgnore
    private AsnEntity asn;

    private String materialCode;
    private String materialName;
    
    @JsonAlias({"quantity"})
    private BigDecimal expectedQuantity; // 预期数量
    private BigDecimal receivedQuantity; // 实收数量
    
    private String unit;
    private String batchNo;
}
