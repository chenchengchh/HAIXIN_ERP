package com.hxcoe.scada.job;

import com.hxcoe.common.result.Result;
import com.hxcoe.scada.client.EmsIntegrationClient;
import com.hxcoe.scada.entity.ScadaTagValueEntity;
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

/**
 * 能源数据同步定时任务
 * 定时将SCADA能源类点位最新值推送给EMS
 *
 * @author author
 * @date 2026-07-25
 */
@Component
public class EnergyDataSyncJob {

    private static final Logger log = LoggerFactory.getLogger(EnergyDataSyncJob.class);

    /**
     * 默认采集区域
     */
    private static final String DEFAULT_AREA = "SCADA采集";

    private final ScadaTagValueRepository scadaTagValueRepository;
    private final EmsIntegrationClient emsIntegrationClient;

    @Autowired
    public EnergyDataSyncJob(ScadaTagValueRepository scadaTagValueRepository,
                             @Autowired(required = false) EmsIntegrationClient emsIntegrationClient) {
        this.scadaTagValueRepository = scadaTagValueRepository;
        this.emsIntegrationClient = emsIntegrationClient;
    }

    /**
     * 定时同步能源数据到EMS
     * 查询能源类点位每个tagCode的最新一条记录，组装批量请求推送
     */
    @Scheduled(fixedDelayString = "${scada.integration.ems.sync-delay-ms:60000}",
            initialDelayString = "${scada.integration.ems.initial-delay-ms:30000}")
    public void syncEnergyDataToEms() {
        if (emsIntegrationClient == null) {
            log.warn("EMS集成客户端不可用，跳过本次能源数据同步");
            return;
        }

        // 按tagCode分组取最新一条能源类点位数据
        Map<String, ScadaTagValueEntity> latestByTag = new HashMap<>();
        for (ScadaTagValueEntity entity : scadaTagValueRepository.findAll()) {
            if (!isEnergyTag(entity)) {
                continue;
            }
            latestByTag.merge(entity.getTagCode(), entity,
                    (a, b) -> b.getTs() != null && (a.getTs() == null || b.getTs().isAfter(a.getTs())) ? b : a);
        }

        if (latestByTag.isEmpty()) {
            return;
        }

        // 映射为EMS推送数据项
        List<Map<String, Object>> items = new ArrayList<>();
        for (ScadaTagValueEntity entity : latestByTag.values()) {
            Map<String, Object> item = new HashMap<>();
            item.put("energyType", resolveEnergyType(entity.getTagCode()));
            item.put("area", DEFAULT_AREA);
            item.put("actualValue", entity.getValue());
            item.put("unit", entity.getUnit());
            item.put("collectionTime", entity.getTs() != null
                    ? entity.getTs().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
            item.put("deviceCode", entity.getTagCode());
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
            Result<Map<String, Object>> result = emsIntegrationClient.pushEnergyData(body);
            log.info("能源数据同步EMS完成，eventId={}，结果={}", body.get("eventId"), result != null ? result.getData() : null);
        } catch (Exception e) {
            log.warn("能源数据同步EMS失败：{}", e.getMessage());
        }
    }

    /**
     * 判断点位是否为能源类点位
     * tagCode以ENERGY_/POWER_/ELEC_开头，或单位为kWh/kW/kvar
     *
     * @param entity 点位数据
     * @return true-能源类点位
     */
    private boolean isEnergyTag(ScadaTagValueEntity entity) {
        if (entity == null || !StringUtils.hasText(entity.getTagCode())) {
            return false;
        }
        String tagCode = entity.getTagCode();
        if (tagCode.startsWith("ENERGY_") || tagCode.startsWith("POWER_") || tagCode.startsWith("ELEC_")) {
            return true;
        }
        String unit = entity.getUnit();
        return "kWh".equals(unit) || "kW".equals(unit) || "kvar".equals(unit);
    }

    /**
     * 根据点位编码前缀映射能源类型
     *
     * @param tagCode 点位编码
     * @return 能源类型
     */
    private String resolveEnergyType(String tagCode) {
        if (tagCode == null) {
            return "电力";
        }
        if (tagCode.startsWith("POWER_") || tagCode.startsWith("ELEC_")) {
            return "电力";
        }
        if (tagCode.startsWith("ENERGY_W")) {
            return "水";
        }
        if (tagCode.startsWith("ENERGY_G")) {
            return "燃气";
        }
        return "电力";
    }
}
