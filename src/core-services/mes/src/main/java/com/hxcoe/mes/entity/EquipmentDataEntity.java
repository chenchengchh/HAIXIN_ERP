package com.hxcoe.mes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mes_equipment_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id", length = 64)
    private String equipmentId;

    @Column(name = "equipment_name", length = 128)
    private String equipmentName;

    @Column(name = "equipment_type", length = 64)
    private String equipmentType;

    @Column(name = "parameter_name", length = 64)
    private String parameterName;

    @Column(name = "parameter_value")
    private BigDecimal parameterValue;

    @Column(name = "unit", length = 16)
    private String unit;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "status", length = 32)
    private String status;
}
