package com.hxcoe.les.dto;

import lombok.Data;

@Data
public class LesVehicleLocationDto {

    private Long vehicleId;
    private String licensePlate;
    private Double longitude;
    private Double latitude;
    private Double speed;
    private Double direction;
    private String timestamp;
}

