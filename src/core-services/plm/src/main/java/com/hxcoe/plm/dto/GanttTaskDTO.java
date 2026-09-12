package com.hxcoe.plm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GanttTaskDTO {
    private Long id;
    private String text;

    @JsonProperty("start_date")
    private String startDate;

    private Integer duration;
    private Integer progress;
    private Long parent;
    private String type;

    /** 任务负责人 */
    private String assignee;

    /** 任务状态（pending/in-progress/completed） */
    private String status;

    /** 任务结束日期（yyyy-MM-dd） */
    @JsonProperty("end_date")
    private String endDate;
}

