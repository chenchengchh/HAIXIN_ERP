package com.hxcoe.wms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.wms.entity.WarehouseEntity;
import com.hxcoe.wms.entity.ZoneEntity;
import com.hxcoe.wms.repository.WarehouseRepository;
import com.hxcoe.wms.repository.ZoneRepository;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ZoneController.class)
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE"
})
public class ZoneControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ZoneRepository zoneRepository;

    @MockBean
    private WarehouseRepository warehouseRepository;

    @MockBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void shouldListZones() throws Exception {
        ZoneEntity zone = zone("Z-01", "原料区", "WH-1");
        WarehouseEntity warehouse = warehouse("WH-1", "主仓");
        when(zoneRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(zone)));
        when(warehouseRepository.findByWarehouseCode("WH-1")).thenReturn(warehouse);

        mockMvc.perform(get("/api/v1/wms/base/zones")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].zoneCode").value("Z-01"))
                .andExpect(jsonPath("$.data.list[0].warehouseName").value("主仓"));
    }

    @Test
    void shouldReturn404WhenZoneMissing() throws Exception {
        when(zoneRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/wms/base/zones/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("库区不存在"));
    }

    @Test
    void shouldCreateZone() throws Exception {
        ZoneEntity saved = zone("Z-02", "成品区", "WH-1");
        saved.setId(2L);
        WarehouseEntity warehouse = warehouse("WH-1", "主仓");
        when(zoneRepository.save(any(ZoneEntity.class))).thenReturn(saved);
        when(warehouseRepository.findByWarehouseCode("WH-1")).thenReturn(warehouse);

        mockMvc.perform(post("/api/v1/wms/base/zones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.zoneCode").value("Z-02"))
                .andExpect(jsonPath("$.data.warehouseName").value("主仓"));
    }

    @Test
    void shouldReturn404WhenUpdateMisses() throws Exception {
        ZoneEntity payload = zone("Z-09", "异常区", "WH-1");
        when(zoneRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/wms/base/zones/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("库区不存在"));
    }

    @Test
    void shouldUpdateStatusAndDeleteZone() throws Exception {
        ZoneEntity existing = zone("Z-03", "待检区", "WH-1");
        existing.setId(3L);
        WarehouseEntity warehouse = warehouse("WH-1", "主仓");
        when(zoneRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(zoneRepository.save(any(ZoneEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(warehouseRepository.findByWarehouseCode("WH-1")).thenReturn(warehouse);
        doNothing().when(zoneRepository).deleteById(3L);

        mockMvc.perform(put("/api/v1/wms/base/zones/3/status")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("0"));

        mockMvc.perform(delete("/api/v1/wms/base/zones/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("库区删除成功"));
    }

    private static ZoneEntity zone(String zoneCode, String zoneName, String warehouseCode) {
        ZoneEntity zone = new ZoneEntity();
        zone.setZoneCode(zoneCode);
        zone.setZoneName(zoneName);
        zone.setWarehouseCode(warehouseCode);
        zone.setZoneType("1");
        zone.setStatus("1");
        return zone;
    }

    private static WarehouseEntity warehouse(String warehouseCode, String warehouseName) {
        WarehouseEntity warehouse = new WarehouseEntity();
        warehouse.setWarehouseCode(warehouseCode);
        warehouse.setWarehouseName(warehouseName);
        warehouse.setStatus(1);
        return warehouse;
    }
}
