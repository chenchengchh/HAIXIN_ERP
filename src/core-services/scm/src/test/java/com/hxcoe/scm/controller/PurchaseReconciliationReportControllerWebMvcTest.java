package com.hxcoe.scm.controller;

import com.hxcoe.scm.entity.PurchaseOrderEntity;
import com.hxcoe.scm.entity.PurchaseOrderItemEntity;
import com.hxcoe.scm.repository.PurchaseOrderItemRepository;
import com.hxcoe.scm.repository.PurchaseOrderRepository;
import com.hxcoe.scm.service.PurchaseReconciliationQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PurchaseReconciliationReportController.class)
class PurchaseReconciliationReportControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseOrderRepository purchaseOrderRepository;

    @MockBean
    private PurchaseOrderItemRepository purchaseOrderItemRepository;

    @MockBean
    private PurchaseReconciliationQueryService purchaseReconciliationQueryService;

    @Test
    void reconciliationPageShouldReturnComputedFields() throws Exception {
        PurchaseOrderEntity po = new PurchaseOrderEntity();
        po.setId(1L);
        po.setOrderNo("PO-001");
        po.setSupplierCode("S1");
        po.setSupplierName("Supplier");
        po.setOrderAmount(new BigDecimal("100.00"));
        po.setOrderStatus(50);

        PurchaseOrderItemEntity item = new PurchaseOrderItemEntity();
        item.setId(10L);
        item.setOrderId(1L);
        item.setOrderNo("PO-001");
        item.setMaterialCode("M1");
        item.setQuantity(new BigDecimal("10"));
        item.setReceivedQuantity(new BigDecimal("5"));
        item.setAmount(new BigDecimal("90.00"));

        when(purchaseOrderRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(po), PageRequest.of(0, 10), 1));
        when(purchaseOrderItemRepository.findByOrderNoIn(eq(List.of("PO-001")))).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/scm/reports/purchase-reconciliation/page?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("PO-001"))
                .andExpect(jsonPath("$.data.list[0].amountMismatch").value(true))
                .andExpect(jsonPath("$.data.list[0].receiveRate").isNumber());
    }

    @Test
    void reconciliationOnlyMismatchShouldReturnAccurateTotal() throws Exception {
        PurchaseOrderEntity po = new PurchaseOrderEntity();
        po.setId(1L);
        po.setOrderNo("PO-001");
        po.setSupplierCode("S1");
        po.setSupplierName("Supplier");
        po.setOrderAmount(new BigDecimal("100.00"));
        po.setOrderStatus(50);

        PurchaseOrderItemEntity item = new PurchaseOrderItemEntity();
        item.setId(10L);
        item.setOrderId(1L);
        item.setOrderNo("PO-001");
        item.setMaterialCode("M1");
        item.setQuantity(new BigDecimal("10"));
        item.setReceivedQuantity(new BigDecimal("5"));
        item.setAmount(new BigDecimal("90.00"));

        when(purchaseReconciliationQueryService.countMismatchOrders(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null))).thenReturn(1L);
        when(purchaseReconciliationQueryService.findMismatchOrderNos(eq(1), eq(10), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null))).thenReturn(List.of("PO-001"));
        when(purchaseOrderRepository.findByOrderNoIn(eq(List.of("PO-001")))).thenReturn(List.of(po));
        when(purchaseOrderItemRepository.findByOrderNoIn(eq(List.of("PO-001")))).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/scm/reports/purchase-reconciliation/page?page=1&size=10&onlyMismatch=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("PO-001"))
                .andExpect(jsonPath("$.data.list[0].hasMismatch").value(true));
    }

    @Test
    void exportShouldReturnXlsx() throws Exception {
        PurchaseOrderEntity po = new PurchaseOrderEntity();
        po.setId(1L);
        po.setOrderNo("PO-001");
        po.setSupplierCode("S1");
        po.setSupplierName("Supplier");
        po.setOrderAmount(new BigDecimal("100.00"));
        po.setOrderStatus(50);

        PurchaseOrderItemEntity item = new PurchaseOrderItemEntity();
        item.setId(10L);
        item.setOrderId(1L);
        item.setOrderNo("PO-001");
        item.setMaterialCode("M1");
        item.setQuantity(new BigDecimal("10"));
        item.setReceivedQuantity(new BigDecimal("5"));
        item.setAmount(new BigDecimal("90.00"));

        when(purchaseReconciliationQueryService.findMismatchOrderNos(eq(1), any(Integer.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null)))
                .thenReturn(List.of("PO-001"));
        when(purchaseReconciliationQueryService.findMismatchOrderNos(eq(2), any(Integer.class), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null)))
                .thenReturn(List.of());
        when(purchaseOrderRepository.findByOrderNoIn(eq(List.of("PO-001")))).thenReturn(List.of(po));
        when(purchaseOrderItemRepository.findByOrderNoIn(eq(List.of("PO-001")))).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/scm/reports/purchase-reconciliation/export?onlyMismatch=true&limit=10"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("purchase-reconciliation-")))
                .andExpect(result -> assertTrue(result.getResponse().getContentAsByteArray().length > 0));
    }

    @Test
    void reasonSummaryShouldReturnCounts() throws Exception {
        when(purchaseReconciliationQueryService.getReasonSummary(eq(null), eq(null), eq(null), eq(null), any(), any()))
                .thenReturn(Map.of(
                        "totalOrders", 10,
                        "mismatchOrders", 2,
                        "qcHoldOrders", 1,
                        "amountMismatchOrders", 1,
                        "overReceivedOrders", 1
                ));

        mockMvc.perform(get("/api/v1/scm/reports/purchase-reconciliation/reason-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.mismatchOrders").value(2))
                .andExpect(jsonPath("$.data.qcHoldOrders").value(1));
    }

    @Test
    void supplierAggPageShouldReturnRows() throws Exception {
        when(purchaseReconciliationQueryService.countSupplierAgg(eq(null), eq(null), eq(null), eq(null), any(), any())).thenReturn(1L);
        when(purchaseReconciliationQueryService.findSupplierAgg(eq(1), eq(10), eq(null), eq(null), eq(null), eq(null), any(), any()))
                .thenReturn(List.of(Map.of(
                        "supplierCode", "S1",
                        "supplierName", "Supplier",
                        "totalOrders", 10,
                        "mismatchOrders", 2,
                        "qcHoldOrders", 1,
                        "amountMismatchOrders", 1,
                        "overReceivedOrders", 1
                )));

        mockMvc.perform(get("/api/v1/scm/reports/purchase-reconciliation/aggregate/suppliers/page?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].supplierCode").value("S1"))
                .andExpect(jsonPath("$.data.list[0].mismatchOrders").value(2));
    }

    @Test
    void materialAggPageShouldReturnRows() throws Exception {
        when(purchaseReconciliationQueryService.countMaterialAgg(eq(null), eq(null), eq(null), eq(null), any(), any())).thenReturn(1L);
        when(purchaseReconciliationQueryService.findMaterialAgg(eq(1), eq(10), eq(null), eq(null), eq(null), eq(null), any(), any()))
                .thenReturn(List.of(Map.of(
                        "materialCode", "M1",
                        "materialName", "Material",
                        "orderCount", 3,
                        "orderedQtySum", 10,
                        "receivedQtySum", 5,
                        "overReceivedLines", 0
                )));

        mockMvc.perform(get("/api/v1/scm/reports/purchase-reconciliation/aggregate/materials/page?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].materialCode").value("M1"));
    }

    @Test
    void qcHoldPageShouldReturnOnlyHoldOrders() throws Exception {
        PurchaseOrderEntity po = new PurchaseOrderEntity();
        po.setId(2L);
        po.setOrderNo("PO-002");
        po.setOrderStatus(58);

        when(purchaseOrderRepository.findByOrderStatus(eq(58), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(po), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/v1/scm/reports/purchase-orders/qc-hold/page?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].orderStatus").value(58));
    }
}
