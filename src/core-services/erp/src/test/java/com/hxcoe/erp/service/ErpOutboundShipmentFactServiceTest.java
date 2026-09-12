package com.hxcoe.erp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hxcoe.erp.dto.integration.WmsOutboundShippedRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ErpOutboundShipmentFactServiceTest {

    @Test
    void applyWmsOutboundShippedShouldTreatDuplicateInboxAsSuccess() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);

        ErpOutboundShipmentFactService service = new ErpOutboundShipmentFactService();
        ReflectionTestUtils.setField(service, "jdbcTemplate", jdbcTemplate);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper().registerModule(new JavaTimeModule()));

        WmsOutboundShippedRequest req = new WmsOutboundShippedRequest();
        req.setEventKey("WMS_OUTBOUND_SHIPPED:OUT-1");
        req.setIdempotencyKey(req.getEventKey());
        req.setEventId("evt-1");
        req.setTraceId("trace-1");
        req.setEventType("WMS_OUTBOUND_SHIPPED");
        req.setEventTime(LocalDateTime.now());

        WmsOutboundShippedRequest.OutboundOrderPayload payload = new WmsOutboundShippedRequest.OutboundOrderPayload();
        payload.setOrderNo("OUT-1");
        req.setOutboundOrder(payload);

        doThrow(new DuplicateKeyException("duplicate"))
                .when(jdbcTemplate)
                .update(startsWith("INSERT INTO erp_integration_inbox"), any(MapSqlParameterSource.class));

        boolean ok = service.applyWmsOutboundShipped(req);

        assertTrue(ok);
        verify(jdbcTemplate, never())
                .update(startsWith("INSERT INTO erp_outbound_shipment_fact"), any(MapSqlParameterSource.class));
    }
}
