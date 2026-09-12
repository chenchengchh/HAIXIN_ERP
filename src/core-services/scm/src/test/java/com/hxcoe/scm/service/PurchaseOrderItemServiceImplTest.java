package com.hxcoe.scm.service;

import com.hxcoe.common.result.Result;
import com.hxcoe.scm.entity.PurchaseOrderItemEntity;
import com.hxcoe.scm.repository.PurchaseOrderItemRepository;
import com.hxcoe.scm.service.impl.PurchaseOrderItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PurchaseOrderItemServiceImplTest {

    @Test
    void createOrderItemShouldPopulateAuditTimestamps() {
        PurchaseOrderItemRepository repository = mock(PurchaseOrderItemRepository.class);
        PurchaseOrderItemServiceImpl service = new PurchaseOrderItemServiceImpl();
        ReflectionTestUtils.setField(service, "orderItemRepository", repository);

        when(repository.save(any(PurchaseOrderItemEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrderItemEntity item = new PurchaseOrderItemEntity();
        Result<PurchaseOrderItemEntity> result = service.createOrderItem(item);

        assertEquals(0, result.getCode());
        assertNotNull(item.getCreatedTime());
        assertNotNull(item.getUpdatedTime());
        assertEquals(item.getCreatedTime(), item.getUpdatedTime());
        assertEquals(item, result.getData());
    }
}
