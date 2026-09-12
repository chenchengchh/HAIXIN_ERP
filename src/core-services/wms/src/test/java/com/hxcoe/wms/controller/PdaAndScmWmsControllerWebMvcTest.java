package com.hxcoe.wms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.wms.entity.AsnEntity;
import com.hxcoe.wms.entity.AsnItemEntity;
import com.hxcoe.wms.entity.InventoryEntity;
import com.hxcoe.wms.entity.PickingTaskEntity;
import com.hxcoe.wms.entity.PickingTaskItemEntity;
import com.hxcoe.wms.entity.WarehouseEntity;
import com.hxcoe.wms.repository.AsnRepository;
import com.hxcoe.wms.repository.InventoryRepository;
import com.hxcoe.wms.repository.InventoryTransactionRepository;
import com.hxcoe.wms.repository.LocationRepository;
import com.hxcoe.wms.repository.PickingTaskItemRepository;
import com.hxcoe.wms.repository.PickingTaskRepository;
import com.hxcoe.wms.service.AsnService;
import com.hxcoe.wms.service.InventoryService;
import com.hxcoe.wms.service.WarehouseService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        PdaScanController.class,
        ScmWmsController.class
})
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE"
})
class PdaAndScmWmsControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AsnRepository asnRepository;

    @MockBean
    private AsnService asnService;

    @MockBean
    private LocationRepository locationRepository;

    @MockBean
    private InventoryRepository inventoryRepository;

    @MockBean
    private InventoryTransactionRepository inventoryTransactionRepository;

    @MockBean
    private PickingTaskRepository pickingTaskRepository;

    @MockBean
    private PickingTaskItemRepository pickingTaskItemRepository;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private WarehouseService warehouseService;

    @MockBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void shouldGetInboundTask() throws Exception {
        AsnEntity asn = new AsnEntity();
        asn.setId(1L);
        asn.setAsnNo("ASN-001");
        asn.setDeliveryNoteNo("DN-001");
        asn.setStatus("CREATED");
        AsnItemEntity item = new AsnItemEntity();
        item.setMaterialCode("MAT-001");
        item.setExpectedQuantity(new BigDecimal("10"));
        item.setReceivedQuantity(new BigDecimal("2"));
        asn.setItems(List.of(item));
        when(asnRepository.findByAsnNo("ASN-001")).thenReturn(asn);

        mockMvc.perform(get("/api/v1/wms/pda/inbound/task")
                        .param("taskNo", "ASN-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.taskNo").value("ASN-001"))
                .andExpect(jsonPath("$.data.remainingQuantity").value(8));
    }

    @Test
    void shouldReturn404WhenPutawayLocationMissing() throws Exception {
        when(locationRepository.findByLocationCode("LOC-404")).thenReturn(null);

        mockMvc.perform(post("/api/v1/wms/pda/inbound/1/putaway")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "barcode", "MAT-001",
                                "locationCode", "LOC-404",
                                "quantity", 1
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("库位不存在"));
    }

    @Test
    void shouldReturn404WhenOutboundTaskMissing() throws Exception {
        when(pickingTaskRepository.findByTaskNo(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/wms/pda/outbound/task")
                        .param("taskNo", "PK-404"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("拣货任务不存在"));
    }

    @Test
    void shouldReturn400WhenInventoryInsufficient() throws Exception {
        PickingTaskEntity task = new PickingTaskEntity();
        task.setId(3L);
        task.setTaskNo("PK-003");
        task.setOrderNo("OUT-001");
        task.setStatus("assigned");

        PickingTaskItemEntity item = new PickingTaskItemEntity();
        item.setMaterialCode("MAT-003");
        item.setMaterialName("物料3");
        item.setQuantity(new BigDecimal("5"));
        item.setLocationCode("LOC-003");
        item.setBatchNo("");

        InventoryEntity inventory = new InventoryEntity();
        inventory.setId(1L);
        inventory.setMaterialCode("MAT-003");
        inventory.setWarehouseCode("MAIN_WH");
        inventory.setLocationCode("LOC-003");
        inventory.setBatchNo("");
        inventory.setQuantity(new BigDecimal("0.5"));

        when(pickingTaskRepository.findById(3L)).thenReturn(Optional.of(task));
        when(pickingTaskItemRepository.findByTaskId(3L)).thenReturn(List.of(item));
        when(inventoryRepository.findByMaterialCodeAndWarehouseCodeAndLocationCodeAndBatchNo("MAT-003", "MAIN_WH", "LOC-003", ""))
                .thenReturn(Optional.of(inventory));

        mockMvc.perform(post("/api/v1/wms/pda/outbound/3/pick")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "barcode", "MAT-003",
                                "locationCode", "LOC-003",
                                "quantity", 1
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("库存不足"));
    }

    @Test
    void shouldReturnLegacyWarehouseListAsApiResponse() throws Exception {
        WarehouseEntity warehouse = new WarehouseEntity();
        warehouse.setId(1L);
        warehouse.setWarehouseCode("WH-1");
        warehouse.setWarehouseName("主仓");
        warehouse.setStatus(1);
        when(warehouseService.getWarehouses(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(warehouse)));

        mockMvc.perform(get("/wms/warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].warehouseCode").value("WH-1"));
    }
}
