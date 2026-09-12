package com.hxcoe.scm.controller;

import com.hxcoe.scm.entity.PurchaseOrderEntity;
import com.hxcoe.scm.entity.PurchaseOrderItemEntity;
import com.hxcoe.scm.repository.PurchaseOrderItemRepository;
import com.hxcoe.scm.repository.PurchaseOrderRepository;
import com.hxcoe.scm.service.ErpPoFactOutboxService;
import com.hxcoe.scm.service.PurchaseOrderEventOutboxService;
import com.hxcoe.scm.service.SupplierMirrorService;
import com.hxcoe.scm.service.WarehouseLocationMirrorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScmIntegrationController.class)
class ScmIntegrationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseOrderRepository purchaseOrderRepository;

    @MockBean
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @MockBean
    private PurchaseOrderEventOutboxService purchaseOrderEventOutboxService;

    @MockBean
    private SupplierMirrorService supplierMirrorService;

    @MockBean
    private WarehouseLocationMirrorService warehouseLocationMirrorService;

    @MockBean
    private ErpPoFactOutboxService erpPoFactOutboxService;

    @Test
    void receiptCompletedShouldUpdateStatus() throws Exception {
        PurchaseOrderEntity order = new PurchaseOrderEntity();
        order.setId(1L);
        order.setOrderNo("PO-001");
        order.setOrderStatus(40);

        PurchaseOrderItemEntity item = new PurchaseOrderItemEntity();
        item.setId(10L);
        item.setOrderId(1L);
        item.setOrderNo("PO-001");
        item.setMaterialCode("M1");
        item.setQuantity(new BigDecimal("10"));
        item.setReceivedQuantity(new BigDecimal("0"));

        when(purchaseOrderRepository.findByOrderNo("PO-001")).thenReturn(Optional.of(order));
        when(purchaseOrderItemRepository.findByOrderNoAndMaterialCode("PO-001", "M1")).thenReturn(List.of(item));
        when(purchaseOrderItemRepository.findByOrderNo("PO-001")).thenReturn(List.of(item));
        when(purchaseOrderItemRepository.save(any(PurchaseOrderItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(purchaseOrderRepository.save(any(PurchaseOrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        String body = """
                {
                  "poNo": "PO-001",
                  "receiptNo": "R1",
                  "lines": [
                    { "materialCode": "M1", "qty": 10 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/scm/integration/wms/receipt-completed")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.poNo").value("PO-001"))
                .andExpect(jsonPath("$.data.orderStatus").value(60));
    }

    @Test
    void iqcFailShouldHoldOrder() throws Exception {
        PurchaseOrderEntity order = new PurchaseOrderEntity();
        order.setId(1L);
        order.setOrderNo("PO-002");
        order.setOrderStatus(40);

        when(purchaseOrderRepository.findByOrderNo("PO-002")).thenReturn(Optional.of(order));
        when(purchaseOrderRepository.save(any(PurchaseOrderEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        String body = """
                { "poNo": "PO-002", "qcNo": "QC-1", "result": "FAIL" }
                """;

        mockMvc.perform(post("/api/v1/scm/integration/qms/iqc-completed")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderStatus").value(58));
    }
}
