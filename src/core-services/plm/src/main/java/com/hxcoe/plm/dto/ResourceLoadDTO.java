package com.hxcoe.plm.dto;

import lombok.Data;

@Data
public class ResourceLoadDTO {
    private String resourceId;
    private String resourceName;
    private Integer load;
    private String date;
}

