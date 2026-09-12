package com.hxcoe.srm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.srm.dto.integration.PurchaseOrderEventRequest;
import com.hxcoe.srm.service.PurchaseOrderMirrorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "hxcoe.integration", name = "transport", havingValue = "RABBIT")
public class ScmPoEventRabbitListener {

    private final ObjectMapper objectMapper;
    private final PurchaseOrderMirrorService purchaseOrderMirrorService;

    public ScmPoEventRabbitListener(ObjectMapper objectMapper, PurchaseOrderMirrorService purchaseOrderMirrorService) {
        this.objectMapper = objectMapper;
        this.purchaseOrderMirrorService = purchaseOrderMirrorService;
    }

    @RabbitListener(queues = "${hxcoe.integration.rabbit.queue-scm-po-events-srm:srm.po-events.v1}")
    public void onMessage(String body) {
        try {
            PurchaseOrderEventRequest req = objectMapper.readValue(body, PurchaseOrderEventRequest.class);
            purchaseOrderMirrorService.applyScmEvent(req);
        } catch (Exception ex) {
            log.warn("SRM 消费 SCM PO Rabbit 消息失败 error={}", ex.getMessage());
        }
    }
}

