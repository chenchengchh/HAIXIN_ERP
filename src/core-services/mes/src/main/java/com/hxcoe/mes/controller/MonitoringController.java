package com.hxcoe.mes.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.mes.entity.EquipmentDataEntity;
import com.hxcoe.mes.entity.EquipmentFaultEntity;
import com.hxcoe.mes.entity.ProductionExecutionEntity;
import com.hxcoe.mes.repository.EquipmentDataRepository;
import com.hxcoe.mes.repository.EquipmentFaultRepository;
import com.hxcoe.mes.repository.ProductionExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/mes", "/mes/v1", "/api/v1/mes", "/api/mes"})
public class MonitoringController {

    @Autowired
    private ProductionExecutionRepository productionExecutionRepository;

    @Autowired
    private EquipmentDataRepository equipmentDataRepository;

    @Autowired
    private EquipmentFaultRepository equipmentFaultRepository;

    @GetMapping("/production-progress")
    public Result<List<Map<String, Object>>> productionProgress() {
        List<ProductionExecutionEntity> executions = productionExecutionRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (ProductionExecutionEntity item : executions) {
            if (item.getIsDeleted() != null && item.getIsDeleted() == 1) {
                continue;
            }
            int totalQty = item.getPlanQuantity() != null ? item.getPlanQuantity() : 0;
            int completedQty = item.getQualifiedQuantity() != null ? item.getQualifiedQuantity() : 0;
            int progress = 0;
            if (totalQty > 0) {
                progress = BigDecimal.valueOf(completedQty)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalQty), 0, RoundingMode.HALF_UP)
                        .intValue();
            }
            String status = item.getExecutionStatus() != null && item.getExecutionStatus() == 3 ? "completed" : "in_progress";
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", String.valueOf(item.getId()));
            row.put("workOrderId", String.valueOf(item.getId()));
            row.put("workOrderNo", item.getExecutionNo());
            row.put("materialId", item.getProductCode());
            row.put("materialName", item.getProductName());
            row.put("totalQty", totalQty);
            row.put("completedQty", completedQty);
            row.put("progress", progress);
            row.put("status", status);
            row.put("updateTime", item.getUpdatedTime() != null ? item.getUpdatedTime() : item.getCreatedTime());
            result.add(row);
        }
        return Result.success("生产进度查询成功", result);
    }

    @GetMapping("/equipment-status")
    public Result<List<Map<String, Object>>> equipmentStatus() {
        List<EquipmentDataEntity> dataList = equipmentDataRepository.findAll();
        Map<String, EquipmentDataEntity> latestByEquipment = dataList.stream()
                .filter(d -> d.getEquipmentId() != null && !d.getEquipmentId().isBlank())
                .collect(Collectors.toMap(
                        EquipmentDataEntity::getEquipmentId,
                        Function.identity(),
                        (a, b) -> {
                            LocalDateTime at = a.getTimestamp();
                            LocalDateTime bt = b.getTimestamp();
                            if (at == null) return b;
                            if (bt == null) return a;
                            return bt.isAfter(at) ? b : a;
                        }
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (EquipmentDataEntity item : latestByEquipment.values()) {
            String status = mapEquipmentStatus(item.getStatus());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", String.valueOf(item.getId()));
            row.put("equipmentId", item.getEquipmentId());
            row.put("equipmentName", item.getEquipmentName());
            row.put("equipmentType", item.getEquipmentType());
            row.put("status", status);
            row.put("uptime", 0);
            row.put("downtime", 0);
            row.put("efficiency", 0);
            row.put("updateTime", item.getTimestamp());
            result.add(row);
        }
        return Result.success("设备状态查询成功", result);
    }

    @GetMapping("/process-params")
    public Result<List<Map<String, Object>>> processParams() {
        List<EquipmentDataEntity> dataList = equipmentDataRepository.findAll();
        List<Map<String, Object>> result = dataList.stream()
                .sorted(Comparator.comparing(EquipmentDataEntity::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(200)
                .map(this::toProcessParam)
                .collect(Collectors.toList());
        return Result.success("工艺参数查询成功", result);
    }

    @GetMapping("/process-params/{id}/advice")
    public Result<Map<String, Object>> processParamAdvice(@PathVariable("id") Long id) {
        EquipmentDataEntity item = equipmentDataRepository.findById(id).orElse(null);
        if (item == null) {
            return Result.fail("工艺参数记录不存在");
        }
        String status = mapParamStatus(item.getStatus());
        String advice;
        List<String> actions = new ArrayList<>();
        if ("alarm".equals(status)) {
            advice = "参数超出控制范围，建议立即处置";
            actions.add("确认报警来源与参数采集是否异常");
            actions.add("按SOP降低设备负载或暂停当前工步");
            actions.add("检查传感器/夹具/原料批次与工艺配方");
            actions.add("记录处置结果并复测参数恢复情况");
        } else if ("warning".equals(status)) {
            advice = "参数接近控制边界，建议预防性调整";
            actions.add("复核参数设定值与工艺卡要求");
            actions.add("检查设备状态（温度/压力/速度）是否有漂移");
            actions.add("必要时校准传感器并复测");
        } else {
            advice = "参数正常";
            actions.add("继续监控并按计划抽检");
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", String.valueOf(item.getId()));
        resp.put("workstationId", item.getEquipmentId());
        resp.put("workstationName", item.getEquipmentName());
        resp.put("parameterName", item.getParameterName());
        resp.put("status", status);
        resp.put("advice", advice);
        resp.put("actions", actions);
        return Result.success("工艺参数处置建议查询成功", resp);
    }

    @GetMapping("/equipment/{id}")
    public Result<Map<String, Object>> equipmentDetail(@PathVariable("id") String equipmentId) {
        List<EquipmentDataEntity> latest = equipmentDataRepository.findTop50ByEquipmentIdOrderByTimestampDesc(equipmentId);
        if (latest.isEmpty()) {
            return Result.fail("设备不存在");
        }
        EquipmentDataEntity item = latest.get(0);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(item.getId()));
        row.put("equipmentId", item.getEquipmentId());
        row.put("equipmentName", item.getEquipmentName());
        row.put("equipmentType", item.getEquipmentType());
        row.put("status", mapEquipmentStatus(item.getStatus()));
        row.put("uptime", 0);
        row.put("downtime", 0);
        row.put("efficiency", 0);
        row.put("temperature", null);
        row.put("pressure", null);
        row.put("speed", null);
        row.put("updateTime", item.getTimestamp());
        return Result.success("设备详情查询成功", row);
    }

    @GetMapping("/equipment/{id}/faults")
    public Result<List<Map<String, Object>>> equipmentFaultHistory(@PathVariable("id") String equipmentId) {
        List<EquipmentFaultEntity> list = equipmentFaultRepository.findTop20ByEquipmentIdOrderByOccurTimeDesc(equipmentId);
        List<Map<String, Object>> result = list.stream().map(f -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("faultId", String.valueOf(f.getId()));
            row.put("faultType", f.getFaultType());
            row.put("faultDescription", f.getFaultDescription());
            row.put("occurTime", f.getOccurTime());
            row.put("repairTime", f.getRepairTime());
            row.put("repairPerson", f.getRepairPerson());
            row.put("status", f.getStatus());
            return row;
        }).collect(Collectors.toList());
        return Result.success("设备故障历史查询成功", result);
    }

    @GetMapping("/workstations/{workstationId}/process-params/realtime")
    public Result<List<Map<String, Object>>> realtimeProcessParams(@PathVariable("workstationId") String workstationId) {
        List<EquipmentDataEntity> latest = equipmentDataRepository.findTop50ByEquipmentIdOrderByTimestampDesc(workstationId);
        List<Map<String, Object>> result = latest.stream().map(this::toProcessParam).collect(Collectors.toList());
        return Result.success("实时工艺参数查询成功", result);
    }

    private Map<String, Object> toProcessParam(EquipmentDataEntity item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", String.valueOf(item.getId()));
        row.put("workstationId", item.getEquipmentId());
        row.put("workstationName", item.getEquipmentName());
        row.put("parameterName", item.getParameterName());
        row.put("parameterValue", item.getParameterValue());
        row.put("unit", item.getUnit());
        row.put("upperLimit", 0);
        row.put("lowerLimit", 0);
        row.put("status", mapParamStatus(item.getStatus()));
        row.put("timestamp", item.getTimestamp());
        return row;
    }

    private String mapEquipmentStatus(String status) {
        if (status == null) {
            return "idle";
        }
        String s = status.toLowerCase(Locale.ROOT);
        if (s.contains("alarm")) {
            return "down";
        }
        if (s.contains("warning")) {
            return "idle";
        }
        if (s.contains("normal")) {
            return "running";
        }
        return "idle";
    }

    private String mapParamStatus(String status) {
        if (status == null) {
            return "normal";
        }
        String s = status.toLowerCase(Locale.ROOT);
        if (s.contains("alarm")) {
            return "alarm";
        }
        if (s.contains("warning")) {
            return "warning";
        }
        return "normal";
    }
}
