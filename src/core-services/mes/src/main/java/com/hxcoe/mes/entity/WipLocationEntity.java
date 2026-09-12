package com.hxcoe.mes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mes_wip_location")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WipLocationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sn_code", unique = true, nullable = false, length = 64)
    private String snCode;

    @Column(name = "work_order_no", length = 64)
    private String workOrderNo;

    @Column(name = "current_step_id", length = 64)
    private String currentStepId;

    @Column(name = "current_step_name", length = 128)
    private String currentStepName;

    @Column(name = "current_station_id", length = 64)
    private String currentStationId;

    @Column(name = "current_station_name", length = 128)
    private String currentStationName;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @OneToMany(mappedBy = "wipLocation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("startTime ASC")
    private List<WipLocationHistoryEntity> locationHistory = new ArrayList<>();
}
