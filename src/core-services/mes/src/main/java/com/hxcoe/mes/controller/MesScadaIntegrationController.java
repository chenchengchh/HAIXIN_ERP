package com.hxcoe.mes.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.mes.entity.EquipmentDataEntity;
import com.hxcoe.mes.repository.EquipmentDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MES 接收 SCADA 集成事件入口控制器（SCADA→MES 方向闭环）。
 *
 * <p>对齐 SCADA 侧 {@code MesIntegrationClient} 的契约：
 * POST /api/v1/mes/integration/scada/device-data。
 * 用于生产侧感知 SCADA 采集的设备实时工艺参数（温度/压力/流量/液位等），
 * 写入 mes_equipment_data 表，供 MES 数据采集与监控页面展示。
 */
@RestController
@RequestMapping({"/api/v1/mes/integration/scada", "/mes/integration/scada"})
public class MesScadaIntegrationController {

    @Autowired
    private EquipmentDataRepository equipmentDataRepository;

    /**
     * 接收 SCADA 设备实时参数批量推送，逐条落库 mes_equipment_data。
     *
     * <p>请求体：{eventId, items:[{equipmentId, equipmentName, equipmentType,
     * parameterName, parameterValue, unit, collectionTime, quality}]}
     *
     * @param body 推送请求体
     * @return 接收与跳过条数统计
     */
    @PostMapping("/device-data")
    public Result<Map<String, Object>> receiveDeviceData(@RequestBody Map<String, Object> body) {
        Object itemsObj = body == null ? null : body.get("items");
        if (!(itemsObj instanceof List)) {
            return Result.fail("items 不能为空且必须为数组");
        }
        List<?> items = (List<?>) itemsObj;
        int received = 0;
        int skipped = 0;
        List<EquipmentDataEntity> toSave = new ArrayList<>();
        for (Object o : items) {
            if (!(o instanceof Map)) {
                skipped++;
                continue;
            }
            Map<?, ?> item = (Map<?, ?>) o;
            String equipmentId = str(item.get("equipmentId"));
            String parameterName = str(item.get("parameterName"));
            if (equipmentId == null || equipmentId.isBlank()
                    || parameterName == null || parameterName.isBlank()) {
                skipped++;
                continue;
            }

            EquipmentDataEntity entity = new EquipmentDataEntity();
            entity.setEquipmentId(equipmentId);
            entity.setEquipmentName(str(item.get("equipmentName")));
            entity.setEquipmentType(str(item.get("equipmentType")));
            entity.setParameterName(parameterName);
            entity.setParameterValue(toBigDecimal(item.get("parameterValue")));
            entity.setUnit(str(item.get("unit")));
            entity.setTimestamp(parseTime(str(item.get("collectionTime"))));
            entity.setStatus(str(item.get("quality")) != null ? str(item.get("quality")) : "good");
            toSave.add(entity);
            received++;
        }
        if (!toSave.isEmpty()) {
            equipmentDataRepository.saveAll(toSave);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("received", received);
        data.put("skipped", skipped);
        data.put("eventId", body.get("eventId"));
        return Result.success("MES 已接收 SCADA 设备参数", data);
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
     * 将对象安全转换为 BigDecimal。
     *
     * @param value 原始值
     * @return BigDecimal 表示，无法转换则返回 null
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 容错解析时间：优先 ISO 格式，失败回退 "yyyy-MM-dd HH:mm:ss"，均失败取当前时间。
     *
     * @param text 时间字符串
     * @return 解析后的 LocalDateTime
     */
    private LocalDateTime parseTime(String text) {
        if (text == null || text.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(text.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(text.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ex) {
                return LocalDateTime.now();
            }
        }
    }
}
