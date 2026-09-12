package com.hxcoe.scrm.entity.douyin;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scrm_task_log")
public class TaskLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;
    private String logType; // info, success, warning, error
    private String message;
    private LocalDateTime createTime;
}
