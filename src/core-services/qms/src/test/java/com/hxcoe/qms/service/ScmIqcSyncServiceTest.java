package com.hxcoe.qms.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.qms.client.ScmIntegrationClient;
import com.hxcoe.qms.client.dto.scm.IqcCompletedRequest;
import com.hxcoe.qms.entity.IntegrationOutboxEntity;
import com.hxcoe.qms.entity.QualityInspectionEntity;
import com.hxcoe.qms.repository.IntegrationOutboxRepository;
import com.hxcoe.qms.repository.QualityInspectionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScmIqcSyncServiceTest {

    @Test
    void trySendOneShouldMarkSentOnSuccess() {
        IntegrationOutboxRepository outboxRepository = mock(IntegrationOutboxRepository.class);
        QualityInspectionRepository inspectionRepository = mock(QualityInspectionRepository.class);
        ScmIntegrationClient scmIntegrationClient = mock(ScmIntegrationClient.class);

        ScmIqcSyncService service = new ScmIqcSyncService();
        ReflectionTestUtils.setField(service, "outboxRepository", outboxRepository);
        ReflectionTestUtils.setField(service, "inspectionRepository", inspectionRepository);
        ReflectionTestUtils.setField(service, "scmIntegrationClient", scmIntegrationClient);

        QualityInspectionEntity inspection = new QualityInspectionEntity();
        inspection.setId(1L);
        inspection.setInspectionCode("INS-1");
        inspection.setSourceNo("PO-1");
        inspection.setInspectionResult("FAIL");

        IntegrationOutboxEntity outbox = new IntegrationOutboxEntity();
        outbox.setId(100L);
        outbox.setEventType(ScmIqcSyncService.EVENT_IQC_COMPLETED);
        outbox.setRefNo("INS-1");
        outbox.setEntityId(1L);
        outbox.setStatus("PENDING");
        outbox.setRetryCount(0);

        when(inspectionRepository.findById(1L)).thenReturn(Optional.of(inspection));
        when(scmIntegrationClient.iqcCompleted(any(IqcCompletedRequest.class))).thenReturn(Result.success(Map.of("ok", true)));
        when(outboxRepository.save(any(IntegrationOutboxEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.trySendOne(outbox);

        assertEquals("SENT", outbox.getStatus());
        verify(scmIntegrationClient, times(1)).iqcCompleted(any(IqcCompletedRequest.class));
    }

    @Test
    void trySendOneShouldAcceptApiResponse200() {
        IntegrationOutboxRepository outboxRepository = mock(IntegrationOutboxRepository.class);
        QualityInspectionRepository inspectionRepository = mock(QualityInspectionRepository.class);
        ScmIntegrationClient scmIntegrationClient = mock(ScmIntegrationClient.class);

        ScmIqcSyncService service = new ScmIqcSyncService();
        ReflectionTestUtils.setField(service, "outboxRepository", outboxRepository);
        ReflectionTestUtils.setField(service, "inspectionRepository", inspectionRepository);
        ReflectionTestUtils.setField(service, "scmIntegrationClient", scmIntegrationClient);

        QualityInspectionEntity inspection = new QualityInspectionEntity();
        inspection.setId(2L);
        inspection.setInspectionCode("INS-2");
        inspection.setSourceNo("PO-2");
        inspection.setInspectionResult("PASS");

        IntegrationOutboxEntity outbox = new IntegrationOutboxEntity();
        outbox.setId(101L);
        outbox.setEventType(ScmIqcSyncService.EVENT_IQC_COMPLETED);
        outbox.setRefNo("INS-2");
        outbox.setEntityId(2L);
        outbox.setStatus("PENDING");
        outbox.setRetryCount(0);

        when(inspectionRepository.findById(2L)).thenReturn(Optional.of(inspection));
        when(scmIntegrationClient.iqcCompleted(any(IqcCompletedRequest.class))).thenReturn(Result.error(200, "请求成功"));
        when(outboxRepository.save(any(IntegrationOutboxEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.trySendOne(outbox);

        assertEquals("SENT", outbox.getStatus());
        verify(scmIntegrationClient, times(1)).iqcCompleted(any(IqcCompletedRequest.class));
    }
}

