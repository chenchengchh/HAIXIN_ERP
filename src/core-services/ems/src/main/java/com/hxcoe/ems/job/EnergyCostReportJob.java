package com.hxcoe.ems.job;

import com.hxcoe.common.result.Result;
import com.hxcoe.ems.client.ErpEnergyCostClient;
import com.hxcoe.ems.entity.RealTimeDataEntity;
import com.hxcoe.ems.repository.RealTimeDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 能源成本定时汇总推送任务（EMS→ERP 能源成本闭环）
 * 每日凌晨2点按 能源类型+区域 分组汇总昨日能耗，按配置单价计算成本后推送 ERP
 *
 * @author author
 * @date 2026-07-25
 */
@Slf4j
@Component
public class EnergyCostReportJob {

    /**
     * 默认能源单价（元/单位），配置缺失或未命中时使用
     */
    private static final double DEFAULT_UNIT_PRICE = 0.85;

    /**
     * 中文能源类型到单价配置键的别名映射
     */
    private static final Map<String, String> ENERGY_TYPE_ALIAS = Map.of(
            "电力", "power",
            "水", "water",
            "燃气", "gas",
            "热能", "heat"
    );

    private final RealTimeDataRepository realTimeDataRepository;

    /**
     * ERP 能源成本推送客户端（可选注入，服务不可用时仅记录告警）
     */
    @Autowired(required = false)
    private ErpEnergyCostClient erpEnergyCostClient;

    /**
     * 能源单价配置，格式：power:0.85,water:4.0,gas:3.5
     */
    @Value("${ems.integration.erp.unit-price-map:power:0.85,water:4.0,gas:3.5}")
    private String unitPriceMapConfig;

    @Autowired
    public EnergyCostReportJob(RealTimeDataRepository realTimeDataRepository) {
        this.realTimeDataRepository = realTimeDataRepository;
    }

    /**
     * 每日凌晨2点定时执行昨日能源成本汇总推送（cron 可配置）
     */
    @Scheduled(cron = "${ems.integration.erp.cost-cron:0 0 2 * * ?}")
    public void reportYesterdayCost() {
        Map<String, Object> summary = reportCostForDate(LocalDate.now().minusDays(1));
        log.info("能源成本定时推送完成: {}", summary);
    }

    /**
     * 手动触发昨日能源成本汇总推送（供集成控制器调用，便于测试）
     *
     * @return 推送统计结果（period/groups/pushed/failed）
     */
    public Map<String, Object> reportNow() {
        return reportCostForDate(LocalDate.now().minusDays(1));
    }

    /**
     * 汇总指定日期的能耗数据并逐组推送 ERP
     *
     * @param date 统计日期
     * @return 推送统计结果（period/groups/pushed/failed）
     */
    public Map<String, Object> reportCostForDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        List<RealTimeDataEntity> dataList = realTimeDataRepository.findByCollectionTimeBetween(start, end);

        // 按 能源类型+区域 分组汇总采集值
        Map<String, BigDecimal> consumptionByGroup = new LinkedHashMap<>();
        Map<String, String> unitByGroup = new HashMap<>();
        for (RealTimeDataEntity item : dataList) {
            if (item.getActualValue() == null) {
                continue;
            }
            String key = item.getEnergyType() + "|" + item.getArea();
            consumptionByGroup.merge(key, BigDecimal.valueOf(item.getActualValue()), BigDecimal::add);
            unitByGroup.putIfAbsent(key, item.getUnit());
        }

        Map<String, Double> priceMap = parseUnitPriceMap();
        String period = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        int pushed = 0;
        int failed = 0;
        for (Map.Entry<String, BigDecimal> entry : consumptionByGroup.entrySet()) {
            String[] parts = entry.getKey().split("\\|", 2);
            String energyType = parts[0];
            String area = parts.length > 1 ? parts[1] : "";
            BigDecimal consumption = entry.getValue();
            double unitPrice = resolveUnitPrice(priceMap, energyType);
            BigDecimal totalCost = consumption.multiply(BigDecimal.valueOf(unitPrice))
                    .setScale(2, RoundingMode.HALF_UP);

            // 组装推送事件体
            Map<String, Object> body = new HashMap<>();
            body.put("eventId", UUID.randomUUID().toString());
            body.put("period", period);
            body.put("energyType", energyType);
            body.put("area", area);
            body.put("consumption", consumption.setScale(4, RoundingMode.HALF_UP));
            body.put("unit", unitByGroup.getOrDefault(entry.getKey(), ""));
            body.put("unitPrice", unitPrice);
            body.put("totalCost", totalCost);

            if (pushToErp(body)) {
                pushed++;
            } else {
                failed++;
            }
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("period", period);
        summary.put("groups", consumptionByGroup.size());
        summary.put("pushed", pushed);
        summary.put("failed", failed);
        return summary;
    }

    /**
     * 推送单组能源成本到 ERP，失败仅记录告警不抛异常
     *
     * @param body 推送事件体
     * @return true-推送成功
     */
    private boolean pushToErp(Map<String, Object> body) {
        if (erpEnergyCostClient == null) {
            log.warn("ERP能源成本客户端不可用，跳过推送: {}", body);
            return false;
        }
        try {
            Result<Map<String, Object>> result = erpEnergyCostClient.pushEnergyCost(body);
            if (result == null || result.getCode() == null || result.getCode() != 0) {
                log.warn("ERP能源成本推送返回失败: eventId={}, msg={}",
                        body.get("eventId"), result == null ? null : result.getMsg());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("ERP能源成本推送异常: eventId={}, error={}", body.get("eventId"), e.getMessage());
            return false;
        }
    }

    /**
     * 解析单价配置字符串为映射表
     *
     * @return 能源类型键 -> 单价
     */
    private Map<String, Double> parseUnitPriceMap() {
        Map<String, Double> priceMap = new HashMap<>();
        if (unitPriceMapConfig == null || unitPriceMapConfig.isBlank()) {
            return priceMap;
        }
        for (String pair : unitPriceMapConfig.split(",")) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                try {
                    priceMap.put(kv[0].trim(), Double.parseDouble(kv[1].trim()));
                } catch (NumberFormatException e) {
                    log.warn("能源单价配置项解析失败，已忽略: {}", pair);
                }
            }
        }
        return priceMap;
    }

    /**
     * 按能源类型解析单价：先按原值精确匹配，再按中文别名匹配，最后回退默认单价
     *
     * @param priceMap   单价配置表
     * @param energyType 能源类型
     * @return 单价（元/单位）
     */
    private double resolveUnitPrice(Map<String, Double> priceMap, String energyType) {
        Double price = priceMap.get(energyType);
        if (price == null) {
            String alias = ENERGY_TYPE_ALIAS.get(energyType);
            if (alias != null) {
                price = priceMap.get(alias);
            }
        }
        return price != null ? price : DEFAULT_UNIT_PRICE;
    }
}
