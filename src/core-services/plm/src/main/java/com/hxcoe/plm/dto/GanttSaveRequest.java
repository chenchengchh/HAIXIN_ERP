package com.hxcoe.plm.dto;

import lombok.Data;

import java.util.List;

@Data
public class GanttSaveRequest {
    private List<GanttTaskDTO> tasks;
    private List<GanttLinkDTO> links;
}

