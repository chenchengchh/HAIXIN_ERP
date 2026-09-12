package com.hxcoe.erp.client;

import com.hxcoe.common.dto.mes.WorkOrderCreateRequestDTO;
import com.hxcoe.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * ERP 调用 MES 集成接口的 Feign 客户端（HTTP 模式时使用）。
 *
 * <p>用于 ERP 创建生产单后通过 Outbox 异步触发 MES 工单创建。
 * 当 hxcoe.integration.transport=HTTP 时由 MesWorkOrderOutboxService 调用。
 */
@FeignClient(name = "mes-service", path = "/api/v1/mes/integration", contextId = "erpMesIntegrationClient")
public interface MesIntegrationClient {

    /**
     * 接收 ERP 生产单创建事件，MES 侧据此创建工单。
     *
     * @param req 工单创建请求 DTO
     * @return 处理结果
     */
    @PostMapping("/erp/production-created")
    Result<Map<String, Object>> receiveProductionCreated(@RequestBody WorkOrderCreateRequestDTO req);
}
