package com.hxcoe.scrm.entity.douyin;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scrm_douyin_task")
public class DouyinTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String status; // running, stopped, completed, error
    private String searchKeyword;
    
    @Column(columnDefinition = "TEXT")
    private String intentKeywords;
    
    @Column(columnDefinition = "TEXT")
    private String excludeKeywords;
    
    @Column(columnDefinition = "TEXT")
    private String messageTemplate;
    
    // 优化字段命名，添加列名映射
    @Column(name = "max_message_count")
    private Integer maxMessageCount;
    
    // 添加缺失字段：存储发送间隔，JSON格式 [min, max]
    @Column(columnDefinition = "TEXT", name = "message_interval")
    private String messageInterval;
    
    private LocalDateTime lastRunTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 添加乐观锁版本号，用于并发控制
    @Version
    private Integer version;
}
