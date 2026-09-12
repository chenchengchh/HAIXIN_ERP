package com.hxcoe.crm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hxcoe.crm.dto.integration.WmsOutboundShippedRequest;
import com.hxcoe.crm.entity.SalesOrderEntity;
import com.hxcoe.crm.repository.SalesOrderRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

class CrmSalesOrderDeliveryServiceTest {

    @Test
    void applyWmsOutboundShippedShouldUpdateSalesOrderDeliveryState() {
        SalesOrderRepository salesOrderRepository = mock(SalesOrderRepository.class);
        NamedParameterJdbcTemplate jdbcTemplate = mock(NamedParameterJdbcTemplate.class);

        CrmSalesOrderDeliveryService service = new CrmSalesOrderDeliveryService();
        ReflectionTestUtils.setField(service, "salesOrderRepository", salesOrderRepository);
        ReflectionTestUtils.setField(service, "jdbcTemplate", jdbcTemplate);

        SalesOrderEntity order = new SalesOrderEntity();
        order.setId(1L);
        order.setOrderNo("SO-CRM-1");
        order.setStatus("approved");

        WmsOutboundShippedRequest.OutboundOrderPayload payload = new WmsOutboundShippedRequest.OutboundOrderPayload();
        payload.setOrderNo("OUT-CRM-1");
        payload.setOrderType("SALES");
        payload.setSourceNo("SO-CRM-1");
        payload.setShippedTime(LocalDateTime.of(2026, 5, 28, 10, 0));

        WmsOutboundShippedRequest request = new WmsOutboundShippedRequest();
        request.setEventId("evt-1");
        request.setEventKey("WMS_OUTBOUND_SHIPPED_CRM:OUT-CRM-1");
        request.setEventType("WMS_OUTBOUND_SHIPPED");
        request.setOutboundOrder(payload);

        when(jdbcTemplate.update(anyString(), any(org.springframework.jdbc.core.namedparam.SqlParameterSource.class))).thenReturn(1);
        when(salesOrderRepository.findByOrderNo("SO-CRM-1")).thenReturn(order);
        when(salesOrderRepository.save(any(SalesOrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        service.applyWmsOutboundShipped(request);

        assertEquals("SHIPPED", order.getDeliveryStatus());
        assertEquals("in_progress", order.getStatus());
        assertEquals(LocalDateTime.of(2026, 5, 28, 10, 0), order.getDeliveryTime());
        verify(salesOrderRepository).save(order);
    }
}
