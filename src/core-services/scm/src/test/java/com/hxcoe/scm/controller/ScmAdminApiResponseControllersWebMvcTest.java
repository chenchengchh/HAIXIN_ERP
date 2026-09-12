package com.hxcoe.scm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.entity.InventoryStrategyEntity;
import com.hxcoe.scm.entity.LogisticsProviderEntity;
import com.hxcoe.scm.entity.PoReconciliationDiffEntity;
import com.hxcoe.scm.entity.PurchaseOrderItemEntity;
import com.hxcoe.scm.repository.LogisticsProviderRepository;
import com.hxcoe.scm.repository.PoReconciliationDiffRepository;
import com.hxcoe.scm.service.InventoryOptimizationService;
import com.hxcoe.scm.service.PoMigrationReconciliationAdminService;
import com.hxcoe.scm.service.PurchaseOrderEventOutboxService;
import com.hxcoe.scm.service.PurchaseOrderItemService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        LogisticsProviderController.class,
        PurchaseOrderItemController.class,
        InventoryOptimizationController.class,
        RouteOptimizationController.class,
        IntegrationAdminController.class,
        PoMigrationAdminController.class
})
class ScmAdminApiResponseControllersWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LogisticsProviderRepository logisticsProviderRepository;

    @MockBean
    private PurchaseOrderItemService purchaseOrderItemService;

    @MockBean
    private InventoryOptimizationService optimizationService;

    @MockBean
    private PurchaseOrderEventOutboxService purchaseOrderEventOutboxService;

    @MockBean
    private PoMigrationReconciliationAdminService poMigrationReconciliationAdminService;

    @MockBean
    private PoReconciliationDiffRepository poReconciliationDiffRepository;

    @Test
    void shouldReturnLogisticsProvidersAnd404Update() throws Exception {
        LogisticsProviderEntity provider = new LogisticsProviderEntity();
        provider.setId(1L);
        provider.setProviderId("LP-001");
        provider.setProviderName("顺达物流");
        when(logisticsProviderRepository.findAll()).thenReturn(List.of(provider));
        when(logisticsProviderRepository.findById(9L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/scm/logistics/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].providerId").value("LP-001"));

        mockMvc.perform(put("/api/v1/scm/logistics/providers/9")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"providerId\":\"LP-009\",\"providerName\":\"测试物流\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("物流商不存在"));
    }

    @Test
    void shouldAdaptPurchaseOrderItemResponses() throws Exception {
        PurchaseOrderItemEntity item = new PurchaseOrderItemEntity();
        item.setId(1L);
        item.setOrderNo("PO-001");
        item.setMaterialCode("MAT-001");
        item.setMaterialName("物料A");
        when(purchaseOrderItemService.getOrderItemById(1L)).thenReturn(Result.success("成功", item));
        when(purchaseOrderItemService.getOrderItemById(9L)).thenReturn(Result.notFound("明细不存在"));

        mockMvc.perform(get("/api/v1/scm/purchase-order-items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.materialCode").value("MAT-001"));

        mockMvc.perform(get("/api/v1/scm/purchase-order-items/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("明细不存在"));
    }

    @Test
    void shouldReturnInventoryOptimizationAndRouteResponses() throws Exception {
        InventoryStrategyEntity strategy = new InventoryStrategyEntity();
        strategy.setId(1L);
        strategy.setMaterialCode("MAT-001");
        strategy.setAbcClass("A");
        strategy.setReorderPoint(new BigDecimal("10"));
        when(optimizationService.getStrategies(any(), any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(PageResult.build(1L, 10, 1, List.of(strategy)));
        when(optimizationService.getStrategy(9L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/scm/inventory-optimization/strategies")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].materialCode").value("MAT-001"));

        mockMvc.perform(get("/api/v1/scm/inventory-optimization/strategies/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("策略不存在"));

        mockMvc.perform(post("/api/v1/scm/route-optimization/optimize")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "startLocation", "深圳",
                                "endLocation", "喀什",
                                "transportMode", "road"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.route").value("深圳 → 喀什"));
    }

    @Test
    void shouldReturnIntegrationRetryAndPoMigrationResponses() throws Exception {
        when(purchaseOrderEventOutboxService.trySendPendingBatch()).thenReturn(3);
        when(poMigrationReconciliationAdminService.migrateFromSrmToScm())
                .thenReturn(Map.of("insertedOrders", 2, "insertedItems", 4));
        when(poMigrationReconciliationAdminService.refreshSrmVsScmDiffs())
                .thenReturn(Map.of("upserted", 5));

        PoReconciliationDiffEntity diff = new PoReconciliationDiffEntity();
        diff.setId(1L);
        diff.setOrderNo("PO-001");
        diff.setStatus("OPEN");
        when(poReconciliationDiffRepository.findByStatusOrderByUpdatedTimeDesc(any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(diff)));
        when(poReconciliationDiffRepository.findById(1L)).thenReturn(Optional.of(diff));
        when(poReconciliationDiffRepository.findById(9L)).thenReturn(Optional.empty());
        when(poReconciliationDiffRepository.save(any(PoReconciliationDiffEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/api/v1/scm/admin/integration/po-events/retry")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.processed").value(3));

        mockMvc.perform(post("/api/v1/scm/admin/po-migration/migrate-from-srm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.insertedOrders").value(2));

        mockMvc.perform(get("/api/v1/scm/admin/po-migration/reconciliation/diffs/page")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].orderNo").value("PO-001"));

        mockMvc.perform(post("/api/v1/scm/admin/po-migration/reconciliation/diffs/1/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));

        mockMvc.perform(post("/api/v1/scm/admin/po-migration/reconciliation/diffs/9/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("差异记录不存在"));
    }

    @Test
    void shouldDeletePurchaseOrderItem() throws Exception {
        when(purchaseOrderItemService.deleteOrderItem(anyLong())).thenReturn(Result.success("删除成功", null));

        mockMvc.perform(delete("/api/v1/scm/purchase-order-items/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
