package com.hxcoe.mes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mes_equipment_fault")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentFaultEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_id", nullable = false, length = 64)
    private String equipmentId;

    @Column(name = "equipment_name", length = 128)
    private String equipmentName;

    @Column(name = "fault_type", length = 64)
    private String faultType;

    @Column(name = "fault_description", length = 255)
    private String faultDescription;

    @Column(name = "occur_time")
    private LocalDateTime occurTime;

    @Column(name = "repair_time")
    private LocalDateTime repairTime;

    @Column(name = "repair_person", length = 64)
    private String repairPerson;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
