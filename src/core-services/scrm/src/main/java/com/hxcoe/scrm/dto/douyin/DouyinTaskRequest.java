package com.hxcoe.scrm.dto.douyin;

import lombok.Data;

import java.util.List;

/**
 * 抖音任务请求DTO，用于接收前端发送的嵌套结构数据
 */
@Data
public class DouyinTaskRequest {
    private String name;
    private String searchKeyword;
    private FilterConfigRequest filterConfig;
    private MessageConfigRequest messageConfig;

    @Data
    public static class FilterConfigRequest {
        private List<String> intentKeywords;
        private List<String> excludeKeywords;
    }

    @Data
    public static class MessageConfigRequest {
        private String template;
        private Integer maxCount;
        private Integer[] interval;
    }
}
