package com.hxcoe.mes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mes_wip_location_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WipLocationHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wip_location_id", nullable = false)
    private WipLocationEntity wipLocation;

    @Column(name = "station_id", length = 64)
    private String stationId;

    @Column(name = "station_name", length = 128)
    private String stationName;

    @Column(name = "step_id", length = 64)
    private String stepId;

    @Column(name = "step_name", length = 128)
    private String stepName;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;
}
