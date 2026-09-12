package com.hxcoe.erp.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.erp.entity.ErpEnergyCostEntity;
import com.hxcoe.erp.repository.ErpEnergyCostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ERP能源成本集成控制器（EMS→ERP 能源成本闭环）
 * 接收 EMS 定时汇总推送的能耗成本数据，按 eventId 幂等落库
 *
 * @author author
 * @date 2026-07-25
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/erp/integration", "/erp/integration"})
public class ErpEnergyIntegrationController {

    private final ErpEnergyCostRepository erpEnergyCostRepository;

    @Autowired
    public ErpEnergyIntegrationController(ErpEnergyCostRepository erpEnergyCostRepository) {
        this.erpEnergyCostRepository = erpEnergyCostRepository;
    }

    /**
     * 接收EMS能源成本推送
     * POST /api/v1/erp/integration/ems/energy-cost
     * 按 eventId 做幂等查重，已存在则标记 duplicated 直接返回
     *
     * @param body 推送请求体（eventId/period/energyType/area/consumption/unit/unitPrice/totalCost）
     * @return 接收结果（received/duplicated）
     */
    @PostMapping("/ems/energy-cost")
    public Result<Map<String, Object>> receiveEnergyCost(@RequestBody Map<String, Object> body) {
        // 必填字段校验：事件ID、统计周期、能源类型、区域、用量、单价、总成本
        String eventId = asString(body.get("eventId"));
        String period = asString(body.get("period"));
        String energyType = asString(body.get("energyType"));
        String area = asString(body.get("area"));
        BigDecimal consumption = asBigDecimal(body.get("consumption"));
        BigDecimal unitPrice = asBigDecimal(body.get("unitPrice"));
        BigDecimal totalCost = asBigDecimal(body.get("totalCost"));

        if (!StringUtils.hasText(eventId) || !StringUtils.hasText(period)
                || !StringUtils.hasText(energyType) || !StringUtils.hasText(area)
                || consumption == null || unitPrice == null || totalCost == null) {
            return Result.fail("能源成本推送参数不完整");
        }

        Map<String, Object> data = new HashMap<>();
        // 幂等查重：相同 eventId 已落库则跳过
        if (erpEnergyCostRepository.existsByEventId(eventId)) {
            log.info("能源成本事件已存在，跳过落库: eventId={}", eventId);
            data.put("received", 0);
            data.put("duplicated", true);
            return Result.success("事件已存在，幂等跳过", data);
        }

        ErpEnergyCostEntity entity = new ErpEnergyCostEntity();
        entity.setEventId(eventId);
        entity.setPeriod(period);
        entity.setEnergyType(energyType);
        entity.setArea(area);
        entity.setConsumption(consumption);
        entity.setUnit(asString(body.get("unit")));
        entity.setUnitPrice(unitPrice);
        entity.setTotalCost(totalCost);
        entity.setReceiveTime(LocalDateTime.now());
        erpEnergyCostRepository.save(entity);

        log.info("能源成本落库成功: eventId={}, period={}, energyType={}, area={}, totalCost={}",
                eventId, period, energyType, area, totalCost);
        data.put("received", 1);
        data.put("duplicated", false);
        return Result.success("接收成功", data);
    }

    /**
     * 将对象安全转换为字符串
     *
     * @param value 原始值
     * @return 字符串，null 时返回 null
     */
    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 将对象安全转换为 BigDecimal（兼容 Number 与字符串）
     *
     * @param value 原始值
     * @return BigDecimal，无法转换时返回 null
     */
    private BigDecimal asBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
