package com.hxcoe.mes.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.mes.entity.EquipmentDataEntity;
import com.hxcoe.mes.repository.EquipmentDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * MES 接收 EAM 集成事件入口控制器（EAM→MES 方向闭环）。
 *
 * <p>对齐 EAM 侧 {@code MesEquipmentStatusClient} 的契约：
 * POST /api/v1/mes/integration/eam/equipment-status。
 * 用于生产侧感知 EAM 设备资产状态变更（如设备进入维修中）。
 */
@RestController
@RequestMapping({"/api/v1/mes/integration/eam", "/mes/integration/eam"})
public class MesEamIntegrationController {

    @Autowired
    private EquipmentDataRepository equipmentDataRepository;

    /**
     * 接收 EAM 设备资产状态变更事件，同步 MES 侧该设备最新状态。
     *
     * <p>按 assetCode 查询 mes_equipment_data 中最新记录：存在则更新其状态；
     * 不存在则新建一条资产状态记录（parameterName=asset_status）。
     *
     * @param body 事件体（eventId/assetCode/assetName/status/eventTime）
     * @return 处理结果（含处理方式 updated/created）
     */
    @PostMapping("/equipment-status")
    public Result<Map<String, Object>> receiveEquipmentStatus(@RequestBody Map<String, Object> body) {
        String assetCode = str(body == null ? null : body.get("assetCode"));
        String assetName = str(body == null ? null : body.get("assetName"));
        String status = str(body == null ? null : body.get("status"));
        if (assetCode == null || assetCode.isBlank()) {
            return Result.fail("assetCode 不能为空");
        }
        if (status == null || status.isBlank()) {
            return Result.fail("status 不能为空");
        }

        EquipmentDataEntity entity = equipmentDataRepository
                .findTopByEquipmentIdOrderByTimestampDesc(assetCode)
                .orElse(null);
        String action;
        if (entity != null) {
            // 已存在该设备数据：更新最新记录的状态为 EAM 推送的状态
            entity.setStatus(status);
            if (assetName != null && !assetName.isBlank()) {
                entity.setEquipmentName(assetName);
            }
            entity.setTimestamp(LocalDateTime.now());
            equipmentDataRepository.save(entity);
            action = "updated";
        } else {
            // 不存在该设备数据：新建一条资产状态记录
            entity = new EquipmentDataEntity();
            entity.setEquipmentId(assetCode);
            entity.setEquipmentName(assetName);
            entity.setStatus(status);
            entity.setParameterName("asset_status");
            entity.setTimestamp(LocalDateTime.now());
            equipmentDataRepository.save(entity);
            action = "created";
        }

        Map<String, Object> data = new HashMap<>();
        data.put("action", action);
        data.put("equipmentDataId", entity.getId());
        data.put("equipmentId", assetCode);
        data.put("status", status);
        data.put("eventId", body.get("eventId"));
        return Result.success("MES 已同步 EAM 设备状态（" + action + "）", data);
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
}
