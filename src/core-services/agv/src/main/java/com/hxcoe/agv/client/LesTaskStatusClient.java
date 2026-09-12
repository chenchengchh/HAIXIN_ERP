package com.hxcoe.agv.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * LES任务状态回传客户端（AGV→LES联动）
 */
@FeignClient(name = "les-service", path = "/api/v1/les/integration", contextId = "agvLesTaskStatusClient")
public interface LesTaskStatusClient {

    /**
     * 回传AGV任务状态变更事件到LES
     *
     * @param body 状态事件参数（eventId/agvTaskId/status/eventTime）
     * @return 处理结果
     */
    @PostMapping("/agv/task-status")
    Result<Map<String, Object>> reportTaskStatus(@RequestBody Map<String, Object> body);
}
