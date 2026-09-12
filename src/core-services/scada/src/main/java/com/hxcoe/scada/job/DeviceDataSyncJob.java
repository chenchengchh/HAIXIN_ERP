package com.hxcoe.scada.job;

import com.hxcoe.common.result.Result;
import com.hxcoe.scada.client.MesIntegrationClient;
import com.hxcoe.scada.entity.ScadaCollectPointEntity;
import com.hxcoe.scada.entity.ScadaDeviceEntity;
import com.hxcoe.scada.entity.ScadaTagValueEntity;
import com.hxcoe.scada.repository.ScadaCollectPointRepository;
import com.hxcoe.scada.repository.ScadaDeviceRepository;
import com.hxcoe.scada.repository.ScadaTagValueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 设备工艺参数同步定时任务
 * 定时将SCADA工艺类点位（温度/压力/流量/液位等非能源类）最新值推送给MES，
 * 形成 SCADA→MES 设备实时参数闭环，供MES数据采集与监控页面展示。
 *
 * @author author
 * @date 2026-08-02
 */
@Component
public class DeviceDataSyncJob {

    private static final Logger log = LoggerFactory.getLogger(DeviceDataSyncJob.class);

    private final ScadaTagValueRepository scadaTagValueRepository;
    private final ScadaCollectPointRepository scadaCollectPointRepository;
    private final ScadaDeviceRepository scadaDeviceRepository;
    private final MesIntegrationClient mesIntegrationClient;

    @Autowired
    public DeviceDataSyncJob(ScadaTagValueRepository scadaTagValueRepository,
                             ScadaCollectPointRepository scadaCollectPointRepository,
                             ScadaDeviceRepository scadaDeviceRepository,
                             @Autowired(required = false) MesIntegrationClient mesIntegrationClient) {
        this.scadaTagValueRepository = scadaTagValueRepository;
        this.scadaCollectPointRepository = scadaCollectPointRepository;
        this.scadaDeviceRepository = scadaDeviceRepository;
        this.mesIntegrationClient = mesIntegrationClient;
    }

    /**
     * 定时同步设备工艺参数到MES
     * 查询工艺类点位每个tagCode的最新一条记录，结合采集点与设备信息组装批量请求推送
     */
    @Scheduled(fixedDelayString = "${scada.integration.mes.sync-delay-ms:60000}",
            initialDelayString = "${scada.integration.mes.initial-delay-ms:45000}")
    public void syncDeviceDataToMes() {
        if (mesIntegrationClient == null) {
            log.warn("MES集成客户端不可用，跳过本次设备参数同步");
            return;
        }

        // 采集点：tagCode -> 采集点实体（取设备名/参数中文名）
        Map<String, ScadaCollectPointEntity> pointByTag = scadaCollectPointRepository.findAll().stream()
                .filter(p -> StringUtils.hasText(p.getTagCode()))
                .collect(Collectors.toMap(ScadaCollectPointEntity::getTagCode, Function.identity(), (a, b) -> a));

        // 设备：设备名 -> 设备实体（取设备编码/类型）
        Map<String, ScadaDeviceEntity> deviceByName = scadaDeviceRepository.findAll().stream()
                .filter(d -> StringUtils.hasText(d.getDeviceName()))
                .collect(Collectors.toMap(ScadaDeviceEntity::getDeviceName, Function.identity(), (a, b) -> a));

        // 按tagCode分组取最新一条工艺类点位数据
        Map<String, ScadaTagValueEntity> latestByTag = new HashMap<>();
        for (ScadaTagValueEntity entity : scadaTagValueRepository.findAll()) {
            if (!isProcessTag(entity)) {
                continue;
            }
            latestByTag.merge(entity.getTagCode(), entity,
                    (a, b) -> b.getTs() != null && (a.getTs() == null || b.getTs().isAfter(a.getTs())) ? b : a);
        }

        if (latestByTag.isEmpty()) {
            return;
        }

        // 映射为MES推送数据项
        List<Map<String, Object>> items = new ArrayList<>();
        for (ScadaTagValueEntity entity : latestByTag.values()) {
            ScadaCollectPointEntity point = pointByTag.get(entity.getTagCode());
            ScadaDeviceEntity device = point != null && StringUtils.hasText(point.getDeviceName())
                    ? deviceByName.get(point.getDeviceName()) : null;

            Map<String, Object> item = new HashMap<>();
            // 优先使用SCADA设备编码作为MES设备ID，保证跨模块设备身份一致；无匹配设备时回退tagCode
            item.put("equipmentId", device != null && StringUtils.hasText(device.getDeviceCode())
                    ? device.getDeviceCode() : entity.getTagCode());
            item.put("equipmentName", point != null ? point.getDeviceName() : null);
            item.put("equipmentType", device != null ? device.getDeviceType() : null);
            item.put("parameterName", resolveParameterName(entity.getTagCode(), point));
            item.put("parameterValue", entity.getValue());
            item.put("unit", entity.getUnit());
            item.put("collectionTime", entity.getTs() != null
                    ? entity.getTs().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
            item.put("quality", entity.getQuality());
            items.add(item);
        }

        if (items.isEmpty()) {
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("eventId", UUID.randomUUID().toString());
        body.put("items", items);

        try {
            Result<Map<String, Object>> result = mesIntegrationClient.pushDeviceData(body);
            log.info("设备参数同步MES完成，eventId={}，结果={}", body.get("eventId"), result != null ? result.getData() : null);
        } catch (Exception e) {
            log.warn("设备参数同步MES失败：{}", e.getMessage());
        }
    }

    /**
     * 判断点位是否为工艺类点位（非能源类）
     * 排除能源类前缀（ENERGY_/POWER_/ELEC_）与能源单位（kWh/kW/kvar）
     *
     * @param entity 点位数据
     * @return true-工艺类点位
     */
    private boolean isProcessTag(ScadaTagValueEntity entity) {
        if (entity == null || !StringUtils.hasText(entity.getTagCode())) {
            return false;
        }
        String tagCode = entity.getTagCode();
        if (tagCode.startsWith("ENERGY_") || tagCode.startsWith("POWER_") || tagCode.startsWith("ELEC_")) {
            return false;
        }
        String unit = entity.getUnit();
        return !("kWh".equals(unit) || "kW".equals(unit) || "kvar".equals(unit));
    }

    /**
     * 解析参数中文名：优先采集点备注，其次按点位编码前缀映射
     *
     * @param tagCode 点位编码
     * @param point   采集点实体
     * @return 参数名
     */
    private String resolveParameterName(String tagCode, ScadaCollectPointEntity point) {
        if (point != null && StringUtils.hasText(point.getRemark())) {
            return point.getRemark();
        }
        if (tagCode == null) {
            return "unknown";
        }
        if (tagCode.startsWith("TEMP")) {
            return "温度";
        }
        if (tagCode.startsWith("PRESS")) {
            return "压力";
        }
        if (tagCode.startsWith("FLOW")) {
            return "流量";
        }
        if (tagCode.startsWith("LEVEL")) {
            return "液位";
        }
        return tagCode;
    }
}
