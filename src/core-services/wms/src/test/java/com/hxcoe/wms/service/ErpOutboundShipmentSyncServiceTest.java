package com.hxcoe.wms.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.wms.client.ErpClient;
import com.hxcoe.wms.client.dto.ErpOutboundShippedRequest;
import com.hxcoe.wms.entity.IntegrationOutboxEntity;
import com.hxcoe.wms.entity.OutboundOrderEntity;
import com.hxcoe.wms.entity.OutboundOrderItemEntity;
import com.hxcoe.wms.repository.IntegrationOutboxRepository;
import com.hxcoe.wms.repository.OutboundOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ErpOutboundShipmentSyncServiceTest {

    @Test
    void trySendOneShouldMarkSentOnSuccess() {
        IntegrationOutboxRepository outboxRepository = mock(IntegrationOutboxRepository.class);
        OutboundOrderRepository outboundOrderRepository = mock(OutboundOrderRepository.class);
        ErpClient erpClient = mock(ErpClient.class);

        ErpOutboundShipmentSyncService service = new ErpOutboundShipmentSyncService();
        ReflectionTestUtils.setField(service, "outboxRepository", outboxRepository);
        ReflectionTestUtils.setField(service, "outboundOrderRepository", outboundOrderRepository);
        ReflectionTestUtils.setField(service, "erpClient", erpClient);

        OutboundOrderItemEntity item = new OutboundOrderItemEntity();
        item.setMaterialCode("MAT-1");
        item.setMaterialName("物料1");
        item.setQuantity(new BigDecimal("3"));
        item.setUnit("PCS");

        OutboundOrderEntity order = new OutboundOrderEntity();
        order.setId(1L);
        order.setOrderNo("OUT-1");
        order.setType("SALES");
        order.setStatus("SHIPPED");
        order.setUpdatedTime(LocalDateTime.now());
        order.setItems(List.of(item));

        IntegrationOutboxEntity outbox = new IntegrationOutboxEntity();
        outbox.setId(100L);
        outbox.setEventType(ErpOutboundShipmentSyncService.EVENT_OUTBOUND_SHIPPED);
        outbox.setRefNo("OUT-1");
        outbox.setEntityId(1L);
        outbox.setEventId("evt-1");
        outbox.setTraceId("trace-1");
        outbox.setProducer("wms-service");
        outbox.setEventVersion(1);
        outbox.setPartitionKey("OUT-1");
        outbox.setIdempotencyKey("WMS_OUTBOUND_SHIPPED:OUT-1");
        outbox.setStatus("PENDING");
        outbox.setRetryCount(0);

        when(outboundOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(erpClient.syncOutboundShipped(any(ErpOutboundShippedRequest.class))).thenReturn(Result.success(Map.of("ok", true)));
        when(outboxRepository.save(any(IntegrationOutboxEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.trySendOne(outbox);

        assertEquals("SENT", outbox.getStatus());
        verify(erpClient, times(1)).syncOutboundShipped(any(ErpOutboundShippedRequest.class));
    }
}
