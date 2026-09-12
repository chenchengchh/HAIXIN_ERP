package com.hxcoe.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hxcoe.scm.client.dto.wms.OutboundShippedRequest;
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

class ScmOutboundShipmentFactServiceTest {

    @Test
    void applyWmsOutboundShippedShouldTreatDuplicateInboxAsSuccess() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);

        ScmOutboundShipmentFactService service = new ScmOutboundShipmentFactService();
        ReflectionTestUtils.setField(service, "jdbcTemplate", jdbcTemplate);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper().registerModule(new JavaTimeModule()));

        OutboundShippedRequest req = new OutboundShippedRequest();
        req.setEventKey("WMS_OUTBOUND_SHIPPED_SCM:OUT-1");
        req.setIdempotencyKey(req.getEventKey());
        req.setEventId("evt-1");
        req.setTraceId("trace-1");
        req.setEventType("WMS_OUTBOUND_SHIPPED");

        OutboundShippedRequest.OutboundOrderPayload payload = new OutboundShippedRequest.OutboundOrderPayload();
        payload.setOrderNo("OUT-1");
        req.setOutboundOrder(payload);

        doThrow(new DuplicateKeyException("duplicate"))
                .when(jdbcTemplate)
                .update(startsWith("INSERT INTO scm_integration_inbox"), any(MapSqlParameterSource.class));

        boolean ok = service.applyWmsOutboundShipped(req);

        assertTrue(ok);
        verify(jdbcTemplate, never())
                .update(startsWith("INSERT INTO scm_outbound_shipment_fact"), any(MapSqlParameterSource.class));
    }
}
