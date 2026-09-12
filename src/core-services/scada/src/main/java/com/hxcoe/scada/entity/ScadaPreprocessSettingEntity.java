package com.hxcoe.scada.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "scada_preprocess_setting")
@Data
public class ScadaPreprocessSettingEntity {
    @Id
    private Long id;

    @Column(name = "range_conversion")
    private Boolean rangeConversion;

    @Column(name = "deadband_filter")
    private Boolean deadbandFilter;

    @Column(name = "deadband_value")
    private Double deadbandValue;

    @Column(name = "data_validation")
    private Boolean dataValidation;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}

