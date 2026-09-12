package com.hxcoe.plm.dto;

import lombok.Data;

@Data
public class ProjectDTO {
    private String id;
    private String name;
    private String code;
    private String status;
    private Integer progress;
    private String startDate;
    private String endDate;
    private String manager;
    private String type;
}

