package com.hxcoe.scrm.entity.douyin;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scrm_douyin_video")
public class DouyinVideoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id")
    private Long taskId;

    private String title;
    private String author;
    
    @Column(name = "video_url")
    private String videoUrl;
    
    @Column(name = "crawled_time")
    private LocalDateTime crawledTime;
}
