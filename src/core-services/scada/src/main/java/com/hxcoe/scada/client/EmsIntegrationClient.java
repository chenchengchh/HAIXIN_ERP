package com.hxcoe.scada.client;

import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * EMS集成服务Feign客户端
 * 用于SCADA向EMS推送能源数据
 *
 * @author author
 * @date 2026-07-25
 */
@FeignClient(name = "ems-service", path = "/api/v1/ems/integration", contextId = "scadaEmsIntegrationClient")
public interface EmsIntegrationClient {

    /**
     * 推送能源数据到EMS
     *
     * @param body 推送请求体（eventId + items）
     * @return 接收与跳过条数统计
     */
    @PostMapping("/scada/energy-data")
    Result<Map<String, Object>> pushEnergyData(@RequestBody Map<String, Object> body);
}
