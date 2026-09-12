package com.hxcoe.mes.controller;

import com.hxcoe.common.dto.mes.WorkOrderCreateRequestDTO;
import com.hxcoe.common.result.Result;
import com.hxcoe.mes.entity.WorkOrderEntity;
import com.hxcoe.mes.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * MES 集成入口控制器（HTTP 模式消费端）。
 *
 * <p>对齐 ERP 侧 {@code MesIntegrationClient} 的契约：
 * POST /api/v1/mes/integration/erp/production-created。
 *
 * <p>当 hxcoe.integration.transport=HTTP 时，ERP 的 Outbox RetryJob 通过 Feign 直推此入口；
 * 当 transport=RABBIT/REDIS_STREAM 时，此入口仍保留，便于人工补推或排查。
 */
@RestController
@RequestMapping({"/api/v1/mes/integration", "/mes/integration"})
public class MesIntegrationController {

    @Autowired
    private WorkOrderService workOrderService;

    /**
     * 接收 ERP 生产单创建事件，MES 侧创建工单。
     *
     * @param req 工单创建请求 DTO
     * @return 处理结果（含工单号、ERP 生产单号、事件ID）
     */
    @PostMapping("/erp/production-created")
    public Result<Map<String, Object>> receiveProductionCreated(@RequestBody WorkOrderCreateRequestDTO req) {
        WorkOrderEntity entity = workOrderService.createWorkOrderFromErp(req);
        Map<String, Object> data = new HashMap<>();
        data.put("workOrderNo", entity.getWorkOrderNo());
        data.put("erpProductionNo", entity.getErpProductionNo());
        data.put("workOrderId", entity.getId());
        data.put("status", entity.getStatus());
        data.put("eventId", req == null ? null : req.getEventId());
        data.put("idempotencyKey", req == null ? null : req.getIdempotencyKey());
        return Result.success("MES 工单创建成功", data);
    }
}
