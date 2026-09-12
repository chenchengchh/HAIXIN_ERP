package com.hxcoe.les.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "agv-service")
public interface AgvClient {

    /**
     * 创建AGV任务（用于LES调度结果联动演示）
     *
     * @param body 任务参数
     * @return 创建结果
     */
    @PostMapping("/agv/tasks")
    Result<Map<String, Object>> createTask(@RequestBody Map<String, Object> body);
}

