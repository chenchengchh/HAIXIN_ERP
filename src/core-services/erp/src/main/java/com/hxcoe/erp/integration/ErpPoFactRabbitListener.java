package com.hxcoe.erp.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.erp.dto.integration.PoFactRequest;
import com.hxcoe.erp.service.FinanceTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
public class ErpPoFactRabbitListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FinanceTaskService financeTaskService;

    @RabbitListener(queues = "#{@integrationTransportProperties.rabbit.queueErpPoFacts}")
    public void onMessage(String body) {
        if (body == null || body.isBlank()) {
            return;
        }
        try {
            PoFactRequest req = objectMapper.readValue(body, PoFactRequest.class);
            if (req == null || req.getFact() == null) {
                return;
            }
            financeTaskService.createFromPoFact(req);
        } catch (Exception ex) {
            log.warn("ERP Rabbit 消费 po-facts 失败 error={}", ex.getMessage());
            throw new IllegalStateException(ex);
        }
    }
}
