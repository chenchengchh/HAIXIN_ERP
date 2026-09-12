package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.wms.entity.WaveEntity;
import com.hxcoe.wms.entity.OutboundOrderEntity;
import com.hxcoe.wms.entity.OutboundOrderItemEntity;
import com.hxcoe.wms.entity.PickingTaskEntity;
import com.hxcoe.wms.entity.PickingTaskItemEntity;
import com.hxcoe.wms.repository.OutboundOrderRepository;
import com.hxcoe.wms.repository.PickingTaskRepository;
import com.hxcoe.wms.service.WaveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping({"/api/v1/wms/outbound/waves", "/wms/outbound/waves", "/api/wms/outbound/waves"})
public class WaveController {

    @Autowired
    private WaveService waveService;

    @Autowired
    private OutboundOrderRepository outboundOrderRepository;

    @Autowired
    private PickingTaskRepository pickingTaskRepository;

    @PostMapping
    public ApiResponse<Map<String, Object>> createWave(@RequestBody(required = false) Object body) {
        CreateWaveRequest req = CreateWaveRequest.from(body);
        WaveEntity result = waveService.createWave(req.outboundOrderIds, req.remark, req.createdBy);
        return success("波次创建成功", toRow(result));
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> getWaves(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "waveNo", required = false) String waveNo,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "orderType", required = false) String orderType,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        LocalDateTime start = parseDateTimeOrNull(startTime);
        LocalDateTime end = parseDateTimeOrNull(endTime);
        String backendOrderType = orderType == null ? null : fromFrontOrderType(orderType.trim());
        Page<WaveEntity> result = waveService.getWaves(pageable, waveNo, status, backendOrderType, start, end);
        List<Map<String, Object>> rows = result.getContent().stream().map(this::toRow).toList();
        PageResult<Map<String, Object>> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, rows);
        return success("波次列表查询成功", pageResult);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> getWaveById(@PathVariable Long id) {
        Optional<WaveEntity> result = waveService.getWaveById(id);
        if (result.isEmpty()) {
            return notFound("波次不存在");
        }
        WaveEntity wave = result.get();
        Map<String, Object> detail = toRow(wave);
        List<OutboundOrderEntity> orders = outboundOrderRepository.findAll((root, query, cb) -> cb.equal(root.get("waveId"), id));
        detail.put("orders", orders.stream().map(this::toOrderRow).toList());
        detail.put("tasks", toTaskRows(id));
        return success("波次查询成功", detail);
    }

    @PostMapping("/auto-create")
    public ApiResponse<Map<String, Object>> autoCreate(@RequestBody(required = false) Object body) {
        CreateWaveRequest req = CreateWaveRequest.from(body);
        WaveEntity result = waveService.autoCreate(req.remark, req.createdBy);
        return success("自动生成波次成功", toRow(result));
    }

    @PutMapping("/{id}/allocate")
    public ApiResponse<Map<String, Object>> allocate(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        String assignedTo = body == null ? null : String.valueOf(body.getOrDefault("assignedTo", ""));
        String remark = body == null ? null : String.valueOf(body.getOrDefault("remark", ""));
        WaveEntity result = waveService.allocate(id, blankToNull(assignedTo), blankToNull(remark));
        if (result == null) {
            return notFound("波次不存在");
        }
        return success("波次分配成功", toRow(result));
    }

    @PutMapping("/{id}/start-picking")
    public ApiResponse<Map<String, Object>> startPicking(@PathVariable Long id) {
        WaveEntity result = waveService.startPicking(id);
        if (result == null) {
            return notFound("波次不存在");
        }
        return success("开始拣货成功", toRow(result));
    }

    @PutMapping("/{id}/release")
    public ApiResponse<Map<String, Object>> releaseWave(@PathVariable Long id) {
        WaveEntity result = waveService.releaseWave(id);
        if (result != null) {
            return success("波次释放成功", toRow(result));
        }
        return notFound("波次不存在");
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<Map<String, Object>> complete(@PathVariable Long id) {
        WaveEntity result = waveService.complete(id);
        if (result == null) {
            return notFound("波次不存在");
        }
        return success("波次完成成功", toRow(result));
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancel(@PathVariable Long id) {
        WaveEntity result = waveService.cancel(id);
        if (result == null) {
            return notFound("波次不存在");
        }
        return success("波次取消成功", toRow(result));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        WaveEntity wave = waveService.getWaveById(id).orElse(null);
        if (wave == null) {
            return notFound("波次不存在");
        }
        List<OutboundOrderEntity> orders = outboundOrderRepository.findByWaveId(id);
        for (OutboundOrderEntity order : orders) {
            if (order == null) {
                continue;
            }
            order.setWaveId(null);
            if ("WAVED".equalsIgnoreCase(order.getStatus())) {
                order.setStatus("APPROVED");
            }
            outboundOrderRepository.save(order);
        }
        outboundOrderRepository.flush();
        waveService.cancel(id);
        return success("波次删除成功", null);
    }

    private Map<String, Object> toRow(WaveEntity wave) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", wave.getId());
        row.put("waveNo", wave.getWaveNo());
        row.put("status", wave.getStatus());
        row.put("remark", wave.getRemark());
        row.put("createdBy", wave.getCreatedBy());
        row.put("assignedTo", wave.getAssignedTo());
        row.put("assignedTime", formatDateTime(wave.getAssignedTime()));
        row.put("releasedTime", formatDateTime(wave.getReleasedTime()));
        row.put("completedAt", formatDateTime(wave.getCompletedTime()));
        row.put("createdAt", formatDateTime(wave.getCreatedTime()));
        row.put("createTime", formatDateTime(wave.getCreatedTime()));
        row.put("updatedAt", formatDateTime(wave.getUpdatedTime()));
        row.put("orderCount", wave.getOrderCount() == null ? 0 : wave.getOrderCount());
        row.put("totalQuantity", wave.getTotalQuantity() == null ? BigDecimal.ZERO : wave.getTotalQuantity());
        row.put("orderType", wave.getOrderType());
        return row;
    }

    private Map<String, Object> toOrderRow(OutboundOrderEntity order) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", order.getId());
        row.put("outboundNo", order.getOrderNo());
        row.put("orderNo", order.getOrderNo());
        row.put("orderType", toFrontOrderType(order.getType()));
        row.put("customerName", order.getCustomerName());
        row.put("createTime", formatDateTime(order.getCreatedTime()));
        row.put("status", order.getStatus());
        // 订单总数量为明细数量求和，供前端"订单数量"列展示
        BigDecimal totalQuantity = BigDecimal.ZERO;
        if (order.getItems() != null) {
            for (OutboundOrderItemEntity item : order.getItems()) {
                if (item != null && item.getQuantity() != null) {
                    totalQuantity = totalQuantity.add(item.getQuantity());
                }
            }
        }
        row.put("totalQuantity", totalQuantity);
        return row;
    }

    private List<Map<String, Object>> toTaskRows(Long waveId) {
        List<PickingTaskEntity> tasks = pickingTaskRepository.findAll((root, query, cb) -> cb.equal(root.get("waveId"), waveId));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (PickingTaskEntity task : tasks) {
            if (task == null || task.getItems() == null) {
                continue;
            }
            for (PickingTaskItemEntity item : task.getItems()) {
                if (item == null) {
                    continue;
                }
                Map<String, Object> row = new HashMap<>();
                row.put("taskNo", task.getTaskNo());
                row.put("materialCode", item.getMaterialCode());
                row.put("materialName", item.getMaterialName());
                row.put("specification", "");
                row.put("unit", item.getUnit());
                row.put("quantity", item.getQuantity());
                row.put("locationCode", item.getLocationCode());
                row.put("status", item.getStatus());
                rows.add(row);
            }
        }
        return rows;
    }

    private static String toFrontOrderType(String type) {
        if (type == null) {
            return "";
        }
        String t = type.trim().toUpperCase();
        if ("SALES".equals(t)) {
            return "sales";
        }
        if ("PRODUCTION".equals(t) || "MATERIAL".equals(t)) {
            return "material";
        }
        if ("TRANSFER".equals(t)) {
            return "transfer";
        }
        return type;
    }

    private static String fromFrontOrderType(String type) {
        if (type == null) {
            return null;
        }
        String t = type.trim().toLowerCase();
        if (t.isEmpty()) {
            return null;
        }
        if ("sales".equals(t)) {
            return "SALES";
        }
        if ("material".equals(t)) {
            return "PRODUCTION";
        }
        if ("transfer".equals(t)) {
            return "TRANSFER";
        }
        return type;
    }

    private static String blankToNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static String formatDateTime(LocalDateTime t) {
        // 统一输出"yyyy-MM-dd HH:mm:ss"，避免toString()微秒非0时格式不一致
        return com.hxcoe.wms.util.WmsDateTimes.formatOrNull(t);
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

    private static final class CreateWaveRequest {
        private final List<Long> outboundOrderIds;
        private final String remark;
        private final String createdBy;

        private CreateWaveRequest(List<Long> outboundOrderIds, String remark, String createdBy) {
            this.outboundOrderIds = outboundOrderIds == null ? List.of() : outboundOrderIds;
            this.remark = remark;
            this.createdBy = createdBy == null || createdBy.isBlank() ? "admin" : createdBy.trim();
        }

        @SuppressWarnings("unchecked")
        static CreateWaveRequest from(Object body) {
            if (body == null) {
                return new CreateWaveRequest(List.of(), null, "admin");
            }
            if (body instanceof List<?> list) {
                List<Long> ids = list.stream()
                        .map(v -> {
                            if (v == null) return null;
                            try {
                                return Long.valueOf(String.valueOf(v));
                            } catch (Exception e) {
                                return null;
                            }
                        })
                        .filter(v -> v != null)
                        .toList();
                return new CreateWaveRequest(ids, null, "admin");
            }
            if (body instanceof Map<?, ?> map) {
                Object idsRaw = map.get("outboundOrderIds");
                if (idsRaw == null) {
                    idsRaw = map.get("orderIds");
                }
                List<Long> ids = new ArrayList<>();
                if (idsRaw instanceof List<?> list) {
                    for (Object v : list) {
                        try {
                            ids.add(Long.valueOf(String.valueOf(v)));
                        } catch (Exception ignored) {
                        }
                    }
                }
                Object remarkRaw = map.get("remark");
                Object createdByRaw = map.get("createdBy");
                if (createdByRaw == null) {
                    createdByRaw = map.get("createUser");
                }
                String remark = String.valueOf(remarkRaw == null ? "" : remarkRaw);
                String createdBy = String.valueOf(createdByRaw == null ? "admin" : createdByRaw);
                return new CreateWaveRequest(ids, blankToNull(remark), blankToNull(createdBy));
            }
            return new CreateWaveRequest(List.of(), null, "admin");
        }
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}
