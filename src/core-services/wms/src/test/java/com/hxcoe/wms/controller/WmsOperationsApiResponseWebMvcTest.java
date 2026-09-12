package com.hxcoe.wms.controller;

import com.hxcoe.wms.entity.InventoryEntity;
import com.hxcoe.wms.entity.InventoryTransactionEntity;
import com.hxcoe.wms.entity.PickingTaskEntity;
import com.hxcoe.wms.entity.PickingTaskItemEntity;
import com.hxcoe.wms.repository.InventoryRepository;
import com.hxcoe.wms.repository.InventoryTransactionRepository;
import com.hxcoe.wms.repository.PickingTaskRepository;
import com.hxcoe.wms.service.InventoryService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        PickingTaskController.class,
        InventoryAnalyticsController.class,
        InventoryController.class
})
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE"
})
class WmsOperationsApiResponseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PickingTaskRepository pickingTaskRepository;

    @MockBean
    private InventoryRepository inventoryRepository;

    @MockBean
    private InventoryTransactionRepository inventoryTransactionRepository;

    @MockBean
    private InventoryService inventoryService;

    @MockBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void shouldReturnPickingTaskPageAnd404Detail() throws Exception {
        PickingTaskEntity task = new PickingTaskEntity();
        task.setId(1L);
        task.setTaskNo("PT-001");
        task.setWaveNo("WAVE-001");
        task.setOrderNo("OUT-001");
        task.setStatus("pending");
        when(pickingTaskRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)));
        when(pickingTaskRepository.findById(9L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/wms/outbound/picking-tasks")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].taskNo").value("PT-001"));

        mockMvc.perform(get("/api/v1/wms/outbound/picking-tasks/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("拣货任务不存在"));
    }

    @Test
    void shouldAssignTaskAndReturnOperatorsStats() throws Exception {
        PickingTaskEntity task = new PickingTaskEntity();
        task.setId(2L);
        task.setTaskNo("PT-002");
        task.setStatus("pending");
        task.setOperatorName("张三");
        PickingTaskItemEntity item = new PickingTaskItemEntity();
        item.setId(21L);
        item.setStatus("done");
        item.setLocationCode("A-01");
        task.setItems(List.of(item));

        when(pickingTaskRepository.findById(2L)).thenReturn(Optional.of(task));
        when(pickingTaskRepository.save(any(PickingTaskEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pickingTaskRepository.findAll()).thenReturn(List.of(task));
        when(pickingTaskRepository.findAll(any(Specification.class))).thenReturn(List.of(task));

        mockMvc.perform(put("/api/v1/wms/outbound/picking-tasks/2/assign")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"operatorId\":\"u01\",\"operatorName\":\"张三\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.operatorName").value("张三"));

        mockMvc.perform(get("/api/v1/wms/outbound/picking-tasks/operators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("张三"));

        mockMvc.perform(get("/api/v1/wms/outbound/picking-tasks/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalOrders").value(1));
    }

    @Test
    void shouldReturnInventoryAnalyticsAndInventoryPageResult() throws Exception {
        InventoryEntity inv = new InventoryEntity();
        inv.setId(3L);
        inv.setWarehouseCode("WH-1");
        inv.setLocationCode("A-01");
        inv.setMaterialCode("MAT-001");
        inv.setMaterialName("物料A");
        inv.setQuantity(new BigDecimal("8"));
        inv.setUpdatedTime(LocalDateTime.of(2026, 5, 27, 10, 0));

        InventoryTransactionEntity tx = new InventoryTransactionEntity();
        tx.setId(1L);
        tx.setType("OUT");
        tx.setQuantity(new BigDecimal("5"));
        tx.setTransactionTime(LocalDateTime.now().minusDays(1));

        when(inventoryRepository.findAll()).thenReturn(List.of(inv));
        when(inventoryTransactionRepository.findByTransactionTimeAfter(any(LocalDateTime.class))).thenReturn(List.of(tx));
        when(inventoryService.queryInventory(any(), any(), any(), any(), any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(inv)));
        when(inventoryService.getInventoryByMaterialCode("MAT-001")).thenReturn(List.of(inv));

        mockMvc.perform(get("/api/v1/wms/inventory/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.materialCount").value(1))
                .andExpect(jsonPath("$.data.alertCount").value(1));

        mockMvc.perform(get("/api/v1/wms/inventory/analytics/alerts")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].type").value("库存不足"));

        mockMvc.perform(get("/api/v1/wms/inventory/list")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].material_code").value("MAT-001"));

        mockMvc.perform(get("/api/v1/wms/inventory/by-material/MAT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].material_code").value("MAT-001"));
    }
}
