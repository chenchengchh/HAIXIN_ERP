package com.hxcoe.scada.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * MES集成服务Feign客户端
 * 用于SCADA向MES推送设备实时工艺参数（温度/压力/流量/液位等）
 *
 * @author author
 * @date 2026-08-02
 */
@FeignClient(name = "mes-service", path = "/api/v1/mes/integration/scada", contextId = "scadaMesIntegrationClient")
public interface MesIntegrationClient {

    /**
     * 推送设备实时参数到MES
     *
     * @param body 推送请求体（eventId + items）
     * @return 接收与跳过条数统计
     */
    @PostMapping("/device-data")
    Result<Map<String, Object>> pushDeviceData(@RequestBody Map<String, Object> body);
}
