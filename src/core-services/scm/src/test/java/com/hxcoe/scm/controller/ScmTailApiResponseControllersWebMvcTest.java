package com.hxcoe.scm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.SrmClient;
import com.hxcoe.scm.client.WmsClient;
import com.hxcoe.scm.client.WmsLocationClient;
import com.hxcoe.scm.client.dto.WmsInventoryDTO;
import com.hxcoe.scm.entity.InventoryStrategyEntity;
import com.hxcoe.scm.entity.PurchaseOrderEntity;
import com.hxcoe.scm.entity.ShipmentEntity;
import com.hxcoe.scm.entity.SupplierEntity;
import com.hxcoe.scm.repository.InventoryStrategyRepository;
import com.hxcoe.scm.service.LogisticsService;
import com.hxcoe.scm.service.PurchaseOrderService;
import com.hxcoe.scm.service.SupplierService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        SupplierController.class,
        SupplierCollaborationController.class,
        LogisticsController.class,
        MasterDataController.class,
        InventoryHealthController.class
})
class ScmTailApiResponseControllersWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SrmClient srmClient;

    @MockBean
    private SupplierService supplierService;

    @MockBean
    private PurchaseOrderService purchaseOrderService;

    @MockBean
    private LogisticsService logisticsService;

    @MockBean
    private NamedParameterJdbcTemplate jdbcTemplate;

    @MockBean
    private WmsLocationClient wmsLocationClient;

    @MockBean
    private InventoryStrategyRepository inventoryStrategyRepository;

    @MockBean
    private WmsClient wmsClient;

    @Test
    void shouldReturnSupplierPageFromRemoteAndLocalFallback() throws Exception {
        when(srmClient.getSuppliers(0, 10)).thenReturn(Result.success("ok", Map.of(
                "total", 1,
                "page", 1,
                "size", 10,
                "list", List.of(Map.of("supplierCode", "SUP-001", "supplierName", "远端供应商"))
        )));

        mockMvc.perform(get("/api/v1/scm/suppliers")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].supplierCode").value("SUP-001"));

        when(srmClient.getSuppliers(1, 5)).thenThrow(new RuntimeException("SRM down"));
        SupplierEntity local = new SupplierEntity();
        local.setId(2L);
        local.setSupplierCode("SUP-LOCAL");
        local.setSupplierName("本地供应商");
        when(supplierService.getSuppliersByPage(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(Result.success("ok", PageResult.build(1L, 5, 2, List.of(local))));

        mockMvc.perform(get("/api/v1/scm/suppliers")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.page").value(2))
                .andExpect(jsonPath("$.data.list[0].supplierCode").value("SUP-LOCAL"));
    }

    @Test
    void shouldReturnSupplierCollaborationOrders() throws Exception {
        PurchaseOrderEntity order = new PurchaseOrderEntity();
        order.setId(1L);
        order.setOrderNo("PO-001");
        order.setSupplierName("华星供应商");
        order.setOrderStatus(40);
        order.setCreatedTime(LocalDateTime.of(2026, 5, 27, 9, 0));
        order.setExpectedDeliveryDate(LocalDateTime.of(2026, 5, 30, 10, 0));
        when(purchaseOrderService.getOrdersByPage(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(Result.success("ok", PageResult.build(1L, 10, 1, List.of(order))));

        mockMvc.perform(get("/api/v1/scm/supplier-collaboration/purchase-orders")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].orderId").value("PO-001"))
                .andExpect(jsonPath("$.data.list[0].supplierName").value("华星供应商"));
    }

    @Test
    void shouldReturnLogisticsListAnd404Detail() throws Exception {
        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setId(1L);
        shipment.setShipmentNo("SHP-001");
        shipment.setStatus("CREATED");
        when(logisticsService.getShipments(anyString(), anyString(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(PageResult.build(1L, 10, 1, List.of(shipment)));
        when(logisticsService.getShipment(9L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/scm/logistics/shipments")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].shipmentNo").value("SHP-001"));

        mockMvc.perform(get("/api/v1/scm/logistics/shipments/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("运单不存在"));
    }

    @Test
    void shouldReturnMasterDataPageAnd404Detail() throws Exception {
        when(wmsLocationClient.page(anyInt(), anyInt(), any(), any(), any(), any(), any()))
                .thenReturn(Result.success("ok", Map.of(
                        "total", 1,
                        "page", 1,
                        "size", 10,
                        "list", List.of(Map.of(
                                "id", 1,
                                "locationCode", "LOC-001",
                                "warehouseCode", "WH-1"
                        ))
                )));
        when(wmsLocationClient.detail(9L)).thenReturn(Result.success("ok", Map.of()));
        when(jdbcTemplate.queryForList(anyString(), any(MapSqlParameterSource.class))).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/scm/master-data/locations")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].locationCode").value("LOC-001"));

        mockMvc.perform(get("/api/v1/scm/master-data/locations/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("库位不存在"));
    }

    @Test
    void shouldReturnInventoryHealthSummaryAndAlerts() throws Exception {
        InventoryStrategyEntity strategy = new InventoryStrategyEntity();
        strategy.setId(1L);
        strategy.setMaterialCode("MAT-001");
        strategy.setMaterialName("物料A");
        strategy.setAbcClass("A");
        strategy.setReorderPoint(new BigDecimal("10"));
        when(inventoryStrategyRepository.findAll()).thenReturn(List.of(strategy));

        WmsInventoryDTO inventory = new WmsInventoryDTO();
        inventory.setMaterialCode("MAT-001");
        inventory.setQuantity(new BigDecimal("8"));
        when(wmsClient.getInventoryByMaterialCode("MAT-001"))
                .thenReturn(Result.success("ok", List.of(inventory)));
        when(wmsClient.getTransactions("MAT-001", 30))
                .thenReturn(Result.success("ok", List.of(Map.of(
                        "type", "OUT",
                        "quantity", "5",
                        "transactionTime", "2026-05-20 08:00:00"
                ))));

        mockMvc.perform(get("/api/v1/scm/inventory-health/summary")
                        .param("days", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.lowStockCount").value(1));

        mockMvc.perform(get("/api/v1/scm/inventory-health/alerts")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].type").value("LOW_STOCK"))
                .andExpect(jsonPath("$.data[0].materialCode").value("MAT-001"));
    }

    @Test
    void shouldCreateShipmentApiResponse() throws Exception {
        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setId(3L);
        shipment.setShipmentNo("SHP-003");
        shipment.setStatus("CREATED");
        when(logisticsService.createShipment(any(ShipmentEntity.class))).thenReturn(shipment);

        mockMvc.perform(post("/api/v1/scm/logistics/shipments")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "relatedType", "PO",
                                "relatedNo", "PO-003"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.shipmentNo").value("SHP-003"));
    }
}
