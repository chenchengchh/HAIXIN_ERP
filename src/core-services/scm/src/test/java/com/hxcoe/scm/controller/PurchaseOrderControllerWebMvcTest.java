package com.hxcoe.scm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.SrmClient;
import com.hxcoe.scm.entity.PurchaseOrderEntity;
import com.hxcoe.scm.entity.PurchaseOrderItemEntity;
import com.hxcoe.scm.repository.PurchaseOrderItemRepository;
import com.hxcoe.scm.repository.PurchaseOrderRepository;
import com.hxcoe.scm.service.ScmPoAuditService;
import com.hxcoe.scm.service.PurchaseOrderEventOutboxService;
import com.hxcoe.scm.service.PurchaseOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PurchaseOrderController.class)
class PurchaseOrderControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PurchaseOrderService purchaseOrderService;

    @MockBean
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @MockBean
    private PurchaseOrderRepository purchaseOrderRepository;

    @MockBean
    private SrmClient srmClient;

    @MockBean
    private PurchaseOrderEventOutboxService purchaseOrderEventOutboxService;

    @MockBean
    private ScmPoAuditService scmPoAuditService;

    @Test
    void shouldReturnOrdersPage() throws Exception {
        PurchaseOrderEntity e = new PurchaseOrderEntity();
        e.setId(1L);
        e.setOrderNo("PO-001");

        PageResult<PurchaseOrderEntity> page = PageResult.build(1L, 10, 1, List.of(e));
        when(purchaseOrderService.getOrdersByPage(any(PageRequest.class))).thenReturn(Result.success(page));

        mockMvc.perform(get("/api/v1/scm/purchase-orders?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("PO-001"));
    }

    @Test
    void shouldReturn404WhenOrderMissing() throws Exception {
        when(purchaseOrderService.getOrderById(anyLong())).thenReturn(Result.notFound("采购订单不存在"));

        mockMvc.perform(get("/api/v1/scm/purchase-orders/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("采购订单不存在"));
    }

    @Test
    void shouldReturnOrderItems() throws Exception {
        PurchaseOrderItemEntity item = new PurchaseOrderItemEntity();
        item.setId(1L);
        item.setOrderNo("PO-001");
        item.setMaterialCode("MAT-001");
        when(purchaseOrderItemRepository.findByOrderNo(anyString())).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/scm/purchase-orders/by-no/PO-001/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].materialCode").value("MAT-001"));
    }

    @Test
    void shouldReturn404WhenSupplierConfirmOrderMissing() throws Exception {
        when(purchaseOrderRepository.findByOrderNo(anyString())).thenReturn(Optional.empty());

        PurchaseOrderController.SupplierConfirmRequest req = new PurchaseOrderController.SupplierConfirmRequest();
        req.setOperator("tester");
        req.setCommitDeliveryDate(LocalDateTime.of(2026, 5, 26, 12, 0));

        mockMvc.perform(post("/api/v1/scm/purchase-orders/by-no/PO-404/supplier/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("采购订单不存在"));
    }

    @Test
    void shouldConfirmSupplierOrder() throws Exception {
        PurchaseOrderEntity order = new PurchaseOrderEntity();
        order.setId(1L);
        order.setOrderNo("PO-002");
        order.setSupplierCode("S001");
        order.setOrderStatus(30);
        when(purchaseOrderRepository.findByOrderNo("PO-002")).thenReturn(Optional.of(order));
        when(purchaseOrderRepository.save(any(PurchaseOrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseOrderController.SupplierConfirmRequest req = new PurchaseOrderController.SupplierConfirmRequest();
        req.setOperator("supplierA");
        req.setCommitDeliveryDate(LocalDateTime.of(2026, 5, 30, 10, 0));

        mockMvc.perform(post("/api/v1/scm/purchase-orders/by-no/PO-002/supplier/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderStatus").value(40))
                .andExpect(jsonPath("$.data.updatedBy").value("supplierA"));

        verify(purchaseOrderEventOutboxService).enqueuePoEvent("PO-002", "PO_CONFIRMED");
        verify(scmPoAuditService).upsertSupplierCommit(anyString(), anyString(), any(), anyString(), anyString(), any());
        verify(scmPoAuditService).recordStatusChange(anyString(), any(), any(), anyString(), anyString(), any());
    }

    @Test
    void shouldImportFromSrmWhenRemoteReturnsApiResponse200() throws Exception {
        when(srmClient.getPurchaseOrders(0, 200)).thenReturn(Result.error(200, "请求成功", Map.of(
                "content", List.of(Map.of(
                        "id", 99L,
                        "orderNo", "PO-100",
                        "supplierId", 8L,
                        "supplierName", "供应商A",
                        "totalAmount", 88.5,
                        "status", "APPROVED"
                ))
        )));
        when(srmClient.getPurchaseOrders(1, 200)).thenReturn(Result.error(200, "请求成功", Map.of("content", List.of())));
        when(srmClient.getPurchaseOrderById(99L)).thenReturn(Result.error(200, "请求成功", Map.of(
                "items", List.of(Map.of(
                        "materialCode", "MAT-100",
                        "materialName", "物料100",
                        "unit", "PCS",
                        "quantity", 3,
                        "unitPrice", 10,
                        "subtotal", 30,
                        "receivedQuantity", 0
                ))
        )));
        when(purchaseOrderRepository.findByOrderNo("PO-100")).thenReturn(Optional.empty());
        when(purchaseOrderRepository.save(any(PurchaseOrderEntity.class))).thenAnswer(invocation -> {
            PurchaseOrderEntity saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        mockMvc.perform(post("/api/v1/scm/purchase-orders/import-from-srm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.importedOrders").value(1))
                .andExpect(jsonPath("$.data.importedItems").value(1));
    }
}
