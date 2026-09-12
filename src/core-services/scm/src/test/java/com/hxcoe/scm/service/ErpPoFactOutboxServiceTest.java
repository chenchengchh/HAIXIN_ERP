package com.hxcoe.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hxcoe.common.integration.IntegrationTransportProperties;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.ErpIntegrationClient;
import com.hxcoe.scm.client.dto.erp.PoFactRequest;
import com.hxcoe.scm.entity.IntegrationTaskEntity;
import com.hxcoe.scm.repository.IntegrationTaskRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ErpPoFactOutboxServiceTest {

    @Test
    void shouldConfirmTaskWhenErpReturnsApiResponse200() {
        IntegrationTaskRepository integrationTaskRepository = mock(IntegrationTaskRepository.class);
        ErpIntegrationClient erpIntegrationClient = mock(ErpIntegrationClient.class);

        ErpPoFactOutboxService service = new ErpPoFactOutboxService();
        ReflectionTestUtils.setField(service, "integrationTaskRepository", integrationTaskRepository);
        ReflectionTestUtils.setField(service, "erpIntegrationClient", erpIntegrationClient);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper().registerModule(new JavaTimeModule()));
        IntegrationTransportProperties props = new IntegrationTransportProperties();
        props.setTransport(IntegrationTransportProperties.Transport.HTTP);
        ReflectionTestUtils.setField(service, "integrationTransportProperties", props);

        IntegrationTaskEntity task = new IntegrationTaskEntity();
        task.setId(1L);
        task.setStatus("PENDING");
        task.setRetryCount(0);
        task.setRequestBody("{\"eventType\":\"po.receipt.completed.v1\",\"eventKey\":\"k\",\"eventTime\":\"2026-01-01T00:00:00\"}");

        when(integrationTaskRepository.save(any(IntegrationTaskEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(erpIntegrationClient.receivePoFacts(any(PoFactRequest.class)))
                .thenReturn(Result.error(200, "请求成功", Map.of("accepted", true)));

        service.trySendOne(task);

        assertEquals("CONFIRMED", task.getStatus());
        assertEquals(null, task.getLastError());
    }
}
