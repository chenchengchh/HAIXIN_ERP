package com.hxcoe.erp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hxcoe.erp.dto.integration.PurchaseOrderEventRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ErpPoSnapshotServiceTest {

    @Test
    void applyScmPoEventShouldTreatDuplicateInboxAsSuccess() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);

        ErpPoSnapshotService service = new ErpPoSnapshotService();
        ReflectionTestUtils.setField(service, "jdbcTemplate", jdbcTemplate);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper().registerModule(new JavaTimeModule()));

        PurchaseOrderEventRequest req = new PurchaseOrderEventRequest();
        req.setEventKey("PO_CREATED:PO-1:2026-01-01T00:00:00");
        req.setIdempotencyKey(req.getEventKey());
        req.setEventId("evt-1");
        req.setTraceId("trace-1");
        req.setEventType("PO_CREATED");

        PurchaseOrderEventRequest.PurchaseOrderPayload po = new PurchaseOrderEventRequest.PurchaseOrderPayload();
        po.setOrderNo("PO-1");
        req.setPurchaseOrder(po);

        doThrow(new DuplicateKeyException("duplicate"))
                .when(jdbcTemplate)
                .update(startsWith("INSERT INTO erp_integration_inbox"), any(MapSqlParameterSource.class));

        boolean ok = service.applyScmPoEvent(req);

        assertTrue(ok);
        verify(jdbcTemplate, never())
                .update(startsWith("INSERT INTO erp_po_snapshot"), any(MapSqlParameterSource.class));
    }
}
