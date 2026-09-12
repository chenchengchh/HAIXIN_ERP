package com.hxcoe.eam.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * EAM 调用 MES 设备状态同步接口的 Feign 客户端（EAM→MES 方向闭环）。
 *
 * <p>EAM 设备资产状态变更后，将状态事件推送到 MES 侧
 * {@code MesEamIntegrationController} 的 /equipment-status 入口。
 */
@FeignClient(name = "mes-service", path = "/api/v1/mes/integration/eam", contextId = "eamMesEquipmentStatusClient")
public interface MesEquipmentStatusClient {

    /**
     * 推送 EAM 设备资产状态变更事件到 MES。
     *
     * @param body 事件体（eventId/assetCode/assetName/status/eventTime）
     * @return MES 侧处理结果
     */
    @PostMapping("/equipment-status")
    Result<Map<String, Object>> pushEquipmentStatus(@RequestBody Map<String, Object> body);
}
