package com.hxcoe.ems.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * EMS 调用 ERP 能源成本集成接口的 Feign 客户端（EMS→ERP 能源成本闭环）。
 *
 * <p>EMS 定时汇总昨日能耗数据后，将能耗成本推送到 ERP 侧
 * {@code ErpEnergyIntegrationController} 的 /ems/energy-cost 入口。
 */
@FeignClient(name = "erp-service", path = "/api/v1/erp/integration", contextId = "emsErpEnergyCostClient")
public interface ErpEnergyCostClient {

    /**
     * 推送能源成本数据到 ERP。
     *
     * @param body 事件体（eventId/period/energyType/area/consumption/unit/unitPrice/totalCost）
     * @return ERP 侧处理结果（received/duplicated）
     */
    @PostMapping("/ems/energy-cost")
    Result<Map<String, Object>> pushEnergyCost(@RequestBody Map<String, Object> body);
}
