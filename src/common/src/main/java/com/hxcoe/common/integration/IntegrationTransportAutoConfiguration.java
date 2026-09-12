package com.hxcoe.common.integration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;

@AutoConfiguration
@EnableConfigurationProperties(IntegrationTransportProperties.class)
public class IntegrationTransportAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
    public TopicExchange hxcoeIntegrationExchange(IntegrationTransportProperties props) {
        return new TopicExchange(props.getRabbit().getExchange(), true, false);
    }

    @Bean
    @ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
    public Queue erpPoFactsQueue(IntegrationTransportProperties props) {
        return new Queue(props.getRabbit().getQueueErpPoFacts(), true);
    }

    @Bean
    @ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
    public Binding erpPoFactsBinding(TopicExchange hxcoeIntegrationExchange, Queue erpPoFactsQueue, IntegrationTransportProperties props) {
        return BindingBuilder.bind(erpPoFactsQueue)
                .to(hxcoeIntegrationExchange)
                .with(props.getRabbit().getRoutingKeyErpPoFacts());
    }

    @Bean
    @ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
    public Queue scmPoEventsSrmQueue(IntegrationTransportProperties props) {
        return new Queue(props.getRabbit().getQueueScmPoEventsSrm(), true);
    }

    @Bean
    @ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
    public Binding scmPoEventsSrmBinding(TopicExchange hxcoeIntegrationExchange, Queue scmPoEventsSrmQueue, IntegrationTransportProperties props) {
        return BindingBuilder.bind(scmPoEventsSrmQueue)
                .to(hxcoeIntegrationExchange)
                .with(props.getRabbit().getRoutingKeyScmPoEvents());
    }

    @Bean
    @ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
    public Queue scmPoEventsWmsQueue(IntegrationTransportProperties props) {
        return new Queue(props.getRabbit().getQueueScmPoEventsWms(), true);
    }

    @Bean
    @ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
    public Binding scmPoEventsWmsBinding(TopicExchange hxcoeIntegrationExchange, Queue scmPoEventsWmsQueue, IntegrationTransportProperties props) {
        return BindingBuilder.bind(scmPoEventsWmsQueue)
                .to(hxcoeIntegrationExchange)
                .with(props.getRabbit().getRoutingKeyScmPoEvents());
    }

    @Bean
    @ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
    public Queue scmPoEventsErpQueue(IntegrationTransportProperties props) {
        return new Queue(props.getRabbit().getQueueScmPoEventsErp(), true);
    }

    @Bean
    @ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
    public Binding scmPoEventsErpBinding(TopicExchange hxcoeIntegrationExchange, Queue scmPoEventsErpQueue, IntegrationTransportProperties props) {
        return BindingBuilder.bind(scmPoEventsErpQueue)
                .to(hxcoeIntegrationExchange)
                .with(props.getRabbit().getRoutingKeyScmPoEvents());
    }

    /**
     * MES 工单创建队列（ERP→MES，B1 事件）。
     */
    @Bean
    @ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
    public Queue mesWorkOrderCreateQueue(IntegrationTransportProperties props) {
        return new Queue(props.getRabbit().getQueueMesWorkOrderCreate(), true);
    }

    @Bean
    @ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
    public Binding mesWorkOrderCreateBinding(TopicExchange hxcoeIntegrationExchange, Queue mesWorkOrderCreateQueue, IntegrationTransportProperties props) {
        return BindingBuilder.bind(mesWorkOrderCreateQueue)
                .to(hxcoeIntegrationExchange)
                .with(props.getRabbit().getRoutingKeyMesWorkOrderCreate());
    }

    /**
     * MES 工单完工事件队列-SCM 侧（MES→SCM，B6 事件）。
     */
    @Bean
    @ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
    public Queue mesWorkOrderCompletionScmQueue(IntegrationTransportProperties props) {
        return new Queue(props.getRabbit().getQueueMesWorkOrderCompletionScm(), true);
    }

    @Bean
    @ConditionalOnProperty(name = "hxcoe.integration.transport", havingValue = "RABBIT")
    public Binding mesWorkOrderCompletionScmBinding(TopicExchange hxcoeIntegrationExchange, Queue mesWorkOrderCompletionScmQueue, IntegrationTransportProperties props) {
        return BindingBuilder.bind(mesWorkOrderCompletionScmQueue)
                .to(hxcoeIntegrationExchange)
                .with(props.getRabbit().getRoutingKeyMesWorkOrderCompletion());
    }
}
