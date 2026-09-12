package com.hxcoe.scrm.dto.douyin;

import com.hxcoe.scrm.entity.douyin.TaskLogEntity;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class DouyinTaskDTO {
    private Long id;
    private String name;
    private String status;
    private String searchKeyword;
    
    private FilterConfigDTO filterConfig;
    private MessageConfigDTO messageConfig;
    private TaskStatsDTO stats;
    
    private LocalDateTime lastRunTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    private List<TaskLogEntity> logs;

    @Data
    public static class FilterConfigDTO {
        // 设置合理的默认值
        private List<String> intentKeywords = new ArrayList<>(List.of("多少钱", "怎么买", "求链接", "感兴趣", "价格"));
        private List<String> excludeKeywords = new ArrayList<>(List.of("互粉", "骗子"));
    }

    @Data
    public static class MessageConfigDTO {
        // 设置默认消息模板
        private String template = "你好，刚看到你在评论区问关于{产品}的问题，我是厂家直销，可以加个V详细发您资料参考下~";
        // 设置默认最大发送数量
        private Integer maxCount = 10;
        // 设置默认发送间隔
        private Integer[] interval = new Integer[]{5, 15};
    }

    @Data
    public static class TaskStatsDTO {
        private long videosFound = 0;
        private long customersFound = 0;
        private long messagesSent = 0;
    }
}
