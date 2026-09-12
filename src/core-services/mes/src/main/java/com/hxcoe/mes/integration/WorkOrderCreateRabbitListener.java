package com.hxcoe.mes.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.dto.mes.WorkOrderCreateRequestDTO;
import com.hxcoe.common.integration.IntegrationTransportProperties;
import com.hxcoe.mes.service.WorkOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * MES 工单创建事件 RabbitMQ 消费者（RABBIT 模式消费端）。
 *
 * <p>监听 ERP 通过 Outbox 推送的生产单创建事件，反序列化后调用
 * {@link WorkOrderService#createWorkOrderFromErp} 创建工单。
 *
 * <p>仅在 hxcoe.integration.transport=RABBIT 时启用。
 * 队列名通过 SpEL 读取 {@link IntegrationTransportProperties#getRabbit()} 配置，
 * 与 {@code IntegrationTransportAutoConfiguration} 声明的队列 Bean 保持一致。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
public class WorkOrderCreateRabbitListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WorkOrderService workOrderService;

    /**
     * 消费工单创建事件。
     *
     * <p>消费失败时抛出异常触发 RabbitMQ 重试机制（默认自动 ACK 失败重回队列）。
     * 业务幂等由 {@link WorkOrderService#createWorkOrderFromErp} 按 erpProductionNo 保证。
     *
     * @param body 事件 JSON 字符串
     */
    @RabbitListener(queues = "#{@integrationTransportProperties.rabbit.queueMesWorkOrderCreate}")
    public void onMessage(String body) {
        if (body == null || body.isBlank()) {
            return;
        }
        try {
            WorkOrderCreateRequestDTO req = objectMapper.readValue(body, WorkOrderCreateRequestDTO.class);
            if (req == null || req.getWorkOrder() == null) {
                log.warn("MES Rabbit 消费工单创建事件：载荷为空，丢弃 body={}", body);
                return;
            }
            workOrderService.createWorkOrderFromErp(req);
        } catch (Exception ex) {
            log.warn("MES Rabbit 消费工单创建事件失败 error={}", ex.getMessage());
            throw new IllegalStateException(ex);
        }
    }
}
