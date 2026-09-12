package com.hxcoe.srm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.result.Result;
import com.hxcoe.srm.client.ErpSupplierEventClient;
import com.hxcoe.srm.client.ScmSupplierEventClient;
import com.hxcoe.srm.dto.integration.SupplierEventRequest;
import com.hxcoe.srm.entity.IntegrationTaskEntity;
import com.hxcoe.srm.repository.IntegrationTaskRepository;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SupplierEventOutboxServiceTest {

    @Test
    void shouldConfirmTaskWhenScmEventSendSucceeds() throws Exception {
        IntegrationTaskRepository integrationTaskRepository = mock(IntegrationTaskRepository.class);
        ScmSupplierEventClient scmSupplierEventClient = mock(ScmSupplierEventClient.class);
        ErpSupplierEventClient erpSupplierEventClient = mock(ErpSupplierEventClient.class);

        SupplierEventRequest request = new SupplierEventRequest();
        request.setEventId("evt-1");
        request.setEventKey("SUPPLIER_CREATED:SUP-1");
        request.setTraceId("trace-1");

        IntegrationTaskEntity task = new IntegrationTaskEntity();
        task.setActionType(SupplierEventOutboxService.ACTION_TYPE_SCM_SUPPLIER_EVENT);
        task.setEventId("evt-1");
        task.setTraceId("trace-1");
        task.setIdempotencyKey("SCM:SUPPLIER_CREATED:SUP-1");
        task.setStatus("PENDING");
        task.setRetryCount(0);
        task.setRequestBody(new ObjectMapper().writeValueAsString(request));

        when(scmSupplierEventClient.receiveSupplierEvent(any())).thenReturn(Result.success(Map.of("eventKey", "SUPPLIER_CREATED:SUP-1")));

        SupplierEventOutboxService service = new SupplierEventOutboxService();
        ReflectionTestUtils.setField(service, "integrationTaskRepository", integrationTaskRepository);
        ReflectionTestUtils.setField(service, "scmSupplierEventClient", scmSupplierEventClient);
        ReflectionTestUtils.setField(service, "erpSupplierEventClient", erpSupplierEventClient);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());

        service.trySendOne(task);

        assertEquals("CONFIRMED", task.getStatus());
        assertEquals(null, task.getLastError());
        verify(integrationTaskRepository).save(task);
    }

    @Test
    void shouldMarkFailedWhenErpEventSendReturnsError() throws Exception {
        IntegrationTaskRepository integrationTaskRepository = mock(IntegrationTaskRepository.class);
        ScmSupplierEventClient scmSupplierEventClient = mock(ScmSupplierEventClient.class);
        ErpSupplierEventClient erpSupplierEventClient = mock(ErpSupplierEventClient.class);

        SupplierEventRequest request = new SupplierEventRequest();
        request.setEventId("evt-2");
        request.setEventKey("SUPPLIER_UPDATED:SUP-2");
        request.setTraceId("trace-2");

        IntegrationTaskEntity task = new IntegrationTaskEntity();
        task.setActionType(SupplierEventOutboxService.ACTION_TYPE_ERP_SUPPLIER_EVENT);
        task.setEventId("evt-2");
        task.setTraceId("trace-2");
        task.setIdempotencyKey("ERP:SUPPLIER_UPDATED:SUP-2");
        task.setStatus("PENDING");
        task.setRetryCount(1);
        task.setRequestBody(new ObjectMapper().writeValueAsString(request));

        when(erpSupplierEventClient.receiveSupplierEvent(any())).thenReturn(Result.error("下游失败"));

        SupplierEventOutboxService service = new SupplierEventOutboxService();
        ReflectionTestUtils.setField(service, "integrationTaskRepository", integrationTaskRepository);
        ReflectionTestUtils.setField(service, "scmSupplierEventClient", scmSupplierEventClient);
        ReflectionTestUtils.setField(service, "erpSupplierEventClient", erpSupplierEventClient);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());

        service.trySendOne(task);

        assertEquals("FAILED", task.getStatus());
        assertEquals(2, task.getRetryCount());
        assertEquals("ERP失败:下游失败", task.getLastError());
        verify(integrationTaskRepository).save(task);
    }

    @Test
    void shouldConfirmTaskWhenErpReturnsApiResponseCode() throws Exception {
        IntegrationTaskRepository integrationTaskRepository = mock(IntegrationTaskRepository.class);
        ScmSupplierEventClient scmSupplierEventClient = mock(ScmSupplierEventClient.class);
        ErpSupplierEventClient erpSupplierEventClient = mock(ErpSupplierEventClient.class);

        SupplierEventRequest request = new SupplierEventRequest();
        request.setEventId("evt-3");
        request.setEventKey("SUPPLIER_SYNC:SUP-3");
        request.setTraceId("trace-3");

        IntegrationTaskEntity task = new IntegrationTaskEntity();
        task.setActionType(SupplierEventOutboxService.ACTION_TYPE_ERP_SUPPLIER_EVENT);
        task.setEventId("evt-3");
        task.setTraceId("trace-3");
        task.setIdempotencyKey("ERP:SUPPLIER_SYNC:SUP-3");
        task.setStatus("PENDING");
        task.setRetryCount(0);
        task.setRequestBody(new ObjectMapper().writeValueAsString(request));

        when(erpSupplierEventClient.receiveSupplierEvent(any())).thenReturn(Result.error(200, "请求成功"));

        SupplierEventOutboxService service = new SupplierEventOutboxService();
        ReflectionTestUtils.setField(service, "integrationTaskRepository", integrationTaskRepository);
        ReflectionTestUtils.setField(service, "scmSupplierEventClient", scmSupplierEventClient);
        ReflectionTestUtils.setField(service, "erpSupplierEventClient", erpSupplierEventClient);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());

        service.trySendOne(task);

        assertEquals("CONFIRMED", task.getStatus());
        assertEquals(null, task.getLastError());
        verify(integrationTaskRepository).save(task);
    }
}
