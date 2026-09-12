package com.hxcoe.agv.controller;

import com.hxcoe.agv.client.LesTaskStatusClient;
import com.hxcoe.agv.entity.*;
import com.hxcoe.agv.repository.*;
import com.hxcoe.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping({"/agv", "/api/v1/agv"})
public class AgvApiController {

    private static final Logger log = LoggerFactory.getLogger(AgvApiController.class);

    @Autowired
    private AgvDeviceRepository deviceRepository;

    @Autowired
    private AgvTaskRepository taskRepository;

    @Autowired
    private AgvTaskAssignmentRepository taskAssignmentRepository;

    @Autowired
    private AgvPathPlanRepository pathPlanRepository;

    @Autowired
    private AgvPathPointRepository pathPointRepository;

    @Autowired
    private AgvTrafficNodeRepository trafficNodeRepository;

    @Autowired
    private AgvTrafficLockRepository trafficLockRepository;

    @Autowired
    private AgvTrafficRuleRepository trafficRuleRepository;

    @Autowired
    private AgvFaultAlertRepository faultAlertRepository;

    @Autowired
    private AgvOperationLogRepository operationLogRepository;

    @Autowired
    private AgvCollaborationStrategyRepository collaborationStrategyRepository;

    @Autowired
    private AgvCollisionAvoidanceConfigRepository collisionAvoidanceConfigRepository;

    @Autowired
    private AgvCollaborationLogRepository collaborationLogRepository;

    @Autowired
    private AgvAreaRepository areaRepository;

    @Autowired(required = false)
    private LesTaskStatusClient lesTaskStatusClient;

    /**
     * 获取 AGV 列表
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getAgvs() {
        List<AgvDeviceEntity> devices = deviceRepository.findAll();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AgvDeviceEntity d : devices) {
            rows.add(toAgv(d));
        }
        return Result.success("查询成功", rows);
    }

    /**
     * 获取 AGV 设备分页列表（前端/devices路径兼容）
     */
    @GetMapping("/devices")
    public Result<Map<String, Object>> getAgvDevices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<AgvDeviceEntity> devices = deviceRepository.findAll();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AgvDeviceEntity d : devices) {
            rows.add(toAgv(d));
        }
        // 手动分页
        int total = rows.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        List<Map<String, Object>> pagedRows = rows.subList(from, to);

        Map<String, Object> result = new HashMap<>();
        result.put("list", pagedRows);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return Result.success("查询成功", result);
    }

    /**
     * 获取 AGV 详情
     */
    @GetMapping("/{agvCode}")
    public Result<Map<String, Object>> getAgvDetail(@PathVariable String agvCode) {
        AgvDeviceEntity d = deviceRepository.findByCode(agvCode);
        if (d == null) {
            return Result.fail("AGV不存在");
        }
        return Result.success("查询成功", toAgv(d));
    }

    /**
     * 更新 AGV 状态
     */
    @PutMapping("/{agvCode}/status")
    public Result<Map<String, Object>> updateAgvStatus(@PathVariable String agvCode, @RequestBody Map<String, Object> body) {
        AgvDeviceEntity d = deviceRepository.findByCode(agvCode);
        if (d == null) {
            return Result.fail("AGV不存在");
        }
        String status = body == null ? null : String.valueOf(body.getOrDefault("status", ""));
        if (status == null || status.isBlank()) {
            return Result.fail("status不能为空");
        }
        d.setStatus(status.trim());
        AgvDeviceEntity saved = deviceRepository.save(d);
        writeOpLog("agv_status", agvCode, agvCode, "更新状态为：" + status, "system");
        return Result.success("更新成功", toAgv(saved));
    }

    /**
     * 获取 AGV 实时位置
     */
    @GetMapping("/{agvCode}/position")
    public Result<Map<String, Object>> getAgvPosition(@PathVariable String agvCode) {
        AgvDeviceEntity d = deviceRepository.findByCode(agvCode);
        if (d == null) {
            return Result.fail("AGV不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("position", d.getPosition() == null ? "" : d.getPosition());
        data.put("speed", d.getSpeed() == null ? 0 : d.getSpeed());
        data.put("direction", d.getDirection() == null ? "" : d.getDirection());
        return Result.success("查询成功", data);
    }

    /**
     * 获取任务列表
     */
    @GetMapping("/tasks")
    public Result<List<Map<String, Object>>> getTasks() {
        List<AgvTaskEntity> list = taskRepository.findAll();
        list.sort(Comparator.comparing(AgvTaskEntity::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AgvTaskEntity t : list) {
            rows.add(toTask(t));
        }
        return Result.success("查询成功", rows);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/tasks/{taskId}")
    public Result<Map<String, Object>> getTaskDetail(@PathVariable String taskId) {
        AgvTaskEntity t = taskRepository.findByTaskId(taskId);
        if (t == null) {
            return Result.fail("任务不存在");
        }
        return Result.success("查询成功", toTask(t));
    }

    /**
     * 创建任务
     */
    @PostMapping("/tasks")
    public Result<Map<String, Object>> createTask(@RequestBody Map<String, Object> body) {
        // 兼容LES联动下发的参数命名（taskType/sourceLocation/targetLocation/remark）
        String type = firstNonBlank(body, "type", "taskType");
        String priority = body == null ? null : String.valueOf(body.getOrDefault("priority", "medium"));
        String agvCode = body == null ? null : String.valueOf(body.getOrDefault("agvCode", ""));
        String startPoint = firstNonBlank(body, "startPoint", "sourceLocation");
        String endPoint = firstNonBlank(body, "endPoint", "targetLocation");
        String cargoInfo = firstNonBlank(body, "cargoInfo", "remark");

        // LES联动：优先使用外部下发的任务标识（LES任务taskNo），保证状态可回传对应
        String taskId = body == null ? null : String.valueOf(body.getOrDefault("taskId", ""));
        if (taskId == null || taskId.isBlank() || "null".equals(taskId)) {
            taskId = "TASK" + String.format("%06d", System.currentTimeMillis() % 1_000_000);
        } else {
            taskId = taskId.trim();
            if (taskRepository.findByTaskId(taskId) != null) {
                return Result.fail("任务标识已存在：" + taskId);
            }
        }
        AgvTaskEntity entity = new AgvTaskEntity();
        entity.setTaskId(taskId);
        entity.setTaskNo(taskId);
        entity.setType(normalizeTaskType(type));
        entity.setPriority(toPriorityNumber(priority));
        entity.setStatus("pending");
        entity.setAgvCode(blankToNull(agvCode));
        entity.setStartPoint(blankToNull(startPoint));
        entity.setEndPoint(blankToNull(endPoint));
        entity.setPayload(blankToNull(cargoInfo));
        entity.setTrackStatus("waiting");
        entity.setProgress(0);
        AgvTaskEntity saved = taskRepository.save(entity);

        writeOpLog("task_create", saved.getAgvCode(), saved.getTaskId(), "创建任务：" + saved.getTaskId(), "system");
        return Result.success("创建成功", toTask(saved));
    }

    /**
     * 更新任务状态
     */
    @PutMapping("/tasks/{taskId}/status")
    public Result<Map<String, Object>> updateTaskStatus(@PathVariable String taskId, @RequestBody Map<String, Object> body) {
        AgvTaskEntity t = taskRepository.findByTaskId(taskId);
        if (t == null) {
            return Result.fail("任务不存在");
        }
        String status = body == null ? null : String.valueOf(body.getOrDefault("status", ""));
        if (status == null || status.isBlank()) {
            return Result.fail("status不能为空");
        }
        t.setStatus(status.trim());
        if (Objects.equals(t.getStatus(), "running") && t.getStartTime() == null) {
            t.setStartTime(LocalDateTime.now());
        }
        if (Objects.equals(t.getStatus(), "completed")) {
            t.setProgress(100);
            if (t.getEndTime() == null) t.setEndTime(LocalDateTime.now());
        }
        AgvTaskEntity saved = taskRepository.save(t);
        writeOpLog("task_status", saved.getAgvCode(), saved.getTaskId(), "更新任务状态为：" + status, "system");
        reportTaskStatusToLes(saved.getTaskId(), saved.getStatus());
        return Result.success("更新成功", toTask(saved));
    }

    /**
     * AGV→LES联动：任务状态保存成功后回传LES，失败仅记录告警日志，不影响状态更新结果。
     *
     * @param agvTaskId AGV任务标识（LES下发时使用LES任务taskNo）
     * @param status    最新任务状态
     */
    private void reportTaskStatusToLes(String agvTaskId, String status) {
        if (lesTaskStatusClient == null) {
            return;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("eventId", UUID.randomUUID().toString());
            body.put("agvTaskId", agvTaskId);
            body.put("status", status);
            body.put("eventTime", LocalDateTime.now().toString());
            Result<Map<String, Object>> result = lesTaskStatusClient.reportTaskStatus(body);
            // 成功码兼容：0（ResultCode.SUCCESS）和200（HTTP风格）均视为成功
            boolean success = result != null && result.getCode() != null
                    && (result.getCode() == 0 || result.getCode() == 200);
            if (!success) {
                log.warn("LES任务状态回传返回失败：agvTaskId={}, status={}, msg={}", agvTaskId, status, result == null ? null : result.getMsg());
            }
        } catch (Exception e) {
            log.warn("LES任务状态回传异常：agvTaskId={}, status={}, error={}", agvTaskId, status, e.getMessage());
        }
    }

    @PutMapping("/tasks/{taskId}/priority")
    public Result<Map<String, Object>> updateTaskPriority(@PathVariable String taskId, @RequestBody Map<String, Object> body) {
        AgvTaskEntity t = taskRepository.findByTaskId(taskId);
        if (t == null) {
            return Result.fail("任务不存在");
        }
        String priority = body == null ? null : String.valueOf(body.getOrDefault("priority", ""));
        if (priority == null || priority.isBlank()) {
            return Result.fail("priority不能为空");
        }
        t.setPriority(toPriorityNumber(priority));
        AgvTaskEntity saved = taskRepository.save(t);
        writeOpLog("task_priority", saved.getAgvCode(), saved.getTaskId(), "更新任务优先级为：" + priority, "system");
        return Result.success("更新成功", toTask(saved));
    }

    /**
     * 分配任务给 AGV
     */
    @PutMapping("/tasks/{taskId}/assign")
    public Result<Map<String, Object>> assignTaskToAgv(@PathVariable String taskId, @RequestBody Map<String, Object> body) {
        AgvTaskEntity t = taskRepository.findByTaskId(taskId);
        if (t == null) {
            return Result.fail("任务不存在");
        }
        String agvCode = body == null ? null : String.valueOf(body.getOrDefault("agvCode", ""));
        if (agvCode == null || agvCode.isBlank()) {
            return Result.fail("agvCode不能为空");
        }
        AgvDeviceEntity d = deviceRepository.findByCode(agvCode.trim());
        if (d == null) {
            return Result.fail("AGV不存在");
        }

        t.setAgvCode(agvCode.trim());
        if (Objects.equals(t.getStatus(), "pending")) {
            t.setStatus("assigned");
        }
        AgvTaskEntity savedTask = taskRepository.save(t);

        AgvTaskAssignmentEntity assignment = new AgvTaskAssignmentEntity();
        assignment.setTaskId(savedTask.getTaskId());
        assignment.setAgvCode(agvCode.trim());
        assignment.setAssignedBy("system");
        assignment.setAssignmentStatus("assigned");
        taskAssignmentRepository.save(assignment);

        d.setCurrentTaskId(savedTask.getTaskId());
        if (d.getStatus() == null || Objects.equals(d.getStatus(), "idle")) {
            d.setStatus("running");
        }
        deviceRepository.save(d);

        writeOpLog("task_assign", agvCode.trim(), savedTask.getTaskId(), "分配任务给：" + agvCode, "system");
        return Result.success("分配成功", toTask(savedTask));
    }

    /**
     * 取消任务
     */
    @PutMapping("/tasks/{taskId}/cancel")
    public Result<Map<String, Object>> cancelTask(@PathVariable String taskId) {
        AgvTaskEntity t = taskRepository.findByTaskId(taskId);
        if (t == null) {
            return Result.fail("任务不存在");
        }
        t.setStatus("cancelled");
        AgvTaskEntity saved = taskRepository.save(t);

        if (saved.getAgvCode() != null && !saved.getAgvCode().isBlank()) {
            AgvDeviceEntity d = deviceRepository.findByCode(saved.getAgvCode());
            if (d != null && Objects.equals(d.getCurrentTaskId(), saved.getTaskId())) {
                d.setCurrentTaskId("");
                if (!Objects.equals(d.getStatus(), "charging")) {
                    d.setStatus("idle");
                }
                deviceRepository.save(d);
            }
        }
        writeOpLog("task_cancel", saved.getAgvCode(), saved.getTaskId(), "取消任务：" + saved.getTaskId(), "system");
        return Result.success("取消成功", toTask(saved));
    }

    /**
     * 获取路径规划列表
     */
    @GetMapping("/path-plans")
    public Result<List<Map<String, Object>>> getPathPlans() {
        List<AgvPathPlanEntity> list = pathPlanRepository.findAll();
        list.sort(Comparator.comparing(AgvPathPlanEntity::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AgvPathPlanEntity p : list) {
            rows.add(toPathPlan(p, null));
        }
        return Result.success("查询成功", rows);
    }

    /**
     * 获取路径规划详情
     */
    @GetMapping("/path-plans/{planId}")
    public Result<Map<String, Object>> getPathPlanDetail(@PathVariable String planId) {
        AgvPathPlanEntity plan = pathPlanRepository.findByPlanId(planId);
        if (plan == null) {
            return Result.fail("路径规划不存在");
        }
        List<AgvPathPointEntity> points = pathPointRepository.findByPlanIdOrderBySeqNoAsc(planId);
        return Result.success("查询成功", toPathPlan(plan, points));
    }

    /**
     * 创建路径规划
     */
    @PostMapping("/path-plans")
    public Result<Map<String, Object>> createPathPlan(@RequestBody Map<String, Object> body) {
        String taskId = body == null ? null : String.valueOf(body.getOrDefault("taskId", ""));
        String agvCode = body == null ? null : String.valueOf(body.getOrDefault("agvCode", ""));
        String startPoint = body == null ? null : String.valueOf(body.getOrDefault("startPoint", ""));
        String endPoint = body == null ? null : String.valueOf(body.getOrDefault("endPoint", ""));
        String algorithm = body == null ? null : String.valueOf(body.getOrDefault("algorithm", "A*"));
        Number distanceNum = body == null ? null : (body.get("distance") instanceof Number n ? n : null);
        Number timeNum = body == null ? null : (body.get("estimatedTime") instanceof Number n ? n : null);

        String planId = "PLAN" + String.format("%06d", System.currentTimeMillis() % 1_000_000);
        AgvPathPlanEntity plan = new AgvPathPlanEntity();
        plan.setPlanId(planId);
        plan.setTaskId(blankToNull(taskId));
        plan.setAgvCode(blankToNull(agvCode));
        plan.setStartPoint(blankToNull(startPoint));
        plan.setEndPoint(blankToNull(endPoint));
        plan.setAlgorithm(blankToNull(algorithm));
        plan.setStatus("planned");
        plan.setDistance(distanceNum == null ? BigDecimal.ZERO : BigDecimal.valueOf(distanceNum.doubleValue()));
        plan.setEstimatedTime(timeNum == null ? BigDecimal.ZERO : BigDecimal.valueOf(timeNum.doubleValue()));
        AgvPathPlanEntity saved = pathPlanRepository.save(plan);

        List<Map<String, Object>> pathPoints = body == null ? null : (body.get("pathPoints") instanceof List<?> list ? (List<Map<String, Object>>) list : null);
        if (pathPoints != null && !pathPoints.isEmpty()) {
            int seq = 1;
            for (Object obj : pathPoints) {
                if (!(obj instanceof Map<?, ?> m)) continue;
                AgvPathPointEntity point = new AgvPathPointEntity();
                point.setPlanId(saved.getPlanId());
                point.setSeqNo(seq++);
                Object desc = m.get("description");
                point.setNodeCode(desc == null ? "" : String.valueOf(desc));
                Double x = toDouble(m.get("x"));
                Double y = toDouble(m.get("y"));
                point.setX(x == null ? BigDecimal.ZERO : BigDecimal.valueOf(x));
                point.setY(y == null ? BigDecimal.ZERO : BigDecimal.valueOf(y));
                point.setHeading(BigDecimal.ZERO);
                point.setSpeedLimit(BigDecimal.ZERO);
                point.setRemark("");
                pathPointRepository.save(point);
            }
        } else {
            createDefaultPathPoints(saved.getPlanId(), saved.getStartPoint(), saved.getEndPoint());
        }

        writeOpLog("path_create", saved.getAgvCode(), saved.getPlanId(), "创建路径规划：" + saved.getPlanId(), "system");
        List<AgvPathPointEntity> points = pathPointRepository.findByPlanIdOrderBySeqNoAsc(saved.getPlanId());
        return Result.success("创建成功", toPathPlan(saved, points));
    }

    /**
     * 优化路径
     */
    @PostMapping("/path-plans/{planId}/optimize")
    public Result<Map<String, Object>> optimizePath(@PathVariable String planId) {
        AgvPathPlanEntity plan = pathPlanRepository.findByPlanId(planId);
        if (plan == null) {
            return Result.fail("路径规划不存在");
        }
        plan.setAlgorithm("opt-" + (plan.getAlgorithm() == null ? "A*" : plan.getAlgorithm()));
        plan.setStatus("executing");
        AgvPathPlanEntity saved = pathPlanRepository.save(plan);
        writeOpLog("path_optimize", saved.getAgvCode(), saved.getPlanId(), "优化路径：" + saved.getPlanId(), "system");
        List<AgvPathPointEntity> points = pathPointRepository.findByPlanIdOrderBySeqNoAsc(saved.getPlanId());
        return Result.success("优化成功", toPathPlan(saved, points));
    }

    /**
     * 获取交通节点列表
     */
    @GetMapping("/traffic/nodes")
    public Result<List<Map<String, Object>>> getTrafficNodes() {
        List<AgvTrafficNodeEntity> list = trafficNodeRepository.findAll();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AgvTrafficNodeEntity n : list) {
            rows.add(toTrafficNode(n));
        }
        return Result.success("查询成功", rows);
    }

    /**
     * 获取交通节点详情
     */
    @GetMapping("/traffic/nodes/{nodeId}")
    public Result<Map<String, Object>> getTrafficNodeDetail(@PathVariable Long nodeId) {
        AgvTrafficNodeEntity n = trafficNodeRepository.findById(nodeId).orElse(null);
        if (n == null) {
            return Result.fail("交通节点不存在");
        }
        return Result.success("查询成功", toTrafficNode(n));
    }

    /**
     * 释放交通节点
     */
    @PutMapping("/traffic/nodes/{nodeId}/release")
    public Result<Map<String, Object>> releaseTrafficNode(@PathVariable Long nodeId) {
        AgvTrafficNodeEntity n = trafficNodeRepository.findById(nodeId).orElse(null);
        if (n == null) {
            return Result.fail("交通节点不存在");
        }
        String nodeCode = n.getNodeCode();
        n.setStatus("normal");
        n.setLockedByAgvCode("");
        n.setLockedTime(null);
        AgvTrafficNodeEntity saved = trafficNodeRepository.save(n);

        AgvTrafficLockEntity lock = new AgvTrafficLockEntity();
        lock.setNodeCode(nodeCode);
        lock.setAgvCode("");
        lock.setLockStatus("released");
        lock.setReleaseTime(LocalDateTime.now());
        trafficLockRepository.save(lock);
        writeOpLog("traffic_release", "", nodeCode, "释放节点：" + nodeCode, "system");
        return Result.success("释放成功", toTrafficNode(saved));
    }

    /**
     * 获取交通规则列表
     */
    @GetMapping("/traffic/rules")
    public Result<List<Map<String, Object>>> getTrafficRules() {
        List<AgvTrafficRuleEntity> list = trafficRuleRepository.findAll();
        list.sort(Comparator.comparing(AgvTrafficRuleEntity::getId));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AgvTrafficRuleEntity r : list) {
            rows.add(toTrafficRule(r));
        }
        return Result.success("查询成功", rows);
    }

    /**
     * 创建交通规则
     */
    @PostMapping("/traffic/rules")
    public Result<Map<String, Object>> createTrafficRule(@RequestBody Map<String, Object> body) {
        String area = body == null ? null : String.valueOf(body.getOrDefault("area", ""));
        String ruleType = body == null ? null : String.valueOf(body.getOrDefault("ruleType", ""));
        String direction = body == null ? null : String.valueOf(body.getOrDefault("direction", ""));
        Number speedLimit = body == null ? null : (body.get("speedLimit") instanceof Number n ? n : null);
        String description = body == null ? null : String.valueOf(body.getOrDefault("description", ""));

        AgvTrafficRuleEntity r = new AgvTrafficRuleEntity();
        r.setRuleName(blankToNull(area) == null ? "交通规则" : area);
        r.setRuleType(blankToNull(ruleType));
        r.setPriority(1);
        r.setEnabled(Boolean.TRUE);
        r.setConditionJson("{\"area\":\"" + safeJson(area) + "\",\"direction\":\"" + safeJson(direction) + "\"}");
        r.setActionJson("{\"speedLimit\":" + (speedLimit == null ? 0 : speedLimit.doubleValue()) + ",\"desc\":\"" + safeJson(description) + "\"}");
        AgvTrafficRuleEntity saved = trafficRuleRepository.save(r);
        return Result.success("创建成功", toTrafficRule(saved));
    }

    /**
     * 更新交通规则（完整更新区域/类型/方向/限速/描述/状态，保留未传字段的旧值）
     */
    @PutMapping("/traffic/rules/{ruleId}")
    public Result<Map<String, Object>> updateTrafficRule(@PathVariable Long ruleId, @RequestBody Map<String, Object> body) {
        AgvTrafficRuleEntity r = trafficRuleRepository.findById(ruleId).orElse(null);
        if (r == null) {
            return Result.fail("交通规则不存在");
        }
        // 提取现有值作为默认
        String area = r.getRuleName() == null ? "" : r.getRuleName();
        String direction = extractJsonString(r.getConditionJson(), "direction", "");
        double speedLimit = extractSpeedLimit(r.getActionJson());
        String desc = extractDesc(r.getActionJson());

        // 应用新值
        if (body != null && body.get("status") instanceof Boolean b) {
            r.setEnabled(b);
        }
        if (body != null && body.get("ruleType") != null) {
            r.setRuleType(String.valueOf(body.get("ruleType")));
        }
        if (body != null && body.get("area") != null && !String.valueOf(body.get("area")).isBlank()) {
            area = String.valueOf(body.get("area")).trim();
            r.setRuleName(area);
        }
        if (body != null && body.get("direction") != null) {
            direction = String.valueOf(body.get("direction"));
        }
        if (body != null && body.get("speedLimit") instanceof Number n) {
            speedLimit = n.doubleValue();
        }
        if (body != null && body.get("description") != null) {
            desc = String.valueOf(body.get("description"));
        }
        // 重建JSON字段，保证condition/action完整
        r.setConditionJson("{\"area\":\"" + safeJson(area) + "\",\"direction\":\"" + safeJson(direction) + "\"}");
        r.setActionJson("{\"speedLimit\":" + speedLimit + ",\"desc\":\"" + safeJson(desc) + "\"}");
        AgvTrafficRuleEntity saved = trafficRuleRepository.save(r);
        return Result.success("更新成功", toTrafficRule(saved));
    }

    /**
     * 切换交通规则状态
     */
    @PutMapping("/traffic/rules/{ruleId}/toggle")
    public Result<Map<String, Object>> toggleTrafficRuleStatus(@PathVariable Long ruleId) {
        AgvTrafficRuleEntity r = trafficRuleRepository.findById(ruleId).orElse(null);
        if (r == null) {
            return Result.fail("交通规则不存在");
        }
        r.setEnabled(r.getEnabled() == null || !r.getEnabled());
        AgvTrafficRuleEntity saved = trafficRuleRepository.save(r);
        return Result.success("切换成功", toTrafficRule(saved));
    }

    /**
     * 删除交通规则
     */
    @DeleteMapping("/traffic/rules/{ruleId}")
    public Result<Void> deleteTrafficRule(@PathVariable Long ruleId) {
        if (!trafficRuleRepository.existsById(ruleId)) {
            return Result.fail("交通规则不存在");
        }
        trafficRuleRepository.deleteById(ruleId);
        return Result.success("删除成功");
    }

    /**
     * 获取故障告警列表
     */
    @GetMapping("/alerts")
    public Result<List<Map<String, Object>>> getFaultAlerts() {
        List<AgvFaultAlertEntity> list = faultAlertRepository.findAll();
        list.sort(Comparator.comparing(AgvFaultAlertEntity::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AgvFaultAlertEntity a : list) {
            rows.add(toAlert(a));
        }
        return Result.success("查询成功", rows);
    }

    /**
     * 获取故障告警详情
     */
    @GetMapping("/alerts/{alertId}")
    public Result<Map<String, Object>> getFaultAlertDetail(@PathVariable Long alertId) {
        AgvFaultAlertEntity a = faultAlertRepository.findById(alertId).orElse(null);
        if (a == null) {
            return Result.fail("告警不存在");
        }
        return Result.success("查询成功", toAlert(a));
    }

    /**
     * 处理故障告警
     */
    @PutMapping("/alerts/{alertId}/handle")
    public Result<Map<String, Object>> handleFaultAlert(@PathVariable Long alertId, @RequestBody Map<String, Object> body) {
        AgvFaultAlertEntity a = faultAlertRepository.findById(alertId).orElse(null);
        if (a == null) {
            return Result.fail("告警不存在");
        }
        String handleResult = body == null ? null : String.valueOf(body.getOrDefault("handleResult", ""));
        String operator = body == null ? null : String.valueOf(body.getOrDefault("operator", "system"));
        a.setHandleResult(handleResult == null ? "" : handleResult);
        a.setOperator(operator == null ? "system" : operator);
        a.setHandleTime(LocalDateTime.now());
        a.setStatus("handled");
        AgvFaultAlertEntity saved = faultAlertRepository.save(a);
        writeOpLog("alert_handle", saved.getAgvCode(), String.valueOf(saved.getId()), "处理告警：" + saved.getId(), operator);
        return Result.success("处理成功", toAlert(saved));
    }

    /**
     * 获取协同策略列表
     */
    @GetMapping("/collaboration/strategies")
    public Result<List<Map<String, Object>>> getCollaborationStrategies() {
        List<AgvCollaborationStrategyEntity> list = collaborationStrategyRepository.findAll();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AgvCollaborationStrategyEntity s : list) {
            rows.add(toStrategy(s));
        }
        return Result.success("查询成功", rows);
    }

    /**
     * 创建协同策略
     */
    @PostMapping("/collaboration/strategies")
    public Result<Map<String, Object>> createCollaborationStrategy(@RequestBody Map<String, Object> body) {
        String strategyName = body == null ? null : String.valueOf(body.getOrDefault("name", body.getOrDefault("strategyName", "")));
        String description = body == null ? null : String.valueOf(body.getOrDefault("description", ""));
        String config = body == null ? null : String.valueOf(body.getOrDefault("config", body.getOrDefault("strategyConfigJson", "{}")));

        String strategyId = "STRATEGY" + String.format("%06d", System.currentTimeMillis() % 1_000_000);
        AgvCollaborationStrategyEntity s = new AgvCollaborationStrategyEntity();
        s.setStrategyId(strategyId);
        s.setStrategyName(blankToNull(strategyName));
        s.setDescription(blankToNull(description));
        s.setStrategyConfigJson(blankToNull(config));
        s.setStatus("inactive");
        AgvCollaborationStrategyEntity saved = collaborationStrategyRepository.save(s);
        return Result.success("创建成功", toStrategy(saved));
    }

    /**
     * 激活协同策略
     */
    @PutMapping("/collaboration/strategies/{strategyId}/activate")
    public Result<Map<String, Object>> activateCollaborationStrategy(@PathVariable String strategyId) {
        AgvCollaborationStrategyEntity s = collaborationStrategyRepository.findByStrategyId(strategyId);
        if (s == null) {
            return Result.fail("协同策略不存在");
        }
        s.setStatus("active");
        AgvCollaborationStrategyEntity saved = collaborationStrategyRepository.save(s);

        AgvCollaborationLogEntity log = new AgvCollaborationLogEntity();
        log.setLogCode("LOG-" + System.currentTimeMillis());
        log.setStrategyId(strategyId);
        log.setContent("激活协同策略：" + strategyId);
        collaborationLogRepository.save(log);

        return Result.success("激活成功", toStrategy(saved));
    }

    /**
     * 更新协同策略
     */
    @PutMapping("/collaboration/strategies/{strategyId}")
    public Result<Map<String, Object>> updateCollaborationStrategy(@PathVariable String strategyId, @RequestBody Map<String, Object> body) {
        AgvCollaborationStrategyEntity s = collaborationStrategyRepository.findByStrategyId(strategyId);
        if (s == null) {
            return Result.fail("协同策略不存在");
        }
        if (body != null && body.get("strategyName") != null) {
            s.setStrategyName(String.valueOf(body.get("strategyName")));
        }
        if (body != null && body.get("description") != null) {
            s.setDescription(String.valueOf(body.get("description")));
        }
        // 兼容config和strategyConfigJson两种命名（与创建接口一致）
        Object configValue = body == null ? null : (body.get("strategyConfigJson") != null ? body.get("strategyConfigJson") : body.get("config"));
        if (configValue != null) {
            s.setStrategyConfigJson(String.valueOf(configValue));
        }
        AgvCollaborationStrategyEntity saved = collaborationStrategyRepository.save(s);
        return Result.success("更新成功", toStrategy(saved));
    }

    /**
     * 获取冲突避免配置
     */
    @GetMapping("/collision/config")
    public Result<Map<String, Object>> getCollisionAvoidanceConfig() {
        AgvCollisionAvoidanceConfigEntity c = collisionAvoidanceConfigRepository.findByConfigKey("global");
        String raw = c == null ? "{}" : (c.getConfigValue() == null ? "{}" : c.getConfigValue());
        Map<String, Object> data = new HashMap<>();
        data.put("id", "global");
        data.put("safetyDistance", extractJsonNumber(raw, "safetyDistance", 1.0));
        data.put("warningDistance", extractJsonNumber(raw, "warningDistance", 2.0));
        data.put("emergencyStopDistance", extractJsonNumber(raw, "emergencyStopDistance", 0.5));
        data.put("handlingStrategy", extractJsonString(raw, "handlingStrategy", "wait-first"));
        data.put("priorityRule", extractJsonString(raw, "priorityRule", "task-priority"));
        data.put("detectionFrequency", (int) extractJsonNumber(raw, "detectionFrequency", 500));
        data.put("createTime", formatTime(c == null ? null : c.getCreateTime()));
        data.put("updateTime", formatTime(c == null ? null : c.getUpdateTime()));
        return Result.success("查询成功", data);
    }

    /**
     * 更新冲突避免配置
     */
    @PutMapping("/collision/config")
    public Result<Map<String, Object>> updateCollisionAvoidanceConfig(@RequestBody Map<String, Object> body) {
        Map<String, Object> payload = body == null ? new HashMap<>() : new HashMap<>(body);
        String value = toJsonString(payload);
        AgvCollisionAvoidanceConfigEntity c = collisionAvoidanceConfigRepository.findByConfigKey("global");
        if (c == null) {
            c = new AgvCollisionAvoidanceConfigEntity();
            c.setConfigKey("global");
        }
        c.setConfigValue(value);
        AgvCollisionAvoidanceConfigEntity saved = collisionAvoidanceConfigRepository.save(c);
        return getCollisionAvoidanceConfig();
    }

    /**
     * 获取电池状态列表
     */
    @GetMapping("/battery/status")
    public Result<List<Map<String, Object>>> getBatteryStatuses() {
        List<AgvDeviceEntity> devices = deviceRepository.findAll();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AgvDeviceEntity d : devices) {
            Map<String, Object> row = new HashMap<>();
            row.put("agvCode", d.getCode());
            row.put("batteryLevel", d.getBatteryLevel() == null ? 0 : d.getBatteryLevel());
            row.put("voltage", d.getVoltage() == null ? 0 : d.getVoltage());
            row.put("temperature", d.getTemperature() == null ? 0 : d.getTemperature());
            row.put("chargingStatus", toChargingStatus(d.getStatus(), d.getBatteryLevel()));
            row.put("estimatedRuntime", estimateRuntimeMinutes(d.getBatteryLevel()));
            row.put("lastUpdate", formatTime(d.getLastUpdate()));
            rows.add(row);
        }
        return Result.success("查询成功", rows);
    }

    /**
     * 获取指定 AGV 电池状态
     */
    @GetMapping("/battery/{agvCode}")
    public Result<Map<String, Object>> getAgvBatteryStatus(@PathVariable String agvCode) {
        AgvDeviceEntity d = deviceRepository.findByCode(agvCode);
        if (d == null) {
            return Result.fail("AGV不存在");
        }
        Map<String, Object> row = new HashMap<>();
        row.put("agvCode", d.getCode());
        row.put("batteryLevel", d.getBatteryLevel() == null ? 0 : d.getBatteryLevel());
        row.put("voltage", d.getVoltage() == null ? 0 : d.getVoltage());
        row.put("temperature", d.getTemperature() == null ? 0 : d.getTemperature());
        row.put("chargingStatus", toChargingStatus(d.getStatus(), d.getBatteryLevel()));
        row.put("estimatedRuntime", estimateRuntimeMinutes(d.getBatteryLevel()));
        row.put("lastUpdate", formatTime(d.getLastUpdate()));
        return Result.success("查询成功", row);
    }

    /**
     * 获取运行统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getOperationStats() {
        List<AgvDeviceEntity> devices = deviceRepository.findAll();
        List<AgvTaskEntity> tasks = taskRepository.findAll();

        int totalAgvCount = devices.size();
        int activeAgvCount = (int) devices.stream().filter(d -> Objects.equals(d.getStatus(), "running") || Objects.equals(d.getStatus(), "idle")).count();
        int runningTaskCount = (int) tasks.stream().filter(t -> Objects.equals(t.getStatus(), "running") || Objects.equals(t.getStatus(), "assigned")).count();
        int completedTaskCount = (int) tasks.stream().filter(t -> Objects.equals(t.getStatus(), "completed")).count();
        int faultAgvCount = (int) devices.stream().filter(d -> Objects.equals(d.getStatus(), "fault")).count();

        double avgBatteryLevel = 0;
        if (!devices.isEmpty()) {
            int sum = 0;
            int cnt = 0;
            for (AgvDeviceEntity d : devices) {
                if (d.getBatteryLevel() != null) {
                    sum += d.getBatteryLevel();
                    cnt++;
                }
            }
            avgBatteryLevel = cnt == 0 ? 0 : (double) sum / cnt;
        }

        double avgTaskCompletionTime = 0;
        int timeCnt = 0;
        long sumMinutes = 0;
        for (AgvTaskEntity t : tasks) {
            if (t.getStartTime() != null && t.getEndTime() != null) {
                long minutes = java.time.Duration.between(t.getStartTime(), t.getEndTime()).toMinutes();
                if (minutes >= 0) {
                    sumMinutes += minutes;
                    timeCnt++;
                }
            }
        }
        avgTaskCompletionTime = timeCnt == 0 ? 0 : (double) sumMinutes / timeCnt;

        double uptimeRate = totalAgvCount == 0 ? 0 : (double) (totalAgvCount - faultAgvCount) * 100.0 / totalAgvCount;

        Map<String, Object> data = new HashMap<>();
        data.put("totalAgvCount", totalAgvCount);
        data.put("activeAgvCount", activeAgvCount);
        data.put("runningTaskCount", runningTaskCount);
        data.put("completedTaskCount", completedTaskCount);
        data.put("faultAgvCount", faultAgvCount);
        data.put("collisionAvoidanceCount", 0);
        data.put("avgTaskCompletionTime", avgTaskCompletionTime);
        data.put("avgBatteryLevel", avgBatteryLevel);
        data.put("uptimeRate", uptimeRate);
        return Result.success("查询成功", data);
    }

    /**
     * 获取统计历史数据（最小可用：按时间区间返回空或日志聚合）
     */
    @GetMapping("/stats/history")
    public Result<List<Map<String, Object>>> getStatsHistory(@RequestParam String startDate, @RequestParam String endDate) {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("startDate", startDate);
        row.put("endDate", endDate);
        row.put("totalAgvCount", (int) deviceRepository.count());
        row.put("activeAgvCount", (int) deviceRepository.findAll().stream().filter(d -> Objects.equals(d.getStatus(), "running") || Objects.equals(d.getStatus(), "idle")).count());
        row.put("runningTaskCount", (int) taskRepository.findAll().stream().filter(t -> Objects.equals(t.getStatus(), "running") || Objects.equals(t.getStatus(), "assigned")).count());
        row.put("completedTaskCount", (int) taskRepository.findAll().stream().filter(t -> Objects.equals(t.getStatus(), "completed")).count());
        row.put("faultAgvCount", (int) deviceRepository.findAll().stream().filter(d -> Objects.equals(d.getStatus(), "fault")).count());
        row.put("collisionAvoidanceCount", 0);
        row.put("avgTaskCompletionTime", 0);
        row.put("avgBatteryLevel", 0);
        row.put("uptimeRate", 0);
        rows.add(row);
        return Result.success("查询成功", rows);
    }

    /**
     * 获取协同日志列表
     */
    @GetMapping("/collaboration/logs")
    public Result<List<Map<String, Object>>> getCollaborationLogs() {
        List<AgvCollaborationLogEntity> list = collaborationLogRepository.findAll();
        list.sort(Comparator.comparing(AgvCollaborationLogEntity::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AgvCollaborationLogEntity l : list) {
            rows.add(toCollabLog(l));
        }
        return Result.success("查询成功", rows);
    }

    /**
     * 获取协同日志详情
     */
    @GetMapping("/collaboration/logs/{logId}")
    public Result<Map<String, Object>> getCollaborationLogDetail(@PathVariable Long logId) {
        AgvCollaborationLogEntity l = collaborationLogRepository.findById(logId).orElse(null);
        if (l == null) {
            return Result.fail("协同日志不存在");
        }
        return Result.success("查询成功", toCollabLog(l));
    }

    /**
     * 发送充电指令
     */
    @PostMapping("/commands/{agvCode}/charge")
    public Result<Map<String, Object>> sendChargeCommand(@PathVariable String agvCode) {
        AgvDeviceEntity d = deviceRepository.findByCode(agvCode);
        if (d == null) {
            return Result.fail("AGV不存在");
        }
        d.setStatus("charging");
        deviceRepository.save(d);
        writeOpLog("command_charge", agvCode, agvCode, "发送充电指令", "system");
        return Result.success("指令已发送", Map.of("success", true, "message", "AGV充电指令已发送"));
    }

    /**
     * 发送暂停指令
     */
    @PostMapping("/commands/{agvCode}/pause")
    public Result<Map<String, Object>> sendPauseCommand(@PathVariable String agvCode) {
        AgvDeviceEntity d = deviceRepository.findByCode(agvCode);
        if (d == null) {
            return Result.fail("AGV不存在");
        }
        writeOpLog("command_pause", agvCode, agvCode, "发送暂停指令", "system");
        return Result.success("指令已发送", Map.of("success", true, "message", "AGV暂停成功"));
    }

    /**
     * 发送继续指令
     */
    @PostMapping("/commands/{agvCode}/resume")
    public Result<Map<String, Object>> sendResumeCommand(@PathVariable String agvCode) {
        AgvDeviceEntity d = deviceRepository.findByCode(agvCode);
        if (d == null) {
            return Result.fail("AGV不存在");
        }
        writeOpLog("command_resume", agvCode, agvCode, "发送继续指令", "system");
        return Result.success("指令已发送", Map.of("success", true, "message", "AGV继续运行成功"));
    }

    /**
     * 发送紧急停止指令
     */
    @PostMapping("/commands/{agvCode}/emergency-stop")
    public Result<Map<String, Object>> sendEmergencyStopCommand(@PathVariable String agvCode) {
        AgvDeviceEntity d = deviceRepository.findByCode(agvCode);
        if (d == null) {
            return Result.fail("AGV不存在");
        }
        d.setStatus("fault");
        deviceRepository.save(d);
        writeOpLog("command_emergency_stop", agvCode, agvCode, "发送紧急停止指令", "system");
        return Result.success("指令已发送", Map.of("success", true, "message", "紧急停止已触发"));
    }

    /**
     * 紧急停止所有 AGV
     */
    @PostMapping("/commands/emergency-stop-all")
    public Result<Map<String, Object>> emergencyStopAllAgvs() {
        List<AgvDeviceEntity> list = deviceRepository.findAll();
        for (AgvDeviceEntity d : list) {
            d.setStatus("fault");
        }
        deviceRepository.saveAll(list);
        writeOpLog("command_emergency_stop_all", "", "", "紧急停止所有AGV", "system");
        return Result.success("指令已发送", Map.of("success", true, "message", "已紧急停止所有AGV"));
    }

    @PostMapping("/commands/pause-all")
    public Result<Map<String, Object>> pauseAllAgvs() {
        List<AgvDeviceEntity> list = deviceRepository.findAll();
        for (AgvDeviceEntity d : list) {
            if (Objects.equals(d.getStatus(), "running")) {
                d.setStatus("idle");
            }
        }
        deviceRepository.saveAll(list);
        writeOpLog("command_pause_all", "", "", "暂停所有AGV", "system");
        return Result.success("指令已发送", Map.of("success", true, "message", "已暂停所有AGV"));
    }

    /**
     * 恢复所有 AGV 运行
     */
    @PostMapping("/commands/resume-all")
    public Result<Map<String, Object>> resumeAllAgvs() {
        List<AgvDeviceEntity> list = deviceRepository.findAll();
        for (AgvDeviceEntity d : list) {
            if (Objects.equals(d.getStatus(), "fault")) {
                d.setStatus("idle");
            }
        }
        deviceRepository.saveAll(list);
        writeOpLog("command_resume_all", "", "", "恢复所有AGV运行", "system");
        return Result.success("指令已发送", Map.of("success", true, "message", "已恢复所有AGV运行"));
    }

    /**
     * 模拟路径
     */
    @PostMapping("/path/simulate")
    public Result<Map<String, Object>> simulatePath(@RequestBody Map<String, Object> body) {
        String startPoint = body == null ? null : String.valueOf(body.getOrDefault("startPoint", ""));
        String endPoint = body == null ? null : String.valueOf(body.getOrDefault("endPoint", ""));
        String algorithm = body == null ? null : String.valueOf(body.getOrDefault("algorithm", "a-star"));
        String simulationId = "SIM" + System.currentTimeMillis();

        Map<String, Object> result = new HashMap<>();
        result.put("simulationId", simulationId);
        result.put("startPoint", startPoint == null ? "" : startPoint);
        result.put("endPoint", endPoint == null ? "" : endPoint);
        result.put("algorithm", algorithm == null ? "" : algorithm);
        result.put("distance", 125.6);
        result.put("time", 2.3);
        result.put("energy", 15.2);
        result.put("status", "success");

        writeOpLog("path_simulation", "", simulationId, toJsonString(result), "system");
        return Result.success("模拟成功", result);
    }

    /**
     * 获取模拟结果
     */
    @GetMapping("/path/simulate/{simulationId}")
    public Result<Map<String, Object>> getSimulationResult(@PathVariable String simulationId) {
        AgvOperationLogEntity log = operationLogRepository.findFirstByLogTypeAndRefId("path_simulation", simulationId);
        if (log == null || log.getContent() == null || log.getContent().isBlank()) {
            return Result.fail("模拟结果不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("simulationId", simulationId);
        data.put("content", log.getContent());
        return Result.success("查询成功", data);
    }

    /**
     * 获取区域列表
     */
    @GetMapping("/areas")
    public Result<List<Map<String, Object>>> getAreas() {
        List<AgvAreaEntity> list = areaRepository.findAll();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AgvAreaEntity a : list) {
            rows.add(toArea(a));
        }
        return Result.success("查询成功", rows);
    }

    /**
     * 创建区域
     */
    @PostMapping("/areas")
    public Result<Map<String, Object>> createArea(@RequestBody Map<String, Object> body) {
        String areaId = body == null ? null : String.valueOf(body.getOrDefault("id", body.getOrDefault("areaId", "")));
        if (areaId == null || areaId.isBlank()) {
            areaId = "AREA" + String.format("%06d", System.currentTimeMillis() % 1_000_000);
        }
        AgvAreaEntity entity = new AgvAreaEntity();
        entity.setAreaId(areaId);
        entity.setAreaName(String.valueOf(body.getOrDefault("name", body.getOrDefault("areaName", ""))));
        entity.setAreaType(String.valueOf(body.getOrDefault("type", body.getOrDefault("areaType", ""))));
        entity.setPolygonJson(String.valueOf(body.getOrDefault("polygonJson", body.getOrDefault("polygon", "{}"))));
        entity.setStatus(String.valueOf(body.getOrDefault("status", "active")));
        AgvAreaEntity saved = areaRepository.save(entity);
        return Result.success("创建成功", toArea(saved));
    }

    /**
     * 更新区域
     */
    @PutMapping("/areas/{areaId}")
    public Result<Map<String, Object>> updateArea(@PathVariable String areaId, @RequestBody Map<String, Object> body) {
        AgvAreaEntity entity = areaRepository.findByAreaId(areaId);
        if (entity == null) {
            return Result.fail("区域不存在");
        }
        if (body != null && body.get("areaName") != null) entity.setAreaName(String.valueOf(body.get("areaName")));
        if (body != null && body.get("areaType") != null) entity.setAreaType(String.valueOf(body.get("areaType")));
        if (body != null && body.get("polygonJson") != null) entity.setPolygonJson(String.valueOf(body.get("polygonJson")));
        if (body != null && body.get("status") != null) entity.setStatus(String.valueOf(body.get("status")));
        AgvAreaEntity saved = areaRepository.save(entity);
        return Result.success("更新成功", toArea(saved));
    }

    /**
     * 删除区域
     */
    @DeleteMapping("/areas/{areaId}")
    public Result<Void> deleteArea(@PathVariable String areaId) {
        AgvAreaEntity entity = areaRepository.findByAreaId(areaId);
        if (entity == null) {
            return Result.fail("区域不存在");
        }
        areaRepository.delete(entity);
        return Result.success("删除成功");
    }

    private Map<String, Object> toAgv(AgvDeviceEntity d) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", d.getId() == null ? "" : String.valueOf(d.getId()));
        row.put("code", d.getCode() == null ? "" : d.getCode());
        row.put("name", d.getName() == null ? "" : d.getName());
        row.put("type", d.getType() == null ? "" : d.getType());
        row.put("model", d.getModel() == null ? "" : d.getModel());
        row.put("status", d.getStatus() == null ? "idle" : d.getStatus());
        row.put("batteryLevel", d.getBatteryLevel() == null ? 0 : d.getBatteryLevel());
        row.put("voltage", d.getVoltage() == null ? 0 : d.getVoltage());
        row.put("temperature", d.getTemperature() == null ? 0 : d.getTemperature());
        row.put("speed", d.getSpeed() == null ? 0 : d.getSpeed());
        row.put("direction", d.getDirection() == null ? "" : d.getDirection());
        row.put("position", d.getPosition() == null ? "" : d.getPosition());
        row.put("currentTaskId", d.getCurrentTaskId() == null ? "" : d.getCurrentTaskId());
        row.put("lastUpdate", formatTime(d.getLastUpdate()));
        row.put("loadStatus", d.getLoadStatus() == null ? "empty" : d.getLoadStatus());
        return row;
    }

    private Map<String, Object> toTask(AgvTaskEntity t) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", t.getTaskId() == null ? "" : t.getTaskId());
        row.put("taskNo", t.getTaskNo() == null ? "" : t.getTaskNo());
        row.put("type", blankToNull(t.getType()) == null ? "transport" : t.getType());
        row.put("priority", fromPriorityNumber(t.getPriority()));
        row.put("agvCode", t.getAgvCode() == null ? "" : t.getAgvCode());
        row.put("startPoint", t.getStartPoint() == null ? "" : t.getStartPoint());
        row.put("endPoint", t.getEndPoint() == null ? "" : t.getEndPoint());
        row.put("cargoInfo", t.getPayload() == null ? "" : t.getPayload());
        row.put("status", t.getStatus() == null ? "pending" : t.getStatus());
        row.put("createTime", formatTime(t.getCreateTime()));
        row.put("startTime", formatTime(t.getStartTime()));
        row.put("endTime", formatTime(t.getEndTime()));
        row.put("trackStatus", t.getTrackStatus() == null ? "" : t.getTrackStatus());
        row.put("progress", t.getProgress() == null ? 0 : t.getProgress());
        row.put("estimatedCompletion", formatTime(t.getEstimatedCompletion()));
        return row;
    }

    private Map<String, Object> toPathPlan(AgvPathPlanEntity plan, List<AgvPathPointEntity> points) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", plan.getPlanId() == null ? "" : plan.getPlanId());
        row.put("taskId", plan.getTaskId() == null ? "" : plan.getTaskId());
        row.put("agvCode", plan.getAgvCode() == null ? "" : plan.getAgvCode());
        row.put("startPoint", plan.getStartPoint() == null ? "" : plan.getStartPoint());
        row.put("endPoint", plan.getEndPoint() == null ? "" : plan.getEndPoint());
        row.put("distance", plan.getDistance() == null ? 0 : plan.getDistance().doubleValue());
        row.put("estimatedTime", plan.getEstimatedTime() == null ? 0 : plan.getEstimatedTime().doubleValue());
        row.put("algorithm", plan.getAlgorithm() == null ? "" : plan.getAlgorithm());
        row.put("status", plan.getStatus() == null ? "planned" : plan.getStatus());
        row.put("createTime", formatTime(plan.getCreateTime()));
        row.put("pathPoints", points == null ? List.of() : toPathPoints(points));
        return row;
    }

    private List<Map<String, Object>> toPathPoints(List<AgvPathPointEntity> points) {
        int maxSeq = points.stream().filter(Objects::nonNull).map(AgvPathPointEntity::getSeqNo).filter(Objects::nonNull).max(Integer::compareTo).orElse(0);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (AgvPathPointEntity p : points) {
            if (p == null) continue;
            Map<String, Object> row = new HashMap<>();
            row.put("id", p.getId() == null ? "" : String.valueOf(p.getId()));
            row.put("x", p.getX() == null ? 0 : p.getX());
            row.put("y", p.getY() == null ? 0 : p.getY());
            row.put("sequence", p.getSeqNo() == null ? 0 : p.getSeqNo());
            row.put("type", inferPointType(p.getSeqNo(), maxSeq));
            row.put("description", p.getNodeCode() == null ? "" : p.getNodeCode());
            rows.add(row);
        }
        return rows;
    }

    private Map<String, Object> toTrafficNode(AgvTrafficNodeEntity n) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", n.getId() == null ? 0 : n.getId());
        row.put("nodeId", n.getNodeCode() == null ? "" : n.getNodeCode());
        row.put("nodeName", n.getNodeName() == null ? "" : n.getNodeName());
        row.put("type", "intersection");
        row.put("status", normalizeNodeStatus(n.getStatus()));
        row.put("lockAgv", n.getLockedByAgvCode() == null ? "" : n.getLockedByAgvCode());
        row.put("lockTime", formatTime(n.getLockedTime()));
        row.put("queueLength", 0);
        row.put("description", "");
        row.put("area", n.getAreaCode() == null ? "" : n.getAreaCode());
        return row;
    }

    private Map<String, Object> toTrafficRule(AgvTrafficRuleEntity r) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", r.getId() == null ? 0 : r.getId());
        row.put("area", r.getRuleName() == null ? "" : r.getRuleName());
        row.put("ruleType", normalizeRuleType(r.getRuleType()));
        row.put("direction", extractJsonString(r.getConditionJson(), "direction", ""));
        row.put("speedLimit", extractSpeedLimit(r.getActionJson()));
        row.put("description", extractDesc(r.getActionJson()));
        row.put("status", r.getEnabled() != null && r.getEnabled());
        row.put("createTime", formatTime(r.getCreateTime()));
        row.put("updateTime", formatTime(r.getUpdateTime()));
        return row;
    }

    private Map<String, Object> toAlert(AgvFaultAlertEntity a) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", a.getId() == null ? 0 : a.getId());
        String agvCode = a.getAgvCode() == null ? "" : a.getAgvCode();
        row.put("agvCode", agvCode);
        row.put("faultType", a.getAlertType() == null ? "" : a.getAlertType());
        row.put("level", a.getAlertLevel() == null ? "info" : a.getAlertLevel());
        row.put("message", a.getMessage() == null ? "" : a.getMessage());
        row.put("position", resolveAgvPosition(agvCode));
        row.put("faultTime", formatTime(a.getCreateTime()));
        row.put("status", normalizeAlertStatus(a.getStatus()));
        row.put("operator", a.getOperator() == null ? "" : a.getOperator());
        row.put("handleTime", formatTime(a.getHandleTime()));
        row.put("handleResult", a.getHandleResult() == null ? "" : a.getHandleResult());
        return row;
    }

    private Map<String, Object> toStrategy(AgvCollaborationStrategyEntity s) {
        Map<String, Object> row = new HashMap<>();
        String raw = s.getStrategyConfigJson() == null ? "{}" : s.getStrategyConfigJson();
        row.put("id", s.getStrategyId() == null ? "" : s.getStrategyId());
        row.put("name", s.getStrategyName() == null ? "" : s.getStrategyName());
        row.put("mode", extractJsonString(raw, "mode", "centralized"));
        row.put("assignmentAlgorithm", extractJsonString(raw, "assignmentAlgorithm", "default"));
        row.put("maxConcurrentTasks", (int) extractJsonNumber(raw, "maxConcurrentTasks", 1));
        row.put("responseTime", (int) extractJsonNumber(raw, "responseTime", 500));
        row.put("description", s.getDescription() == null ? "" : s.getDescription());
        row.put("status", Objects.equals(s.getStatus(), "active"));
        row.put("createTime", formatTime(s.getCreateTime()));
        row.put("updateTime", formatTime(s.getUpdateTime()));
        return row;
    }

    private Map<String, Object> toCollabLog(AgvCollaborationLogEntity l) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", l.getId() == null ? 0 : l.getId());
        row.put("time", formatTime(l.getCreateTime()));
        row.put("content", l.getContent() == null ? "" : l.getContent());
        row.put("type", "system_event");
        row.put("relatedAgv", l.getAgvCode() == null ? "" : l.getAgvCode());
        row.put("relatedTask", "");
        return row;
    }

    private Map<String, Object> toArea(AgvAreaEntity a) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", a.getAreaId());
        row.put("areaId", a.getAreaId());
        row.put("areaName", a.getAreaName());
        row.put("areaType", a.getAreaType());
        row.put("polygonJson", a.getPolygonJson());
        row.put("status", a.getStatus());
        row.put("createTime", formatTime(a.getCreateTime()));
        row.put("updateTime", formatTime(a.getUpdateTime()));
        return row;
    }

    private void writeOpLog(String type, String agvCode, String refId, String content, String operator) {
        AgvOperationLogEntity log = new AgvOperationLogEntity();
        log.setLogType(type);
        log.setAgvCode(agvCode == null ? "" : agvCode);
        log.setRefId(refId == null ? "" : refId);
        log.setContent(content == null ? "" : content);
        log.setOperator(operator == null ? "system" : operator);
        operationLogRepository.save(log);
    }

    private void createDefaultPathPoints(String planId, String start, String end) {
        AgvPathPointEntity p1 = new AgvPathPointEntity();
        p1.setPlanId(planId);
        p1.setSeqNo(1);
        p1.setNodeCode(start == null ? "" : start);
        p1.setX(BigDecimal.ZERO);
        p1.setY(BigDecimal.ZERO);
        p1.setHeading(BigDecimal.ZERO);
        p1.setSpeedLimit(BigDecimal.valueOf(1.2));
        p1.setRemark("");
        pathPointRepository.save(p1);

        AgvPathPointEntity p2 = new AgvPathPointEntity();
        p2.setPlanId(planId);
        p2.setSeqNo(2);
        p2.setNodeCode(end == null ? "" : end);
        p2.setX(BigDecimal.valueOf(10.0));
        p2.setY(BigDecimal.valueOf(5.0));
        p2.setHeading(BigDecimal.ZERO);
        p2.setSpeedLimit(BigDecimal.valueOf(1.2));
        p2.setRemark("");
        pathPointRepository.save(p2);
    }

    private static String blankToNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * 按优先级从多个键中读取第一个非空白字符串值
     *
     * @param body 请求体
     * @param keys 候选键（按优先级顺序）
     * @return 第一个非空白值，全部为空时返回空串
     */
    private static String firstNonBlank(Map<String, Object> body, String... keys) {
        if (body == null || keys == null) return "";
        for (String key : keys) {
            Object v = body.get(key);
            if (v == null) continue;
            String s = String.valueOf(v).trim();
            if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) return s;
        }
        return "";
    }

    /**
     * 任务类型标准化：将LES中文类型映射为AGV英文类型
     *
     * @param type 原始类型
     * @return 标准化类型（transport/charge/inspection/emergency）
     */
    private static String normalizeTaskType(String type) {
        if (type == null || type.isBlank()) return "transport";
        return switch (type.trim()) {
            case "搬运", "运输", "transport" -> "transport";
            case "充电", "charge" -> "charge";
            case "巡检", "inspection" -> "inspection";
            case "紧急", "emergency" -> "emergency";
            default -> "transport";
        };
    }

    private static final java.time.format.DateTimeFormatter TIME_FMT = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 格式化时间为统一格式（yyyy-MM-dd HH:mm:ss）
     * @param t 时间对象
     * @return 格式化后的时间字符串
     */
    private static String formatTime(LocalDateTime t) {
        if (t == null) return "";
        return t.format(TIME_FMT);
    }

    private static int toPriorityNumber(String priority) {
        if (priority == null) return 2;
        return switch (priority.trim().toLowerCase()) {
            case "low" -> 1;
            case "high" -> 3;
            case "emergency" -> 4;
            default -> 2;
        };
    }

    private static String fromPriorityNumber(Integer priority) {
        if (priority == null) return "medium";
        return switch (priority) {
            case 1 -> "low";
            case 3 -> "high";
            case 4 -> "emergency";
            default -> "medium";
        };
    }

    private static String inferPointType(Integer seq, int maxSeq) {
        if (seq == null) return "waypoint";
        if (seq == 1) return "start";
        if (seq == maxSeq) return "end";
        return "waypoint";
    }

    private static String normalizeNodeStatus(String status) {
        if (status == null || status.isBlank()) return "normal";
        String s = status.trim().toLowerCase();
        if (s.equals("locked")) return "locked";
        if (s.equals("maintenance")) return "maintenance";
        if (s.equals("warning")) return "warning";
        if (s.equals("free")) return "normal";
        return status;
    }

    private String resolveAgvPosition(String agvCode) {
        if (agvCode == null || agvCode.isBlank()) return "";
        AgvDeviceEntity d = deviceRepository.findByCode(agvCode.trim());
        if (d == null || d.getPosition() == null) return "";
        return d.getPosition();
    }

    private static String normalizeAlertStatus(String status) {
        if (status == null || status.isBlank()) return "pending";
        String s = status.trim().toLowerCase();
        if (s.equals("open") || s.equals("pending")) return "pending";
        if (s.equals("handled")) return "handled";
        if (s.equals("resolved")) return "resolved";
        return status;
    }

    private static String normalizeRuleType(String ruleType) {
        if (ruleType == null || ruleType.isBlank()) return "speed";
        String t = ruleType.trim().toLowerCase();
        if (t.contains("direction")) return "directional";
        if (t.contains("restrict")) return "restricted";
        if (t.contains("speed")) return "speed";
        return "speed";
    }

    private static String toChargingStatus(String status, Integer batteryLevel) {
        if (status != null && status.trim().equalsIgnoreCase("charging")) return "charging";
        int level = batteryLevel == null ? 0 : batteryLevel;
        if (level >= 95) return "charged";
        return "not_charging";
    }

    private static int estimateRuntimeMinutes(Integer batteryLevel) {
        int level = batteryLevel == null ? 0 : batteryLevel;
        return Math.max(0, level) * 10;
    }

    private static String extractJsonString(String json, String key, String defaultValue) {
        if (json == null || key == null || key.isBlank()) return defaultValue;
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return defaultValue;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return defaultValue;
        int q1 = json.indexOf('"', colon);
        if (q1 < 0) return defaultValue;
        int q2 = json.indexOf('"', q1 + 1);
        if (q2 < 0) return defaultValue;
        return json.substring(q1 + 1, q2);
    }

    private static double extractJsonNumber(String json, String key, double defaultValue) {
        if (json == null || key == null || key.isBlank()) return defaultValue;
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return defaultValue;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return defaultValue;
        int pos = colon + 1;
        while (pos < json.length() && (json.charAt(pos) == ' ' || json.charAt(pos) == '"')) pos++;
        int end = pos;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) end++;
        if (end <= pos) return defaultValue;
        try {
            return Double.parseDouble(json.substring(pos, end));
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    private static Double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        if (v == null) return 0.0;
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (Exception ignored) {
            return 0.0;
        }
    }

    private static String safeJson(String v) {
        if (v == null) return "";
        return v.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String extractDesc(String json) {
        if (json == null) return "";
        int idx = json.indexOf("\"desc\"");
        if (idx < 0) return "";
        int colon = json.indexOf(':', idx);
        if (colon < 0) return "";
        int q1 = json.indexOf('"', colon);
        if (q1 < 0) return "";
        int q2 = json.indexOf('"', q1 + 1);
        if (q2 < 0) return "";
        return json.substring(q1 + 1, q2);
    }

    private static double extractSpeedLimit(String json) {
        if (json == null) return 0;
        int idx = json.indexOf("speedLimit");
        if (idx < 0) return 0;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return 0;
        int end = colon + 1;
        while (end < json.length() && (json.charAt(end) == ' ' || json.charAt(end) == '"')) end++;
        int end2 = end;
        while (end2 < json.length() && (Character.isDigit(json.charAt(end2)) || json.charAt(end2) == '.')) end2++;
        try {
            return Double.parseDouble(json.substring(end, end2));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String toJsonString(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(safeJson(e.getKey())).append("\":");
            Object v = e.getValue();
            if (v == null) {
                sb.append("null");
            } else if (v instanceof Number || v instanceof Boolean) {
                sb.append(v);
            } else {
                sb.append("\"").append(safeJson(String.valueOf(v))).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
