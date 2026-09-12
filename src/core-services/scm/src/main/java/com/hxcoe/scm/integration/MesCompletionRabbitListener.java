package com.hxcoe.scm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.dto.scm.MesCompletionFactDTO;
import com.hxcoe.common.integration.IntegrationTransportProperties;
import com.hxcoe.scm.service.MesCompletionFactService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * SCM 消费 MES 工单完工事件 RabbitMQ 监听器（B6 闭环，RABBIT 模式消费端）。
 *
 * <p>仅在 hxcoe.integration.transport=RABBIT 时启用。
 * 监听队列 mes.work-order-completion.v1（SCM 侧），与 MES 推送端的路由键一致。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
public class MesCompletionRabbitListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MesCompletionFactService mesCompletionFactService;

    /**
     * 消费 MES 完工事件。
     *
     * <p>消费失败时抛出异常触发 RabbitMQ 重试；业务幂等由 Service 层保证。
     *
     * @param body 事件 JSON 字符串
     */
    @RabbitListener(queues = "#{@integrationTransportProperties.rabbit.queueMesWorkOrderCompletionScm}")
    public void onMessage(String body) {
        if (body == null || body.isBlank()) {
            return;
        }
        try {
            MesCompletionFactDTO req = objectMapper.readValue(body, MesCompletionFactDTO.class);
            if (req == null || req.getErpProductionNo() == null) {
                log.warn("SCM Rabbit 消费 MES 完工事件：载荷为空，丢弃 body={}", body);
                return;
            }
            mesCompletionFactService.applyMesCompletion(req);
        } catch (Exception ex) {
            log.warn("SCM Rabbit 消费 MES 完工事件失败 error={}", ex.getMessage());
            throw new IllegalStateException(ex);
        }
    }
}
