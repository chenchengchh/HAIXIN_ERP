package com.hxcoe.srm.entity;

import lombok.Data;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "srm_delivery_note_item")
public class DeliveryNoteItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_note_id")
    @JsonIgnore
    private DeliveryNoteEntity deliveryNote;

    private String purchaseOrderNo;
    private String purchaseOrderItemId;
    
    private String materialCode;
    private String materialName;
    
    private BigDecimal quantity;
    private String unit;
    
    private String batchNo;
}
