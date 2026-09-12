package com.hxcoe.plm.dto;

import lombok.Data;

@Data
public class GanttLinkDTO {
    private Long id;
    private Long source;
    private Long target;
    private String type;
}

