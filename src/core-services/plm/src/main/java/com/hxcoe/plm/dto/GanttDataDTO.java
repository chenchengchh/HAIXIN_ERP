package com.hxcoe.plm.dto;

import lombok.Data;

import java.util.List;

@Data
public class GanttDataDTO {
    private List<GanttTaskDTO> data;
    private List<GanttLinkDTO> links;
}

