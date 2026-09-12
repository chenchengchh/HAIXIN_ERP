package com.hxcoe.srm.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.srm.client.ScmPurchaseOrderClient;
import com.hxcoe.srm.entity.PurchaseOrderEntity;
import com.hxcoe.srm.service.PurchaseOrderService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PurchaseOrderControllerWebMvcTest {

    private MockMvc buildMockMvc(PurchaseOrderService purchaseOrderService, ScmPurchaseOrderClient scmPurchaseOrderClient, boolean poWriteEnabled) {
        PurchaseOrderController controller = new PurchaseOrderController();
        ReflectionTestUtils.setField(controller, "purchaseOrderService", purchaseOrderService);
        ReflectionTestUtils.setField(controller, "scmPurchaseOrderClient", scmPurchaseOrderClient);
        ReflectionTestUtils.setField(controller, "poWriteEnabled", poWriteEnabled);
        return MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldBlockCreateWhenWriteDisabled() throws Exception {
        PurchaseOrderService purchaseOrderService = mock(PurchaseOrderService.class);
        ScmPurchaseOrderClient scmPurchaseOrderClient = mock(ScmPurchaseOrderClient.class);
        MockMvc mockMvc = buildMockMvc(purchaseOrderService, scmPurchaseOrderClient, false);

        mockMvc.perform(post("/api/v1/srm/purchase-orders")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.message").value("采购订单已由SCM管理，请使用 /api/v1/scm/purchase-orders"));
    }

    @Test
    void shouldProxyListToScmResult() throws Exception {
        PurchaseOrderService purchaseOrderService = mock(PurchaseOrderService.class);
        ScmPurchaseOrderClient scmPurchaseOrderClient = mock(ScmPurchaseOrderClient.class);
        MockMvc mockMvc = buildMockMvc(purchaseOrderService, scmPurchaseOrderClient, false);

        when(scmPurchaseOrderClient.getOrders(1, 10)).thenReturn(Result.success(java.util.Map.of(
                "total", 0,
                "list", java.util.List.of()
        )));

        mockMvc.perform(get("/api/v1/srm/purchase-orders?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    void shouldProxyGetByIdToScmByOrderNo() throws Exception {
        PurchaseOrderService purchaseOrderService = mock(PurchaseOrderService.class);
        ScmPurchaseOrderClient scmPurchaseOrderClient = mock(ScmPurchaseOrderClient.class);
        MockMvc mockMvc = buildMockMvc(purchaseOrderService, scmPurchaseOrderClient, false);

        PurchaseOrderEntity local = new PurchaseOrderEntity();
        local.setId(1L);
        local.setOrderNo("PO-001");
        when(purchaseOrderService.getPurchaseOrderById(1L)).thenReturn(Optional.of(local));

        when(scmPurchaseOrderClient.getByOrderNo("PO-001")).thenReturn(Result.success(java.util.Map.of("orderNo", "PO-001")));

        mockMvc.perform(get("/api/v1/srm/purchase-orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderNo").value("PO-001"));
    }

    @Test
    void shouldProxySupplierConfirmToScm() throws Exception {
        PurchaseOrderService purchaseOrderService = mock(PurchaseOrderService.class);
        ScmPurchaseOrderClient scmPurchaseOrderClient = mock(ScmPurchaseOrderClient.class);
        MockMvc mockMvc = buildMockMvc(purchaseOrderService, scmPurchaseOrderClient, false);

        PurchaseOrderEntity local = new PurchaseOrderEntity();
        local.setId(2L);
        local.setOrderNo("PO-002");
        when(purchaseOrderService.getPurchaseOrderById(2L)).thenReturn(Optional.of(local));

        when(scmPurchaseOrderClient.supplierConfirm(eq("PO-002"), any())).thenReturn(
                Result.success(java.util.Map.of("orderNo", "PO-002", "orderStatus", 40))
        );

        mockMvc.perform(post("/api/v1/srm/purchase-orders/2/confirm")
                        .contentType("application/json")
                        .content("{\"commitDeliveryDate\":\"2026-02-06T00:00:00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.orderNo").value("PO-002"));
    }
}
