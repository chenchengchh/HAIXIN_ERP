package com.hxcoe.wms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.wms.entity.WarehouseEntity;
import com.hxcoe.wms.service.WarehouseService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WarehouseController.class)
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE"
})
public class WarehouseControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WarehouseService warehouseService;

    @MockBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void shouldListWarehouses() throws Exception {
        WarehouseEntity warehouse = new WarehouseEntity();
        warehouse.setId(1L);
        warehouse.setWarehouseCode("WH-1");
        warehouse.setWarehouseName("主仓");
        when(warehouseService.getWarehouses(any())).thenReturn(new PageImpl<>(java.util.List.of(warehouse)));

        mockMvc.perform(get("/api/v1/wms/warehouse/list")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].warehouse_code").value("WH-1"));
    }

    @Test
    void shouldReturn404WhenWarehouseMissing() throws Exception {
        when(warehouseService.getWarehouseById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/wms/warehouse/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("仓库不存在"));
    }

    @Test
    void shouldReturn404WhenUpdateMisses() throws Exception {
        WarehouseEntity payload = new WarehouseEntity();
        payload.setWarehouseCode("WH-9");
        when(warehouseService.updateWarehouse(anyLong(), any(WarehouseEntity.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/wms/warehouse/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("仓库不存在"));
    }

    @Test
    void shouldCreateAndDeleteWarehouse() throws Exception {
        WarehouseEntity warehouse = new WarehouseEntity();
        warehouse.setId(2L);
        warehouse.setWarehouseCode("WH-2");
        warehouse.setWarehouseName("二号仓");
        when(warehouseService.createWarehouse(any(WarehouseEntity.class))).thenReturn(warehouse);
        doNothing().when(warehouseService).deleteWarehouse(2L);

        mockMvc.perform(post("/api/v1/wms/warehouse/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(warehouse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.warehouse_code").value("WH-2"));

        mockMvc.perform(delete("/api/v1/wms/warehouse/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("仓库删除成功"));
    }
}
