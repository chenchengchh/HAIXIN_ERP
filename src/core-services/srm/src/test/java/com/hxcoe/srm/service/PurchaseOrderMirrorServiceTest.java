package com.hxcoe.srm.service;

import com.hxcoe.srm.dto.integration.PurchaseOrderEventRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

class PurchaseOrderMirrorServiceTest {

    @Test
    void applyScmEventShouldBeIdempotentByEventKey() {
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);

        PurchaseOrderMirrorService service = new PurchaseOrderMirrorService();
        ReflectionTestUtils.setField(service, "srmSyncJdbcTemplate", jdbcTemplate);

        PurchaseOrderEventRequest req = new PurchaseOrderEventRequest();
        req.setEventKey("PO_UPDATED:PO-1:2026-01-01T00:00:00");
        req.setEventType("PO_UPDATED");
        PurchaseOrderEventRequest.PurchaseOrderPayload payload = new PurchaseOrderEventRequest.PurchaseOrderPayload();
        payload.setOrderNo("PO-1");
        payload.setOrderStatus(40);
        req.setPurchaseOrder(payload);

        doThrow(new DuplicateKeyException("dup"))
                .when(jdbcTemplate)
                .update(startsWith("INSERT INTO srm_integration_inbox"), anyMap());

        service.applyScmEvent(req);

        verify(jdbcTemplate, times(1)).update(startsWith("INSERT INTO srm_integration_inbox"), anyMap());
        verifyNoMoreInteractions(jdbcTemplate);
    }
}
