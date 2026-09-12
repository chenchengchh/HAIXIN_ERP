package com.hxcoe.mes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mes_flow_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlowRecordEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sn_code", nullable = false, length = 64)
    private String snCode;

    @Column(name = "batch_no", length = 64)
    private String batchNo;

    @Column(name = "from_step_id", length = 64)
    private String fromStepId;

    @Column(name = "from_step_name", length = 128)
    private String fromStepName;

    @Column(name = "to_step_id", length = 64)
    private String toStepId;

    @Column(name = "to_step_name", length = 128)
    private String toStepName;

    @Column(name = "from_station_id", length = 64)
    private String fromStationId;

    @Column(name = "from_station_name", length = 128)
    private String fromStationName;

    @Column(name = "to_station_id", length = 64)
    private String toStationId;

    @Column(name = "to_station_name", length = 128)
    private String toStationName;

    @Column(name = "operator_id", length = 64)
    private String operatorId;

    @Column(name = "operator_name", length = 64)
    private String operatorName;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "remarks", length = 255)
    private String remarks;
}
