package com.hxcoe.erp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.erp.dto.integration.PurchaseOrderEventRequest;
import com.hxcoe.erp.service.ErpPoSnapshotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "hxcoe.integration", name = "transport", havingValue = "RABBIT")
public class ScmPoEventRabbitListener {

    private final ObjectMapper objectMapper;
    private final ErpPoSnapshotService erpPoSnapshotService;

    public ScmPoEventRabbitListener(ObjectMapper objectMapper, ErpPoSnapshotService erpPoSnapshotService) {
        this.objectMapper = objectMapper;
        this.erpPoSnapshotService = erpPoSnapshotService;
    }

    @RabbitListener(queues = "${hxcoe.integration.rabbit.queue-scm-po-events-erp:erp.po-events.v1}")
    public void onMessage(String body) {
        try {
            PurchaseOrderEventRequest req = objectMapper.readValue(body, PurchaseOrderEventRequest.class);
            erpPoSnapshotService.applyScmPoEvent(req);
        } catch (Exception ex) {
            log.warn("ERP 消费 SCM PO Rabbit 消息失败 error={}", ex.getMessage());
        }
    }
}

