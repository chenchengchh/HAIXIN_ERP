package com.hxcoe.wms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.wms.dto.integration.PurchaseOrderEventRequest;
import com.hxcoe.wms.entity.InventoryEntity;
import com.hxcoe.wms.entity.PoInstructionEntity;
import com.hxcoe.wms.entity.StockCountJobEntity;
import com.hxcoe.wms.entity.WarehouseEntity;
import com.hxcoe.wms.repository.InventoryRepository;
import com.hxcoe.wms.repository.InventoryTransactionRepository;
import com.hxcoe.wms.repository.PoInstructionRepository;
import com.hxcoe.wms.repository.StockCountItemRepository;
import com.hxcoe.wms.repository.StockCountJobRepository;
import com.hxcoe.wms.repository.WarehouseRepository;
import com.hxcoe.wms.service.WmsDataQualityService;
import com.hxcoe.wms.service.WmsPoInstructionService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        StockCountController.class,
        PdaInventoryController.class,
        IntegrationController.class,
        DataQualityAdminController.class,
        PoInstructionAdminController.class
})
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE"
})
class WmsTailApiResponseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StockCountJobRepository stockCountJobRepository;

    @MockBean
    private StockCountItemRepository stockCountItemRepository;

    @MockBean
    private WarehouseRepository warehouseRepository;

    @MockBean
    private InventoryRepository inventoryRepository;

    @MockBean
    private InventoryTransactionRepository inventoryTransactionRepository;

    @MockBean
    private WmsPoInstructionService wmsPoInstructionService;

    @MockBean
    private WmsDataQualityService wmsDataQualityService;

    @MockBean
    private PoInstructionRepository poInstructionRepository;

    @MockBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void shouldReturnStockCountPageCreateAnd404Detail() throws Exception {
        StockCountJobEntity job = new StockCountJobEntity();
        job.setId(1L);
        job.setCountNo("COUNT-001");
        job.setWarehouseCode("WH-1");
        job.setCountType("1");
        job.setStatus("0");
        job.setCreateUser("admin");
        job.setCreateTime(LocalDateTime.of(2026, 5, 27, 9, 0));

        WarehouseEntity warehouse = new WarehouseEntity();
        warehouse.setId(1L);
        warehouse.setWarehouseCode("WH-1");
        warehouse.setWarehouseName("主仓");

        when(stockCountJobRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(job)));
        when(warehouseRepository.findByWarehouseCode("WH-1")).thenReturn(warehouse);
        when(stockCountJobRepository.save(any(StockCountJobEntity.class))).thenAnswer(invocation -> {
            StockCountJobEntity saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(2L);
            }
            return saved;
        });
        when(stockCountJobRepository.findById(9L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/wms/stock/count-jobs")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].countNo").value("COUNT-001"))
                .andExpect(jsonPath("$.data.list[0].warehouseName").value("主仓"));

        mockMvc.perform(post("/api/v1/wms/stock/count-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"warehouseCode\":\"WH-1\",\"countType\":\"1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.warehouseCode").value("WH-1"));

        mockMvc.perform(get("/api/v1/wms/stock/count-jobs/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("盘点任务不存在"));
    }

    @Test
    void shouldReturn400ForInvalidStockCountOperations() throws Exception {
        StockCountJobEntity job = new StockCountJobEntity();
        job.setId(3L);
        job.setCountNo("COUNT-003");
        job.setWarehouseCode("WH-1");
        job.setStatus("0");
        when(stockCountJobRepository.findById(3L)).thenReturn(Optional.of(job));

        mockMvc.perform(post("/api/v1/wms/stock/count-jobs/3/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"barcode\":\"MAT-001\",\"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("盘点任务未处于执行中"));

        mockMvc.perform(post("/api/v1/wms/stock/count-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("warehouseCode 不能为空"));
    }

    @Test
    void shouldReturnPdaInventoryAndIntegrationResponses() throws Exception {
        InventoryEntity inventory = new InventoryEntity();
        inventory.setId(1L);
        inventory.setMaterialCode("MAT-001");
        inventory.setMaterialName("物料A");
        inventory.setWarehouseCode("WH-1");
        inventory.setLocationCode("A-01");
        inventory.setBatchNo("B-01");
        inventory.setUnit("PCS");
        inventory.setQuantity(new BigDecimal("8"));
        inventory.setUpdatedTime(LocalDateTime.of(2026, 5, 27, 10, 0));

        when(inventoryRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(inventory))
                .thenReturn(List.of());
        when(wmsPoInstructionService.applyScmEvent(any(PurchaseOrderEventRequest.class)))
                .thenReturn(true)
                .thenReturn(false);

        mockMvc.perform(get("/api/v1/wms/pda/inventory/query")
                        .param("barcode", "MAT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.stockInfo.materialCode").value("MAT-001"))
                .andExpect(jsonPath("$.data.stockDetails[0].locationCode").value("A-01"));

        mockMvc.perform(get("/api/v1/wms/pda/inventory/query")
                        .param("barcode", "MAT-404"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("未查询到库存记录"));

        Map<String, Object> payload = Map.of(
                "eventId", "evt-1",
                "eventKey", "po:PO-001",
                "eventType", "PURCHASE_ORDER_CREATED",
                "purchaseOrder", Map.of("orderNo", "PO-001")
        );

        mockMvc.perform(post("/api/v1/wms/integration/scm/purchase-order-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.eventKey").value("po:PO-001"));

        mockMvc.perform(post("/api/v1/wms/integration/scm/purchase-order-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("事件处理失败"));
    }

    @Test
    void shouldReturnAdminTailResponses() throws Exception {
        when(wmsDataQualityService.refresh()).thenReturn(Map.of("openTaskCount", 2));
        when(wmsDataQualityService.listTasks(1, 10, "OPEN"))
                .thenReturn(PageResult.build(1L, 10, 1, List.of(Map.of("id", 11, "status", "OPEN"))));
        when(wmsDataQualityService.resolveTask(11L, "merged", "RESOLVED"))
                .thenReturn(true);
        when(wmsDataQualityService.resolveTask(99L, "merged", "RESOLVED"))
                .thenReturn(false);
        when(wmsDataQualityService.listMetrics(any()))
                .thenReturn(List.of(Map.of("metricKey", "warehouse.total", "metricValue", 3)));

        PoInstructionEntity instruction = new PoInstructionEntity();
        instruction.setPoNo("PO-001");
        instruction.setLastEventType("PURCHASE_ORDER_CREATED");
        instruction.setUpdatedTime(LocalDateTime.of(2026, 5, 27, 11, 0));
        when(poInstructionRepository.findByPoNo("PO-001")).thenReturn(Optional.of(instruction));
        when(poInstructionRepository.findByPoNo("PO-404")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/wms/admin/data-quality/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.openTaskCount").value(2));

        mockMvc.perform(get("/api/v1/wms/admin/data-quality/tasks")
                        .param("page", "1")
                        .param("size", "10")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].status").value("OPEN"));

        mockMvc.perform(post("/api/v1/wms/admin/data-quality/tasks/11/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"merged\",\"status\":\"RESOLVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(post("/api/v1/wms/admin/data-quality/tasks/99/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"resolution\":\"merged\",\"status\":\"RESOLVED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("任务不存在"));

        mockMvc.perform(get("/api/v1/wms/admin/data-quality/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].metricKey").value("warehouse.total"));

        mockMvc.perform(get("/api/v1/wms/admin/po-instructions/by-no/PO-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.poNo").value("PO-001"));

        mockMvc.perform(get("/api/v1/wms/admin/po-instructions/by-no/PO-404"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("未找到"));
    }
}
