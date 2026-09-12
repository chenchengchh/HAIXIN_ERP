package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.wms.entity.PickingTaskEntity;
import com.hxcoe.wms.entity.PickingTaskItemEntity;
import com.hxcoe.wms.repository.PickingTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping({"/api/v1/wms/outbound/picking-tasks", "/wms/outbound/picking-tasks", "/api/wms/outbound/picking-tasks"})
public class PickingTaskController {

    @Autowired
    private PickingTaskRepository pickingTaskRepository;

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String waveNo,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        LocalDateTime start = parseDateTimeOrNull(startTime);
        LocalDateTime end = parseDateTimeOrNull(endTime);

        Specification<PickingTaskEntity> specification = (root, query, cb) -> {
            // Hibernate 6下cb.conjunction()+getExpressions().add()会静默失效，使用List<Predicate>+cb.and()
            List<Predicate> predicates = new ArrayList<>();
            if (waveNo != null && !waveNo.isBlank()) {
                predicates.add(cb.like(root.get("waveNo"), "%" + waveNo.trim() + "%"));
            }
            if (orderNo != null && !orderNo.isBlank()) {
                predicates.add(cb.like(root.get("orderNo"), "%" + orderNo.trim() + "%"));
            }
            if (operatorName != null && !operatorName.isBlank()) {
                predicates.add(cb.equal(root.get("operatorName"), operatorName.trim()));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            if (start != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdTime"), start));
            }
            if (end != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdTime"), end));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<PickingTaskEntity> result = pickingTaskRepository.findAll(specification, pageable);
        List<Map<String, Object>> rows = result.getContent().stream().map(this::toRow).toList();
        PageResult<Map<String, Object>> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, rows);
        return success("拣货任务列表查询成功", pageResult);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        PickingTaskEntity entity = pickingTaskRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("拣货任务不存在");
        }
        Map<String, Object> detail = toRow(entity);
        detail.put("items", toItems(entity.getItems()));
        return success("拣货任务查询成功", detail);
    }

    @PutMapping("/{id}/assign")
    public ApiResponse<Map<String, Object>> assign(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PickingTaskEntity entity = pickingTaskRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("拣货任务不存在");
        }
        String operatorId = body == null ? null : String.valueOf(body.getOrDefault("operatorId", ""));
        String operatorName = body == null ? null : String.valueOf(body.getOrDefault("operatorName", ""));
        entity.setOperatorId(blankToNull(operatorId));
        entity.setOperatorName(blankToNull(operatorName));
        entity.setAssignedTime(LocalDateTime.now());
        if (Objects.equals(entity.getStatus(), "pending")) {
            entity.setStatus("assigned");
        }
        PickingTaskEntity saved = pickingTaskRepository.save(entity);
        return success("拣货任务分配成功", toRow(saved));
    }

    @PutMapping("/{id}/start")
    public ApiResponse<Map<String, Object>> start(@PathVariable Long id) {
        PickingTaskEntity entity = pickingTaskRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("拣货任务不存在");
        }
        entity.setStatus("working");
        entity.setStartTime(LocalDateTime.now());
        PickingTaskEntity saved = pickingTaskRepository.save(entity);
        return success("拣货任务开始成功", toRow(saved));
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<Map<String, Object>> complete(@PathVariable Long id) {
        PickingTaskEntity entity = pickingTaskRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("拣货任务不存在");
        }
        entity.setStatus("done");
        entity.setEndTime(LocalDateTime.now());
        if (entity.getItems() != null) {
            for (PickingTaskItemEntity item : entity.getItems()) {
                if (item == null) {
                    continue;
                }
                item.setStatus("done");
                if (item.getPickTime() == null) {
                    item.setPickTime(LocalDateTime.now());
                }
            }
        }
        PickingTaskEntity saved = pickingTaskRepository.save(entity);
        Map<String, Object> detail = toRow(saved);
        detail.put("items", toItems(saved.getItems()));
        return success("拣货任务完成成功", detail);
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancel(@PathVariable Long id) {
        PickingTaskEntity entity = pickingTaskRepository.findById(id).orElse(null);
        if (entity == null) {
            return notFound("拣货任务不存在");
        }
        entity.setStatus("cancelled");
        PickingTaskEntity saved = pickingTaskRepository.save(entity);
        return success("拣货任务取消成功", toRow(saved));
    }

    /**
     * 修改拣货任务明细数量
     * @param taskId 任务ID
     * @param itemId 明细ID
     * @param body 请求体（quantity 实拣数量，status 可选）
     * @return 更新后的任务详情
     */
    @PutMapping("/{taskId}/items/{itemId}/qty")
    public ApiResponse<Map<String, Object>> updateItemQty(@PathVariable Long taskId, @PathVariable Long itemId, @RequestBody Map<String, Object> body) {
        PickingTaskEntity entity = pickingTaskRepository.findById(taskId).orElse(null);
        if (entity == null) {
            return notFound("拣货任务不存在");
        }
        PickingTaskItemEntity target = null;
        if (entity.getItems() != null) {
            for (PickingTaskItemEntity item : entity.getItems()) {
                if (item != null && Objects.equals(item.getId(), itemId)) {
                    target = item;
                    break;
                }
            }
        }
        if (target == null) {
            return notFound("拣货明细不存在");
        }
        // 更新实拣数量（兼容 quantity / pickedQty 两种字段名）
        Object qtyValue = body == null ? null : body.getOrDefault("quantity", body.get("pickedQty"));
        if (qtyValue != null) {
            try {
                target.setQuantity(new java.math.BigDecimal(String.valueOf(qtyValue)));
            } catch (NumberFormatException e) {
                return ApiResponse.error(400, "数量格式错误");
            }
        }
        // 请求体携带状态则同步更新明细状态
        Object statusValue = body == null ? null : body.get("status");
        if (statusValue != null && !String.valueOf(statusValue).isBlank()) {
            target.setStatus(String.valueOf(statusValue).trim());
            if (Objects.equals(target.getStatus(), "done") && target.getPickTime() == null) {
                target.setPickTime(LocalDateTime.now());
            }
        }
        PickingTaskEntity saved = pickingTaskRepository.save(entity);
        Map<String, Object> detail = toRow(saved);
        detail.put("items", toItems(saved.getItems()));
        return success("拣货明细数量修改成功", detail);
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats(@RequestParam(required = false) String date) {
        LocalDate d = parseDateOrToday(date);
        LocalDateTime start = d.atStartOfDay();
        LocalDateTime end = d.plusDays(1).atStartOfDay().minusNanos(1);

        List<PickingTaskEntity> tasks = pickingTaskRepository.findAll((root, query, cb) -> {
            // Hibernate 6下cb.conjunction()+getExpressions().add()会静默失效，使用List<Predicate>+cb.and()
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdTime"), start));
            predicates.add(cb.lessThanOrEqualTo(root.get("createdTime"), end));
            return cb.and(predicates.toArray(new Predicate[0]));
        });

        int totalOrders = tasks.size();
        int completedOrders = 0;
        int pendingOrders = 0;
        long totalPickSeconds = 0;
        int pickTimeCount = 0;
        int totalItems = 0;
        int doneItems = 0;
        for (PickingTaskEntity t : tasks) {
            if (t == null) {
                continue;
            }
            if (Objects.equals(t.getStatus(), "done") || Objects.equals(t.getStatus(), "completed")) {
                completedOrders++;
            } else if (!Objects.equals(t.getStatus(), "cancelled")) {
                pendingOrders++;
            }
            if (t.getStartTime() != null && t.getEndTime() != null) {
                totalPickSeconds += java.time.Duration.between(t.getStartTime(), t.getEndTime()).toSeconds();
                pickTimeCount++;
            }
            if (t.getItems() != null) {
                for (PickingTaskItemEntity item : t.getItems()) {
                    totalItems++;
                    if (item != null && Objects.equals(item.getStatus(), "done")) {
                        doneItems++;
                    }
                }
            }
        }

        int avgPickTime = pickTimeCount == 0 ? 0 : (int) Math.round((double) totalPickSeconds / pickTimeCount);
        int todayProgress = totalItems == 0 ? 0 : (int) Math.round((double) doneItems * 100.0 / totalItems);

        Map<String, Object> data = new HashMap<>();
        data.put("totalOrders", totalOrders);
        data.put("completedOrders", completedOrders);
        data.put("pendingOrders", pendingOrders);
        data.put("avgPickTime", avgPickTime);
        data.put("todayProgress", todayProgress);
        return success("统计数据查询成功", data);
    }

    @GetMapping("/operators")
    public ApiResponse<List<Map<String, Object>>> operators() {
        List<PickingTaskEntity> tasks = pickingTaskRepository.findAll();
        Map<String, Map<String, Object>> unique = new LinkedHashMap<>();
        for (PickingTaskEntity t : tasks) {
            if (t == null || t.getOperatorName() == null || t.getOperatorName().isBlank()) {
                continue;
            }
            String name = t.getOperatorName().trim();
            Map<String, Object> row = new HashMap<>();
            row.put("id", t.getOperatorId() == null ? name : t.getOperatorId());
            row.put("name", name);
            unique.put(name, row);
        }
        return success("操作员列表查询成功", new ArrayList<>(unique.values()));
    }

    @GetMapping("/performance")
    public ApiResponse<List<Map<String, Object>>> performance(@RequestParam(required = false) String date) {
        LocalDate d = parseDateOrToday(date);
        LocalDateTime start = d.atStartOfDay();
        LocalDateTime end = d.plusDays(1).atStartOfDay().minusNanos(1);

        List<PickingTaskEntity> tasks = pickingTaskRepository.findAll((root, query, cb) -> {
            // Hibernate 6下cb.conjunction()+getExpressions().add()会静默失效，使用List<Predicate>+cb.and()
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdTime"), start));
            predicates.add(cb.lessThanOrEqualTo(root.get("createdTime"), end));
            return cb.and(predicates.toArray(new Predicate[0]));
        });

        Map<String, List<PickingTaskEntity>> byOp = new LinkedHashMap<>();
        for (PickingTaskEntity t : tasks) {
            if (t == null || t.getOperatorName() == null || t.getOperatorName().isBlank()) {
                continue;
            }
            String op = t.getOperatorName().trim();
            byOp.computeIfAbsent(op, k -> new ArrayList<>()).add(t);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map.Entry<String, List<PickingTaskEntity>> e : byOp.entrySet()) {
            String op = e.getKey();
            int completed = 0;
            long totalSeconds = 0;
            int cnt = 0;
            for (PickingTaskEntity t : e.getValue()) {
                if (Objects.equals(t.getStatus(), "done") || Objects.equals(t.getStatus(), "completed")) {
                    completed++;
                }
                if (t.getStartTime() != null && t.getEndTime() != null) {
                    totalSeconds += java.time.Duration.between(t.getStartTime(), t.getEndTime()).toSeconds();
                    cnt++;
                }
            }
            int avgTime = cnt == 0 ? 0 : (int) Math.round((double) totalSeconds / cnt);
            int score = Math.max(0, Math.min(100, completed * 5 + (avgTime == 0 ? 0 : Math.max(0, 60 - avgTime))));
            Map<String, Object> row = new HashMap<>();
            row.put("operator", op);
            row.put("completed", completed);
            row.put("avgTime", avgTime);
            row.put("score", score);
            rows.add(row);
        }
        return success("操作员绩效查询成功", rows);
    }

    @GetMapping("/bottlenecks")
    public ApiResponse<List<Map<String, Object>>> bottlenecks(@RequestParam(defaultValue = "5") int limit) {
        int n = limit <= 0 ? 5 : Math.min(limit, 20);
        List<PickingTaskEntity> tasks = pickingTaskRepository.findAll();
        Map<String, Integer> pendingByLoc = new HashMap<>();
        for (PickingTaskEntity t : tasks) {
            if (t == null || t.getItems() == null) {
                continue;
            }
            for (PickingTaskItemEntity item : t.getItems()) {
                if (item == null || item.getLocationCode() == null || item.getLocationCode().isBlank()) {
                    continue;
                }
                if (!Objects.equals(item.getStatus(), "pending")) {
                    continue;
                }
                String key = locationGroup(item.getLocationCode());
                pendingByLoc.put(key, pendingByLoc.getOrDefault(key, 0) + 1);
            }
        }

        List<Map.Entry<String, Integer>> sorted = pendingByLoc.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .toList();

        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < Math.min(n, sorted.size()); i++) {
            Map.Entry<String, Integer> e = sorted.get(i);
            Map<String, Object> row = new HashMap<>();
            row.put("location", e.getKey());
            row.put("description", "待拣货项数较多");
            row.put("level", e.getValue() >= 10 ? "high" : e.getValue() >= 5 ? "medium" : "low");
            row.put("avgTime", e.getValue() * 3);
            rows.add(row);
        }
        return success("拣货瓶颈查询成功", rows);
    }

    private Map<String, Object> toRow(PickingTaskEntity t) {
        int totalItems = t.getItems() == null ? 0 : t.getItems().size();
        int pickedItems = 0;
        if (t.getItems() != null) {
            for (PickingTaskItemEntity item : t.getItems()) {
                if (item != null && Objects.equals(item.getStatus(), "done")) {
                    pickedItems++;
                }
            }
        }
        int progress = totalItems == 0 ? 0 : (int) Math.round((double) pickedItems * 100.0 / totalItems);

        Map<String, Object> row = new HashMap<>();
        row.put("id", t.getId());
        row.put("taskNo", t.getTaskNo());
        row.put("waveId", t.getWaveId());
        row.put("waveNo", t.getWaveNo());
        row.put("orderNo", t.getOrderNo());
        row.put("operatorId", t.getOperatorId());
        row.put("operatorName", t.getOperatorName());
        row.put("operator", t.getOperatorName());
        row.put("status", t.getStatus());
        row.put("totalItems", totalItems);
        row.put("pickedItems", pickedItems);
        row.put("progress", progress);
        row.put("createTime", formatDateTime(t.getCreatedTime()));
        row.put("createdAt", formatDateTime(t.getCreatedTime()));
        row.put("startTime", formatDateTime(t.getStartTime()));
        row.put("endTime", formatDateTime(t.getEndTime()));
        return row;
    }

    private static List<Map<String, Object>> toItems(List<PickingTaskItemEntity> items) {
        if (items == null) {
            return List.of();
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (PickingTaskItemEntity item : items) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", item.getId());
            row.put("taskId", item.getTask() == null ? null : item.getTask().getId());
            row.put("orderItemId", item.getOutboundOrderItemId());
            row.put("materialId", item.getMaterialCode());
            row.put("materialCode", item.getMaterialCode());
            row.put("materialName", item.getMaterialName());
            row.put("specification", "");
            row.put("unit", item.getUnit());
            row.put("quantity", item.getQuantity());
            row.put("sourceLocation", item.getLocationCode());
            row.put("locationCode", item.getLocationCode());
            row.put("targetLocation", "PICK");
            row.put("batchNo", item.getBatchNo());
            row.put("expiryDate", null);
            row.put("status", Objects.equals(item.getStatus(), "done") ? "done" : "pending");
            row.put("pickTime", formatDateTime(item.getPickTime()));
            row.put("createdAt", formatDateTime(item.getCreatedTime()));
            row.put("updatedAt", formatDateTime(item.getUpdatedTime()));
            rows.add(row);
        }
        return rows;
    }

    private static String formatDateTime(LocalDateTime t) {
        // 统一输出"yyyy-MM-dd HH:mm:ss"，避免toString()微秒非0时格式不一致
        return com.hxcoe.wms.util.WmsDateTimes.formatOrNull(t);
    }

    private static String blankToNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static LocalDateTime parseDateTimeOrNull(String v) {
        if (v == null || v.isBlank()) {
            return null;
        }
        String s = v.trim();
        try {
            if (s.length() == 10) {
                return LocalDate.parse(s).atStartOfDay();
            }
            return LocalDateTime.parse(s.replace(' ', 'T'));
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalDate parseDateOrToday(String v) {
        if (v == null || v.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(v.trim());
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private static String locationGroup(String locationCode) {
        String raw = locationCode.trim();
        int idx = raw.indexOf('-');
        if (idx > 0) {
            return raw.substring(0, idx);
        }
        return raw;
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}

