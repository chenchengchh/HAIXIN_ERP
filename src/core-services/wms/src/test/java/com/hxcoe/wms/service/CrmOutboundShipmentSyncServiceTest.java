package com.hxcoe.wms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hxcoe.common.result.Result;
import com.hxcoe.wms.client.CrmIntegrationClient;
import com.hxcoe.wms.client.dto.CrmOutboundShippedRequest;
import com.hxcoe.wms.entity.IntegrationOutboxEntity;
import com.hxcoe.wms.entity.OutboundOrderEntity;
import com.hxcoe.wms.repository.IntegrationOutboxRepository;
import com.hxcoe.wms.repository.OutboundOrderRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class CrmOutboundShipmentSyncServiceTest {

    @Test
    void trySendOneShouldMarkSentForSalesOrder() {
        IntegrationOutboxRepository outboxRepository = mock(IntegrationOutboxRepository.class);
        OutboundOrderRepository outboundOrderRepository = mock(OutboundOrderRepository.class);
        CrmIntegrationClient crmIntegrationClient = mock(CrmIntegrationClient.class);

        CrmOutboundShipmentSyncService service = new CrmOutboundShipmentSyncService();
        ReflectionTestUtils.setField(service, "outboxRepository", outboxRepository);
        ReflectionTestUtils.setField(service, "outboundOrderRepository", outboundOrderRepository);
        ReflectionTestUtils.setField(service, "crmIntegrationClient", crmIntegrationClient);

        OutboundOrderEntity order = new OutboundOrderEntity();
        order.setId(1L);
        order.setOrderNo("OUT-CRM-1");
        order.setSourceNo("SO-CRM-1");
        order.setType("SALES");
        order.setStatus("SHIPPED");
        order.setUpdatedTime(LocalDateTime.now());

        IntegrationOutboxEntity outbox = new IntegrationOutboxEntity();
        outbox.setId(100L);
        outbox.setEventType(CrmOutboundShipmentSyncService.EVENT_OUTBOUND_SHIPPED_CRM);
        outbox.setRefNo("OUT-CRM-1");
        outbox.setEntityId(1L);
        outbox.setEventId("evt-1");
        outbox.setTraceId("trace-1");
        outbox.setProducer("wms-service");
        outbox.setEventVersion(1);
        outbox.setPartitionKey("SO-CRM-1");
        outbox.setIdempotencyKey("WMS_OUTBOUND_SHIPPED_CRM:OUT-CRM-1");
        outbox.setStatus("PENDING");
        outbox.setRetryCount(0);

        when(outboundOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(crmIntegrationClient.outboundShipped(any(CrmOutboundShippedRequest.class))).thenReturn(Result.success(Map.of("ok", true)));
        when(outboxRepository.save(any(IntegrationOutboxEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.trySendOne(outbox);

        assertEquals("SENT", outbox.getStatus());
        verify(crmIntegrationClient, times(1)).outboundShipped(any(CrmOutboundShippedRequest.class));
    }
}
