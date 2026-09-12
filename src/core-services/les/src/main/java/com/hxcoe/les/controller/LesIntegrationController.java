package com.hxcoe.les.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.les.entity.LesTransportTaskEntity;
import com.hxcoe.les.repository.LesTransportTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * LES集成接收端点：接收AGV等外部系统的联动事件回传
 */
@RestController
@RequestMapping({"/api/v1/les/integration", "/les/integration"})
public class LesIntegrationController {

    private static final Logger log = LoggerFactory.getLogger(LesIntegrationController.class);

    @Autowired
    private LesTransportTaskRepository taskRepository;

    /**
     * 接收AGV任务状态回传（AGV→LES联动）。
     * agvTaskId 即LES运输任务的taskNo（LES下发时以taskNo作为AGV任务标识）。
     *
     * @param body 状态事件：{"eventId":"uuid","agvTaskId":"LES任务taskNo","status":"completed","eventTime":"..."}
     * @return 处理结果，携带 taskNo/oldStatus/newStatus
     */
    @PostMapping("/agv/task-status")
    public Result<Map<String, Object>> receiveAgvTaskStatus(@RequestBody Map<String, Object> body) {
        String agvTaskId = body == null ? null : String.valueOf(body.getOrDefault("agvTaskId", ""));
        String status = body == null ? null : String.valueOf(body.getOrDefault("status", ""));
        if (agvTaskId == null || agvTaskId.isBlank()) {
            return Result.fail("agvTaskId不能为空");
        }
        if (status == null || status.isBlank()) {
            return Result.fail("status不能为空");
        }

        LesTransportTaskEntity task = taskRepository.findByTaskNo(agvTaskId.trim());
        if (task == null) {
            log.warn("AGV状态回传未匹配到运输任务：agvTaskId={}", agvTaskId);
            return Result.fail("运输任务不存在");
        }

        String oldStatus = task.getStatus();
        String newStatus = mapAgvStatusToLes(status.trim());
        task.setStatus(newStatus);
        if ("completed".equals(newStatus) && task.getEndTime() == null) {
            task.setEndTime(LocalDateTime.now());
        }
        if ("in_transit".equals(newStatus) && task.getStartTime() == null) {
            task.setStartTime(LocalDateTime.now());
        }
        taskRepository.save(task);

        Map<String, Object> data = new HashMap<>();
        data.put("taskNo", task.getTaskNo());
        data.put("oldStatus", oldStatus);
        data.put("newStatus", newStatus);
        return Result.success("AGV任务状态回传处理成功", data);
    }

    /**
     * AGV任务状态到LES运输任务状态的映射
     *
     * @param agvStatus AGV任务状态
     * @return LES运输任务状态
     */
    private static String mapAgvStatusToLes(String agvStatus) {
        if (agvStatus == null) {
            return null;
        }
        return switch (agvStatus.trim().toLowerCase()) {
            case "completed", "done" -> "completed";
            case "running", "assigned" -> "in_transit";
            case "cancelled" -> "cancelled";
            default -> agvStatus;
        };
    }
}
