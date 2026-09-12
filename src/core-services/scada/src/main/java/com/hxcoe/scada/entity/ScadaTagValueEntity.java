package com.hxcoe.scada.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "scada_tag_value")
@Data
public class ScadaTagValueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_code", length = 50)
    private String tagCode;

    @Column(name = "ts")
    private LocalDateTime ts;

    @Column(name = "value")
    private Double value;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "quality", length = 20)
    private String quality;
}

