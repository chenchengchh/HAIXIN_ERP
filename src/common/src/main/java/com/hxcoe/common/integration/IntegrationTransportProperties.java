package com.hxcoe.common.integration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hxcoe.integration")
public class IntegrationTransportProperties {
    private Transport transport = Transport.HTTP;

    private Rabbit rabbit = new Rabbit();
    private Redis redis = new Redis();

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

    public Rabbit getRabbit() {
        return rabbit;
    }

    public void setRabbit(Rabbit rabbit) {
        this.rabbit = rabbit;
    }

    public Redis getRedis() {
        return redis;
    }

    public void setRedis(Redis redis) {
        this.redis = redis;
    }

    public enum Transport {
        HTTP,
        RABBIT,
        REDIS_STREAM
    }

    public static class Rabbit {
        private String exchange = "hxcoe.integration";
        private String routingKeyErpPoFacts = "erp.po-facts";
        private String queueErpPoFacts = "erp.po-facts";

        private String routingKeyScmPoEvents = "scm.po-events.v1";
        private String queueScmPoEvents = "scm.po-events.v1";
        private String queueScmPoEventsSrm = "srm.po-events.v1";
        private String queueScmPoEventsWms = "wms.po-events.v1";
        private String queueScmPoEventsErp = "erp.po-events.v1";

        // MES 工单创建事件（ERP→MES）
        private String routingKeyMesWorkOrderCreate = "mes.work-order-create.v1";
        private String queueMesWorkOrderCreate = "mes.work-order-create.v1";

        // MES 工单完工事件（MES→ERP/SCM）
        private String routingKeyMesWorkOrderCompletion = "mes.work-order-completion.v1";
        private String queueMesWorkOrderCompletionErp = "erp.mes-work-order-completion.v1";
        private String queueMesWorkOrderCompletionScm = "scm.mes-work-order-completion.v1";

        public String getRoutingKeyMesWorkOrderCreate() {
            return routingKeyMesWorkOrderCreate;
        }

        public void setRoutingKeyMesWorkOrderCreate(String routingKeyMesWorkOrderCreate) {
            this.routingKeyMesWorkOrderCreate = routingKeyMesWorkOrderCreate;
        }

        public String getQueueMesWorkOrderCreate() {
            return queueMesWorkOrderCreate;
        }

        public void setQueueMesWorkOrderCreate(String queueMesWorkOrderCreate) {
            this.queueMesWorkOrderCreate = queueMesWorkOrderCreate;
        }

        public String getRoutingKeyMesWorkOrderCompletion() {
            return routingKeyMesWorkOrderCompletion;
        }

        public void setRoutingKeyMesWorkOrderCompletion(String routingKeyMesWorkOrderCompletion) {
            this.routingKeyMesWorkOrderCompletion = routingKeyMesWorkOrderCompletion;
        }

        public String getQueueMesWorkOrderCompletionErp() {
            return queueMesWorkOrderCompletionErp;
        }

        public void setQueueMesWorkOrderCompletionErp(String queueMesWorkOrderCompletionErp) {
            this.queueMesWorkOrderCompletionErp = queueMesWorkOrderCompletionErp;
        }

        public String getQueueMesWorkOrderCompletionScm() {
            return queueMesWorkOrderCompletionScm;
        }

        public void setQueueMesWorkOrderCompletionScm(String queueMesWorkOrderCompletionScm) {
            this.queueMesWorkOrderCompletionScm = queueMesWorkOrderCompletionScm;
        }

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getRoutingKeyErpPoFacts() {
            return routingKeyErpPoFacts;
        }

        public void setRoutingKeyErpPoFacts(String routingKeyErpPoFacts) {
            this.routingKeyErpPoFacts = routingKeyErpPoFacts;
        }

        public String getQueueErpPoFacts() {
            return queueErpPoFacts;
        }

        public void setQueueErpPoFacts(String queueErpPoFacts) {
            this.queueErpPoFacts = queueErpPoFacts;
        }

        public String getRoutingKeyScmPoEvents() {
            return routingKeyScmPoEvents;
        }

        public void setRoutingKeyScmPoEvents(String routingKeyScmPoEvents) {
            this.routingKeyScmPoEvents = routingKeyScmPoEvents;
        }

        public String getQueueScmPoEvents() {
            return queueScmPoEvents;
        }

        public void setQueueScmPoEvents(String queueScmPoEvents) {
            this.queueScmPoEvents = queueScmPoEvents;
        }

        public String getQueueScmPoEventsSrm() {
            return queueScmPoEventsSrm;
        }

        public void setQueueScmPoEventsSrm(String queueScmPoEventsSrm) {
            this.queueScmPoEventsSrm = queueScmPoEventsSrm;
        }

        public String getQueueScmPoEventsWms() {
            return queueScmPoEventsWms;
        }

        public void setQueueScmPoEventsWms(String queueScmPoEventsWms) {
            this.queueScmPoEventsWms = queueScmPoEventsWms;
        }

        public String getQueueScmPoEventsErp() {
            return queueScmPoEventsErp;
        }

        public void setQueueScmPoEventsErp(String queueScmPoEventsErp) {
            this.queueScmPoEventsErp = queueScmPoEventsErp;
        }
    }

    public static class Redis {
        private String streamErpPoFacts = "hxcoe:integration:erp.po-facts";
        private String groupErpPoFacts = "erp-service";
        private String consumerErpPoFacts = "erp-1";

        private String streamScmPoEvents = "hxcoe:integration:scm.po-events.v1";
        private String groupScmPoEvents = "srm-service";
        private String consumerScmPoEvents = "srm-1";

        private String streamWmsWarehouseEvents = "hxcoe:integration:wms.warehouse-events.v1";
        private String groupWmsWarehouseEvents = "scm-service";
        private String consumerWmsWarehouseEvents = "scm-1";

        private String streamWmsLocationEvents = "hxcoe:integration:wms.location-events.v1";
        private String groupWmsLocationEvents = "scm-service";
        private String consumerWmsLocationEvents = "scm-1";

        // MES 工单创建事件流（ERP→MES）
        private String streamMesWorkOrderCreate = "hxcoe:integration:mes.work-order-create.v1";
        private String groupMesWorkOrderCreate = "mes-service";
        private String consumerMesWorkOrderCreate = "mes-1";

        // MES 工单完工事件流（MES→ERP/SCM）
        private String streamMesWorkOrderCompletionErp = "hxcoe:integration:erp.mes-work-order-completion.v1";
        private String groupMesWorkOrderCompletionErp = "erp-service";
        private String consumerMesWorkOrderCompletionErp = "erp-1";

        private String streamMesWorkOrderCompletionScm = "hxcoe:integration:scm.mes-work-order-completion.v1";
        private String groupMesWorkOrderCompletionScm = "scm-service";
        private String consumerMesWorkOrderCompletionScm = "scm-1";

        public String getStreamMesWorkOrderCreate() {
            return streamMesWorkOrderCreate;
        }

        public void setStreamMesWorkOrderCreate(String streamMesWorkOrderCreate) {
            this.streamMesWorkOrderCreate = streamMesWorkOrderCreate;
        }

        public String getGroupMesWorkOrderCreate() {
            return groupMesWorkOrderCreate;
        }

        public void setGroupMesWorkOrderCreate(String groupMesWorkOrderCreate) {
            this.groupMesWorkOrderCreate = groupMesWorkOrderCreate;
        }

        public String getConsumerMesWorkOrderCreate() {
            return consumerMesWorkOrderCreate;
        }

        public void setConsumerMesWorkOrderCreate(String consumerMesWorkOrderCreate) {
            this.consumerMesWorkOrderCreate = consumerMesWorkOrderCreate;
        }

        public String getStreamMesWorkOrderCompletionErp() {
            return streamMesWorkOrderCompletionErp;
        }

        public void setStreamMesWorkOrderCompletionErp(String streamMesWorkOrderCompletionErp) {
            this.streamMesWorkOrderCompletionErp = streamMesWorkOrderCompletionErp;
        }

        public String getGroupMesWorkOrderCompletionErp() {
            return groupMesWorkOrderCompletionErp;
        }

        public void setGroupMesWorkOrderCompletionErp(String groupMesWorkOrderCompletionErp) {
            this.groupMesWorkOrderCompletionErp = groupMesWorkOrderCompletionErp;
        }

        public String getConsumerMesWorkOrderCompletionErp() {
            return consumerMesWorkOrderCompletionErp;
        }

        public void setConsumerMesWorkOrderCompletionErp(String consumerMesWorkOrderCompletionErp) {
            this.consumerMesWorkOrderCompletionErp = consumerMesWorkOrderCompletionErp;
        }

        public String getStreamMesWorkOrderCompletionScm() {
            return streamMesWorkOrderCompletionScm;
        }

        public void setStreamMesWorkOrderCompletionScm(String streamMesWorkOrderCompletionScm) {
            this.streamMesWorkOrderCompletionScm = streamMesWorkOrderCompletionScm;
        }

        public String getGroupMesWorkOrderCompletionScm() {
            return groupMesWorkOrderCompletionScm;
        }

        public void setGroupMesWorkOrderCompletionScm(String groupMesWorkOrderCompletionScm) {
            this.groupMesWorkOrderCompletionScm = groupMesWorkOrderCompletionScm;
        }

        public String getConsumerMesWorkOrderCompletionScm() {
            return consumerMesWorkOrderCompletionScm;
        }

        public void setConsumerMesWorkOrderCompletionScm(String consumerMesWorkOrderCompletionScm) {
            this.consumerMesWorkOrderCompletionScm = consumerMesWorkOrderCompletionScm;
        }

        public String getStreamErpPoFacts() {
            return streamErpPoFacts;
        }

        public void setStreamErpPoFacts(String streamErpPoFacts) {
            this.streamErpPoFacts = streamErpPoFacts;
        }

        public String getGroupErpPoFacts() {
            return groupErpPoFacts;
        }

        public void setGroupErpPoFacts(String groupErpPoFacts) {
            this.groupErpPoFacts = groupErpPoFacts;
        }

        public String getConsumerErpPoFacts() {
            return consumerErpPoFacts;
        }

        public void setConsumerErpPoFacts(String consumerErpPoFacts) {
            this.consumerErpPoFacts = consumerErpPoFacts;
        }

        public String getStreamScmPoEvents() {
            return streamScmPoEvents;
        }

        public void setStreamScmPoEvents(String streamScmPoEvents) {
            this.streamScmPoEvents = streamScmPoEvents;
        }

        public String getGroupScmPoEvents() {
            return groupScmPoEvents;
        }

        public void setGroupScmPoEvents(String groupScmPoEvents) {
            this.groupScmPoEvents = groupScmPoEvents;
        }

        public String getConsumerScmPoEvents() {
            return consumerScmPoEvents;
        }

        public void setConsumerScmPoEvents(String consumerScmPoEvents) {
            this.consumerScmPoEvents = consumerScmPoEvents;
        }

        public String getStreamWmsWarehouseEvents() {
            return streamWmsWarehouseEvents;
        }

        public void setStreamWmsWarehouseEvents(String streamWmsWarehouseEvents) {
            this.streamWmsWarehouseEvents = streamWmsWarehouseEvents;
        }

        public String getGroupWmsWarehouseEvents() {
            return groupWmsWarehouseEvents;
        }

        public void setGroupWmsWarehouseEvents(String groupWmsWarehouseEvents) {
            this.groupWmsWarehouseEvents = groupWmsWarehouseEvents;
        }

        public String getConsumerWmsWarehouseEvents() {
            return consumerWmsWarehouseEvents;
        }

        public void setConsumerWmsWarehouseEvents(String consumerWmsWarehouseEvents) {
            this.consumerWmsWarehouseEvents = consumerWmsWarehouseEvents;
        }

        public String getStreamWmsLocationEvents() {
            return streamWmsLocationEvents;
        }

        public void setStreamWmsLocationEvents(String streamWmsLocationEvents) {
            this.streamWmsLocationEvents = streamWmsLocationEvents;
        }

        public String getGroupWmsLocationEvents() {
            return groupWmsLocationEvents;
        }

        public void setGroupWmsLocationEvents(String groupWmsLocationEvents) {
            this.groupWmsLocationEvents = groupWmsLocationEvents;
        }

        public String getConsumerWmsLocationEvents() {
            return consumerWmsLocationEvents;
        }

        public void setConsumerWmsLocationEvents(String consumerWmsLocationEvents) {
            this.consumerWmsLocationEvents = consumerWmsLocationEvents;
        }
    }
}
