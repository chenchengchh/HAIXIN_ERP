package com.hxcoe.wms.service;

import com.hxcoe.wms.entity.OutboundOrderEntity;
import com.hxcoe.wms.repository.InventoryRepository;
import com.hxcoe.wms.repository.InventoryTransactionRepository;
import com.hxcoe.wms.repository.OutboundOrderRepository;
import com.hxcoe.wms.service.impl.OutboundOrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OutboundOrderServiceImplTest {

    @Test
    void createOutboundOrderShouldGenerateOrderNoWhenMissing() {
        OutboundOrderRepository outboundOrderRepository = mock(OutboundOrderRepository.class);
        InventoryRepository inventoryRepository = mock(InventoryRepository.class);
        InventoryTransactionRepository inventoryTransactionRepository = mock(InventoryTransactionRepository.class);

        OutboundOrderServiceImpl service = new OutboundOrderServiceImpl();
        ReflectionTestUtils.setField(service, "outboundOrderRepository", outboundOrderRepository);
        ReflectionTestUtils.setField(service, "inventoryRepository", inventoryRepository);
        ReflectionTestUtils.setField(service, "inventoryTransactionRepository", inventoryTransactionRepository);

        when(outboundOrderRepository.save(any(OutboundOrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OutboundOrderEntity order = new OutboundOrderEntity();
        OutboundOrderEntity saved = service.createOutboundOrder(order);

        assertNotNull(saved.getOrderNo());
        assertEquals("CREATED", saved.getStatus());
    }
}
