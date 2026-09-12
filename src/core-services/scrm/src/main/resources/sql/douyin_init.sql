-- 任务表
CREATE TABLE `scrm_douyin_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '任务名称',
  `status` varchar(20) NOT NULL DEFAULT 'stopped' COMMENT '状态: running/stopped/completed/error',
  `search_keyword` varchar(200) NOT NULL COMMENT '搜索关键词',
  `intent_keywords` text COMMENT '意向词(JSON数组)',
  `exclude_keywords` text COMMENT '排除词(JSON数组)',
  `message_template` text COMMENT '私信模板',
  `max_message_count` int(11) DEFAULT 10 COMMENT '最大发送量',
  `last_run_time` datetime DEFAULT NULL COMMENT '上次运行时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抖音采集任务表';

-- 采集视频表
CREATE TABLE `scrm_douyin_video` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(20) NOT NULL COMMENT '关联任务ID',
  `title` varchar(255) NOT NULL COMMENT '视频标题',
  `author` varchar(100) DEFAULT NULL COMMENT '作者昵称',
  `video_url` varchar(500) NOT NULL COMMENT '视频链接',
  `crawled_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抖音采集视频表';

-- 意向客户表
CREATE TABLE `scrm_douyin_customer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(20) NOT NULL COMMENT '关联任务ID',
  `nickname` varchar(100) NOT NULL COMMENT '用户昵称',
  `comment_content` text COMMENT '评论内容',
  `match_keyword` varchar(50) DEFAULT NULL COMMENT '命中的意向词',
  `status` varchar(20) DEFAULT 'pending' COMMENT '状态: pending/sent/failed',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抖音意向客户表';

-- 执行日志表
CREATE TABLE `scrm_task_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `task_id` bigint(20) NOT NULL COMMENT '关联任务ID',
  `log_type` varchar(20) NOT NULL COMMENT '类型: info/success/warning/error',
  `message` text NOT NULL COMMENT '日志内容',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行日志表';
