package com.hxcoe.eam.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.eam.entity.AssetEntity;
import com.hxcoe.eam.entity.FaultRecordEntity;
import com.hxcoe.eam.repository.AssetRepository;
import com.hxcoe.eam.repository.FaultRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * EAM 接收 MES 集成事件入口控制器（MES→EAM 方向闭环）。
 *
 * <p>对齐 MES 侧 {@code EamFaultReportClient} 的契约：
 * POST /api/v1/eam/integration/mes/equipment-fault。
 * MES 登记设备故障后推送至此，自动生成 EAM 故障记录以触发维修流程。
 */
@RestController
@RequestMapping({"/api/v1/eam/integration", "/eam/integration"})
public class EamIntegrationController {

    @Autowired
    private FaultRecordRepository faultRecordRepository;

    @Autowired
    private AssetRepository assetRepository;

    /**
     * 接收 MES 设备故障事件，生成 EAM 故障记录（状态 reported）。
     *
     * <p>幂等：FaultRecordEntity 无 eventId 字段，按 设备名称+上报时间+故障类型 组合查重，
     * 重复请求返回成功并标记 duplicated=true。
     *
     * @param body 事件体（eventId/equipmentCode/equipmentName/faultType/faultDescription/occurTime）
     * @return 处理结果（含 faultRecordId 与 duplicated 标记）
     */
    @PostMapping("/mes/equipment-fault")
    public Result<Map<String, Object>> receiveMesEquipmentFault(@RequestBody Map<String, Object> body) {
        String equipmentName = str(body == null ? null : body.get("equipmentName"));
        String faultType = str(body == null ? null : body.get("faultType"));
        String faultDescription = str(body == null ? null : body.get("faultDescription"));
        LocalDateTime reportTime = parseDateTime(body == null ? null : body.get("occurTime"));
        if (reportTime == null) {
            reportTime = LocalDateTime.now();
        }

        // 幂等查重：设备名称+上报时间+故障类型 组合唯一
        List<FaultRecordEntity> existing = faultRecordRepository
                .findByEquipmentNameAndReportTimeAndType(equipmentName, reportTime, faultType);
        if (!existing.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put("duplicated", true);
            data.put("faultRecordId", existing.get(0).getId());
            data.put("eventId", body.get("eventId"));
            return Result.success("故障记录已存在，幂等去重返回", data);
        }

        // 生成故障记录：上报人固定为 MES系统，初始状态 reported（触发维修流程）
        FaultRecordEntity record = new FaultRecordEntity();
        record.setEquipmentName(equipmentName);
        record.setType(faultType);
        record.setDescription(faultDescription);
        record.setReporter("MES系统");
        record.setReportTime(reportTime);
        record.setStatus("reported");
        // 若能按名称在资产表查到设备，则回填资产ID；查不到则留空
        if (equipmentName != null && !equipmentName.isBlank()) {
            Optional<AssetEntity> asset = assetRepository.findFirstByName(equipmentName);
            asset.ifPresent(a -> record.setEquipmentId(a.getId()));
        }
        faultRecordRepository.save(record);

        Map<String, Object> data = new HashMap<>();
        data.put("duplicated", false);
        data.put("faultRecordId", record.getId());
        data.put("eventId", body.get("eventId"));
        return Result.success("EAM 故障记录已生成", data);
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
