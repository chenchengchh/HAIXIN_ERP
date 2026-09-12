package com.hxcoe.aibrain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AI 决策中枢服务（自动化刻度盘方案）
 * 职责：跨模块数据聚合、决策建议生成（AUTO级）、人工确认后动作回写（CONFIRM级）、反馈学习环
 * P0 仅骨架：健康检查与决策引擎包结构预留；P2 接入 Spring AI / MCP 工具层
 */
@EnableScheduling
@EnableFeignClients
@SpringBootApplication
public class AiBrainApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiBrainApplication.class, args);
    }
}
