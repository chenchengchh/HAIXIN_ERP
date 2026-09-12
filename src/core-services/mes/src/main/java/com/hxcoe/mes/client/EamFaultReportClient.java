package com.hxcoe.mes.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * MES 调用 EAM 故障上报接口的 Feign 客户端（MES→EAM 方向闭环）。
 *
 * <p>MES 登记设备故障后，将故障事件推送到 EAM 侧
 * {@code EamIntegrationController} 的 /mes/equipment-fault 入口，自动生成 EAM 故障记录。
 */
@FeignClient(name = "eam-service", path = "/api/v1/eam/integration", contextId = "mesEamFaultReportClient")
public interface EamFaultReportClient {

    /**
     * 上报 MES 设备故障事件到 EAM。
     *
     * @param body 事件体（eventId/equipmentCode/equipmentName/faultType/faultDescription/occurTime）
     * @return EAM 侧处理结果
     */
    @PostMapping("/mes/equipment-fault")
    Result<Map<String, Object>> reportEquipmentFault(@RequestBody Map<String, Object> body);
}
