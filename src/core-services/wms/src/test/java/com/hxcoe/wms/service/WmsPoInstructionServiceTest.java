package com.hxcoe.wms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.wms.dto.integration.PurchaseOrderEventRequest;
import com.hxcoe.wms.repository.IntegrationInboxRepository;
import com.hxcoe.wms.repository.PoInstructionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WmsPoInstructionServiceTest {

    @Test
    void applyScmEventShouldTreatDuplicateInboxAsSuccess() {
        IntegrationInboxRepository integrationInboxRepository = mock(IntegrationInboxRepository.class);
        PoInstructionRepository poInstructionRepository = mock(PoInstructionRepository.class);

        WmsPoInstructionService service = new WmsPoInstructionService();
        ReflectionTestUtils.setField(service, "integrationInboxRepository", integrationInboxRepository);
        ReflectionTestUtils.setField(service, "poInstructionRepository", poInstructionRepository);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());

        PurchaseOrderEventRequest req = new PurchaseOrderEventRequest();
        req.setEventKey("PO_CREATED:PO-1:2026-01-01T00:00:00");
        req.setIdempotencyKey(req.getEventKey());
        req.setEventId("evt-1");
        req.setTraceId("trace-1");
        req.setEventType("PO_CREATED");

        when(integrationInboxRepository.findByEventKey(req.getEventKey()))
                .thenReturn(Optional.of(new com.hxcoe.wms.entity.IntegrationInboxEntity()));

        boolean ok = service.applyScmEvent(req);

        assertTrue(ok);
        verify(poInstructionRepository, never()).save(any());
    }
}
