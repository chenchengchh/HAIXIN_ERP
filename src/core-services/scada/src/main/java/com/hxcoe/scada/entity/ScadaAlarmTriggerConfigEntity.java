package com.hxcoe.scada.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "scada_alarm_trigger_config")
@Data
public class ScadaAlarmTriggerConfigEntity {
    @Id
    private Long id;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}

