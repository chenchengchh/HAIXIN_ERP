package com.hxcoe.wms.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.wms.client.ScmIntegrationClient;
import com.hxcoe.wms.client.dto.ScmReceiptCompletedRequest;
import com.hxcoe.wms.entity.AsnEntity;
import com.hxcoe.wms.entity.AsnItemEntity;
import com.hxcoe.wms.entity.IntegrationOutboxEntity;
import com.hxcoe.wms.repository.AsnRepository;
import com.hxcoe.wms.repository.IntegrationOutboxRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ScmReceiptSyncServiceTest {

    @Test
    void trySendOneShouldMarkSentOnSuccess() {
        IntegrationOutboxRepository outboxRepository = mock(IntegrationOutboxRepository.class);
        AsnRepository asnRepository = mock(AsnRepository.class);
        ScmIntegrationClient scmIntegrationClient = mock(ScmIntegrationClient.class);

        ScmReceiptSyncService service = new ScmReceiptSyncService();
        ReflectionTestUtils.setField(service, "outboxRepository", outboxRepository);
        ReflectionTestUtils.setField(service, "asnRepository", asnRepository);
        ReflectionTestUtils.setField(service, "scmIntegrationClient", scmIntegrationClient);

        AsnItemEntity item = new AsnItemEntity();
        item.setMaterialCode("M1");
        item.setReceivedQuantity(new BigDecimal("10"));

        AsnEntity asn = new AsnEntity();
        asn.setId(1L);
        asn.setAsnNo("ASN-1");
        asn.setDeliveryNoteNo("PO-1");
        asn.setItems(List.of(item));

        IntegrationOutboxEntity outbox = new IntegrationOutboxEntity();
        outbox.setId(100L);
        outbox.setEventType(ScmReceiptSyncService.EVENT_ASN_RECEIVED);
        outbox.setRefNo("ASN-1");
        outbox.setEntityId(1L);
        outbox.setStatus("PENDING");
        outbox.setRetryCount(0);

        when(asnRepository.findById(1L)).thenReturn(Optional.of(asn));
        when(scmIntegrationClient.receiptCompleted(any(ScmReceiptCompletedRequest.class))).thenReturn(Result.success(Map.of("ok", true)));
        when(outboxRepository.save(any(IntegrationOutboxEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.trySendOne(outbox);

        assertEquals("SENT", outbox.getStatus());
        verify(scmIntegrationClient, times(1)).receiptCompleted(any(ScmReceiptCompletedRequest.class));
    }

    @Test
    void trySendOneShouldAcceptApiResponse200() {
        IntegrationOutboxRepository outboxRepository = mock(IntegrationOutboxRepository.class);
        AsnRepository asnRepository = mock(AsnRepository.class);
        ScmIntegrationClient scmIntegrationClient = mock(ScmIntegrationClient.class);

        ScmReceiptSyncService service = new ScmReceiptSyncService();
        ReflectionTestUtils.setField(service, "outboxRepository", outboxRepository);
        ReflectionTestUtils.setField(service, "asnRepository", asnRepository);
        ReflectionTestUtils.setField(service, "scmIntegrationClient", scmIntegrationClient);

        AsnItemEntity item = new AsnItemEntity();
        item.setMaterialCode("M2");
        item.setReceivedQuantity(new BigDecimal("6"));

        AsnEntity asn = new AsnEntity();
        asn.setId(2L);
        asn.setAsnNo("ASN-2");
        asn.setDeliveryNoteNo("PO-2");
        asn.setItems(List.of(item));

        IntegrationOutboxEntity outbox = new IntegrationOutboxEntity();
        outbox.setId(101L);
        outbox.setEventType(ScmReceiptSyncService.EVENT_ASN_RECEIVED);
        outbox.setRefNo("ASN-2");
        outbox.setEntityId(2L);
        outbox.setStatus("PENDING");
        outbox.setRetryCount(0);

        when(asnRepository.findById(2L)).thenReturn(Optional.of(asn));
        when(scmIntegrationClient.receiptCompleted(any(ScmReceiptCompletedRequest.class))).thenReturn(Result.error(200, "请求成功"));
        when(outboxRepository.save(any(IntegrationOutboxEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.trySendOne(outbox);

        assertEquals("SENT", outbox.getStatus());
        verify(scmIntegrationClient, times(1)).receiptCompleted(any(ScmReceiptCompletedRequest.class));
    }
}

