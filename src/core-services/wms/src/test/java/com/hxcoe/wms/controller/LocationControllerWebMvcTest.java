package com.hxcoe.wms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.wms.entity.LocationEntity;
import com.hxcoe.wms.entity.WarehouseEntity;
import com.hxcoe.wms.entity.ZoneEntity;
import com.hxcoe.wms.repository.LocationRepository;
import com.hxcoe.wms.repository.WarehouseRepository;
import com.hxcoe.wms.repository.ZoneRepository;
import com.hxcoe.wms.service.MasterDataEventOutboxService;
import java.util.List;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LocationController.class)
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE"
})
public class LocationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LocationRepository locationRepository;

    @MockBean
    private WarehouseRepository warehouseRepository;

    @MockBean
    private ZoneRepository zoneRepository;

    @MockBean
    private MasterDataEventOutboxService masterDataEventOutboxService;

    @MockBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void shouldListLocations() throws Exception {
        LocationEntity location = location("LOC-01", "A-01-01", "WH-1", "Z-01");
        when(locationRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(location)));
        when(warehouseRepository.findByWarehouseCode("WH-1")).thenReturn(warehouse("WH-1", "主仓"));
        when(zoneRepository.findByZoneCode("Z-01")).thenReturn(zone("Z-01", "原料区"));

        mockMvc.perform(get("/api/v1/wms/base/locations")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].locationCode").value("LOC-01"))
                .andExpect(jsonPath("$.data.list[0].warehouseName").value("主仓"))
                .andExpect(jsonPath("$.data.list[0].zoneName").value("原料区"));
    }

    @Test
    void shouldReturn404WhenLocationMissing() throws Exception {
        when(locationRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/wms/base/locations/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("库位不存在"));
    }

    @Test
    void shouldCreateAndBatchCreateLocations() throws Exception {
        LocationEntity created = location("LOC-02", "B-01-01", "WH-1", "Z-01");
        created.setId(2L);
        when(locationRepository.save(any(LocationEntity.class))).thenReturn(created);
        when(locationRepository.saveAll(any())).thenReturn(List.of(created));
        when(warehouseRepository.findByWarehouseCode("WH-1")).thenReturn(warehouse("WH-1", "主仓"));
        when(zoneRepository.findByZoneCode("Z-01")).thenReturn(zone("Z-01", "原料区"));

        mockMvc.perform(post("/api/v1/wms/base/locations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.locationCode").value("LOC-02"));

        mockMvc.perform(post("/api/v1/wms/base/locations/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(created))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].locationCode").value("LOC-02"));

        verify(masterDataEventOutboxService, times(2))
                .enqueueLocationCreatedOrUpdated(any(LocationEntity.class), anyString());
    }

    @Test
    void shouldReturn404WhenUpdateMisses() throws Exception {
        LocationEntity payload = location("LOC-09", "异常库位", "WH-1", "Z-01");
        when(locationRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/wms/base/locations/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("库位不存在"));
    }

    @Test
    void shouldUpdateStatusDeleteGenerateAndPrint() throws Exception {
        LocationEntity existing = location("LOC-03", "C-01-01", "WH-1", "Z-01");
        existing.setId(3L);
        when(locationRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(locationRepository.save(any(LocationEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(warehouseRepository.findByWarehouseCode("WH-1")).thenReturn(warehouse("WH-1", "主仓"));
        when(zoneRepository.findByZoneCode("Z-01")).thenReturn(zone("Z-01", "原料区"));
        doNothing().when(locationRepository).deleteById(3L);
        doNothing().when(masterDataEventOutboxService).enqueueLocationDeleted("LOC-03");

        mockMvc.perform(put("/api/v1/wms/base/locations/3/status")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("0"));

        mockMvc.perform(delete("/api/v1/wms/base/locations/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("库位删除成功"));

        mockMvc.perform(get("/api/v1/wms/base/locations/generate-codes")
                        .param("prefix", "BIN")
                        .param("count", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0]").value("BIN-0001"));

        mockMvc.perform(post("/api/v1/wms/base/locations/print-codes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codes\":[\"BIN-0001\",\"BIN-0002\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.printed").value(2));

        verify(masterDataEventOutboxService).enqueueLocationDeleted("LOC-03");
    }

    private static LocationEntity location(String locationCode, String locationName, String warehouseCode, String zoneCode) {
        LocationEntity location = new LocationEntity();
        location.setLocationCode(locationCode);
        location.setLocationName(locationName);
        location.setWarehouseCode(warehouseCode);
        location.setZoneCode(zoneCode);
        location.setLocationTypeCode("PALLET");
        location.setStatus("1");
        location.setRemark("");
        return location;
    }

    private static WarehouseEntity warehouse(String warehouseCode, String warehouseName) {
        WarehouseEntity warehouse = new WarehouseEntity();
        warehouse.setWarehouseCode(warehouseCode);
        warehouse.setWarehouseName(warehouseName);
        warehouse.setStatus(1);
        return warehouse;
    }

    private static ZoneEntity zone(String zoneCode, String zoneName) {
        ZoneEntity zone = new ZoneEntity();
        zone.setZoneCode(zoneCode);
        zone.setZoneName(zoneName);
        zone.setStatus("1");
        return zone;
    }
}
