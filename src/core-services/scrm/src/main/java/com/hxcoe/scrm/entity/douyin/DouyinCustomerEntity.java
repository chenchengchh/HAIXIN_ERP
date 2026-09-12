package com.hxcoe.scrm.entity.douyin;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scrm_douyin_customer")
public class DouyinCustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id")
    private Long taskId;

    private String nickname;
    
    @Column(name = "comment_content", columnDefinition = "TEXT")
    private String commentContent;
    
    @Column(name = "match_keyword")
    private String matchKeyword;
    
    private String status; // pending, sent, failed
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
}
