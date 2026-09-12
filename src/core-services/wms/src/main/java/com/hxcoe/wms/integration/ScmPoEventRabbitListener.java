package com.hxcoe.wms.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.wms.dto.integration.PurchaseOrderEventRequest;
import com.hxcoe.wms.service.WmsPoInstructionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "hxcoe.integration", name = "transport", havingValue = "RABBIT")
public class ScmPoEventRabbitListener {

    private final ObjectMapper objectMapper;
    private final WmsPoInstructionService wmsPoInstructionService;

    public ScmPoEventRabbitListener(ObjectMapper objectMapper, WmsPoInstructionService wmsPoInstructionService) {
        this.objectMapper = objectMapper;
        this.wmsPoInstructionService = wmsPoInstructionService;
    }

    @RabbitListener(queues = "${hxcoe.integration.rabbit.queue-scm-po-events-wms:wms.po-events.v1}")
    public void onMessage(String body) {
        try {
            PurchaseOrderEventRequest req = objectMapper.readValue(body, PurchaseOrderEventRequest.class);
            wmsPoInstructionService.applyScmEvent(req);
        } catch (Exception ex) {
            log.warn("WMS 消费 SCM PO Rabbit 消息失败 error={}", ex.getMessage());
        }
    }
}

