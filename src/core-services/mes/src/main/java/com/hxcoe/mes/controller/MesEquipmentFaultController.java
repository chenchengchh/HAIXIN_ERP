package com.hxcoe.mes.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.mes.client.EamFaultReportClient;
import com.hxcoe.mes.entity.EquipmentFaultEntity;
import com.hxcoe.mes.repository.EquipmentFaultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * MES 设备故障登记控制器。
 *
 * <p>提供故障登记入口：保存 mes_equipment_fault 后推送 EAM 生成故障记录（MES→EAM 闭环）。
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/mes/equipment", "/mes/equipment"})
public class MesEquipmentFaultController {

    @Autowired
    private EquipmentFaultRepository equipmentFaultRepository;

    @Autowired(required = false)
    private EamFaultReportClient eamFaultReportClient;

    /**
     * 登记设备故障：保存 MES 故障记录，并推送 EAM 自动生成 EAM 故障记录。
     *
     * @param body 故障信息（equipmentId/equipmentName/faultType/faultDescription/occurTime）
     * @return 处理结果（含 faultId）
     */
    @PostMapping("/faults")
    public Result<Map<String, Object>> registerFault(@RequestBody Map<String, Object> body) {
        String equipmentId = str(body == null ? null : body.get("equipmentId"));
        if (equipmentId == null || equipmentId.isBlank()) {
            return Result.fail("equipmentId 不能为空");
        }

        // 保存 MES 侧故障记录
        EquipmentFaultEntity fault = new EquipmentFaultEntity();
        fault.setEquipmentId(equipmentId);
        fault.setEquipmentName(str(body.get("equipmentName")));
        fault.setFaultType(str(body.get("faultType")));
        fault.setFaultDescription(str(body.get("faultDescription")));
        LocalDateTime occurTime = parseDateTime(body.get("occurTime"));
        fault.setOccurTime(occurTime != null ? occurTime : LocalDateTime.now());
        fault.setStatus("reported");
        fault.setCreateTime(LocalDateTime.now());
        equipmentFaultRepository.save(fault);

        // 故障记录保存后推送 EAM（失败仅告警，不影响本地登记）
        pushFaultToEam(fault);

        Map<String, Object> data = new HashMap<>();
        data.put("faultId", fault.getId());
        data.put("equipmentId", fault.getEquipmentId());
        data.put("status", fault.getStatus());
        return Result.success("MES 设备故障登记成功", data);
    }

    /**
     * 推送设备故障事件到 EAM（MES→EAM 闭环）。
     *
     * <p>推送失败仅记录 warn 日志，不影响本地故障登记结果。
     *
     * @param fault 已保存的 MES 故障记录
     */
    private void pushFaultToEam(EquipmentFaultEntity fault) {
        if (eamFaultReportClient == null) {
            log.warn("EamFaultReportClient 不可用，跳过推送 equipmentId={}", fault.getEquipmentId());
            return;
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("eventId", UUID.randomUUID().toString());
            body.put("equipmentCode", fault.getEquipmentId());
            body.put("equipmentName", fault.getEquipmentName());
            body.put("faultType", fault.getFaultType());
            body.put("faultDescription", fault.getFaultDescription());
            body.put("occurTime", fault.getOccurTime() != null ? fault.getOccurTime().toString() : null);
            eamFaultReportClient.reportEquipmentFault(body);
            log.info("MES->EAM 设备故障推送成功 equipmentId={} faultId={}", fault.getEquipmentId(), fault.getId());
        } catch (Exception e) {
            log.warn("MES->EAM 设备故障推送失败 equipmentId={} faultId={}，原因：{}",
                    fault.getEquipmentId(), fault.getId(), e.getMessage());
        }
    }

    /**
     * 将对象安全转换为字符串。
     *
     * @param value 原始值
     * @return 字符串表示，null 则返回 null
     */
    private String str(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 解析日期时间字符串（ISO 格式，如 2026-07-25T10:15:30），解析失败返回 null。
     *
     * @param value 原始值
     * @return 解析后的时间，无法解析时返回 null
     */
    private LocalDateTime parseDateTime(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(String.valueOf(value).trim().replace(' ', 'T'));
        } catch (Exception e) {
            return null;
        }
    }
}
