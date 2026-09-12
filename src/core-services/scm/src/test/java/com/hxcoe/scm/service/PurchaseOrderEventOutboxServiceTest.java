package com.hxcoe.scm.service;

import com.hxcoe.common.integration.IntegrationTransportProperties;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.SrmIntegrationClient;
import com.hxcoe.scm.client.dto.srm.PurchaseOrderEventRequest;
import com.hxcoe.scm.entity.IntegrationTaskEntity;
import com.hxcoe.scm.entity.PurchaseOrderEntity;
import com.hxcoe.scm.repository.IntegrationTaskRepository;
import com.hxcoe.scm.repository.PurchaseOrderItemRepository;
import com.hxcoe.scm.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PurchaseOrderEventOutboxServiceTest {

    @Test
    void trySendOneShouldMarkConfirmedOnSuccess() {
        IntegrationTaskRepository integrationTaskRepository = mock(IntegrationTaskRepository.class);
        PurchaseOrderRepository purchaseOrderRepository = mock(PurchaseOrderRepository.class);
        PurchaseOrderItemRepository purchaseOrderItemRepository = mock(PurchaseOrderItemRepository.class);
        SrmIntegrationClient srmIntegrationClient = mock(SrmIntegrationClient.class);

        PurchaseOrderEventOutboxService service = new PurchaseOrderEventOutboxService();
        ReflectionTestUtils.setField(service, "integrationTaskRepository", integrationTaskRepository);
        ReflectionTestUtils.setField(service, "purchaseOrderRepository", purchaseOrderRepository);
        ReflectionTestUtils.setField(service, "purchaseOrderItemRepository", purchaseOrderItemRepository);
        ReflectionTestUtils.setField(service, "srmIntegrationClient", srmIntegrationClient);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper().registerModule(new JavaTimeModule()));
        IntegrationTransportProperties props = new IntegrationTransportProperties();
        props.setTransport(IntegrationTransportProperties.Transport.HTTP);
        ReflectionTestUtils.setField(service, "integrationTransportProperties", props);

        PurchaseOrderEntity po = new PurchaseOrderEntity();
        po.setId(1L);
        po.setOrderNo("PO-1");
        po.setUpdatedTime(LocalDateTime.now());

        when(purchaseOrderRepository.findByOrderNo("PO-1")).thenReturn(Optional.of(po));
        when(purchaseOrderItemRepository.findByOrderNo("PO-1")).thenReturn(java.util.List.of());
        when(integrationTaskRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(integrationTaskRepository.save(any(IntegrationTaskEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(srmIntegrationClient.receivePurchaseOrderEvent(any(PurchaseOrderEventRequest.class))).thenReturn(Result.success(Map.of("ok", true)));

        service.enqueuePoEvent("PO-1", "PO_UPDATED");

        IntegrationTaskEntity task = new IntegrationTaskEntity();
        task.setId(10L);
        task.setStatus("PENDING");
        task.setRetryCount(0);
        task.setRequestBody("{\"eventType\":\"PO_UPDATED\",\"eventKey\":\"k\",\"eventTime\":\"2026-01-01T00:00:00\"}");

        service.trySendOne(task);

        assertEquals("CONFIRMED", task.getStatus());
    }

    @Test
    void trySendOneShouldAcceptApiResponse200FromRemote() {
        IntegrationTaskRepository integrationTaskRepository = mock(IntegrationTaskRepository.class);
        PurchaseOrderRepository purchaseOrderRepository = mock(PurchaseOrderRepository.class);
        PurchaseOrderItemRepository purchaseOrderItemRepository = mock(PurchaseOrderItemRepository.class);
        SrmIntegrationClient srmIntegrationClient = mock(SrmIntegrationClient.class);

        PurchaseOrderEventOutboxService service = new PurchaseOrderEventOutboxService();
        ReflectionTestUtils.setField(service, "integrationTaskRepository", integrationTaskRepository);
        ReflectionTestUtils.setField(service, "purchaseOrderRepository", purchaseOrderRepository);
        ReflectionTestUtils.setField(service, "purchaseOrderItemRepository", purchaseOrderItemRepository);
        ReflectionTestUtils.setField(service, "srmIntegrationClient", srmIntegrationClient);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper().registerModule(new JavaTimeModule()));
        IntegrationTransportProperties props = new IntegrationTransportProperties();
        props.setTransport(IntegrationTransportProperties.Transport.HTTP);
        ReflectionTestUtils.setField(service, "integrationTransportProperties", props);
        ReflectionTestUtils.setField(service, "srmEnabled", true);
        ReflectionTestUtils.setField(service, "wmsEnabled", false);
        ReflectionTestUtils.setField(service, "erpEnabled", false);

        IntegrationTaskEntity task = new IntegrationTaskEntity();
        task.setId(11L);
        task.setStatus("PENDING");
        task.setRetryCount(0);
        task.setRequestBody("{\"eventType\":\"PO_UPDATED\",\"eventKey\":\"k\",\"eventTime\":\"2026-01-01T00:00:00\"}");

        when(integrationTaskRepository.save(any(IntegrationTaskEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(srmIntegrationClient.receivePurchaseOrderEvent(any(PurchaseOrderEventRequest.class)))
                .thenReturn(Result.error(200, "请求成功"));

        service.trySendOne(task);

        assertEquals("CONFIRMED", task.getStatus());
    }
}
