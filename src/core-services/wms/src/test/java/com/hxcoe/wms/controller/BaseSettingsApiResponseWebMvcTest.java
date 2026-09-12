package com.hxcoe.wms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.wms.entity.BarcodeRuleEntity;
import com.hxcoe.wms.entity.LocationTypeEntity;
import com.hxcoe.wms.entity.MaterialTypeEntity;
import com.hxcoe.wms.repository.BarcodeRuleRepository;
import com.hxcoe.wms.repository.LocationTypeRepository;
import com.hxcoe.wms.repository.MaterialTypeRepository;
import java.math.BigDecimal;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        MaterialTypeController.class,
        LocationTypeController.class,
        BarcodeRuleController.class
})
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE"
})
class BaseSettingsApiResponseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MaterialTypeRepository materialTypeRepository;

    @MockBean
    private LocationTypeRepository locationTypeRepository;

    @MockBean
    private BarcodeRuleRepository barcodeRuleRepository;

    @MockBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void shouldListMaterialTypes() throws Exception {
        MaterialTypeEntity entity = new MaterialTypeEntity();
        entity.setId(1L);
        entity.setTypeCode("MT-01");
        entity.setTypeName("原料");
        entity.setStatus("1");
        when(materialTypeRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));

        mockMvc.perform(get("/api/v1/wms/base/material-types")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].typeCode").value("MT-01"));
    }

    @Test
    void shouldReturn404WhenMaterialTypeMissing() throws Exception {
        MaterialTypeEntity payload = new MaterialTypeEntity();
        payload.setTypeName("辅料");
        when(materialTypeRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/v1/wms/base/material-types/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("物料类型不存在"));
    }

    @Test
    void shouldCreateAndUpdateLocationType() throws Exception {
        LocationTypeEntity saved = new LocationTypeEntity();
        saved.setId(2L);
        saved.setTypeCode("LT-01");
        saved.setTypeName("托盘位");
        saved.setTypeDesc("托盘货位");
        saved.setMaxWeight(new BigDecimal("1000"));
        saved.setMixFlag(Boolean.TRUE);
        saved.setStatus("1");
        when(locationTypeRepository.save(any(LocationTypeEntity.class))).thenReturn(saved);
        when(locationTypeRepository.findById(2L)).thenReturn(Optional.of(saved));

        mockMvc.perform(post("/api/v1/wms/base/location-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.typeCode").value("LT-01"));

        mockMvc.perform(put("/api/v1/wms/base/location-types/2/status")
                        .param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("0"));
    }

    @Test
    void shouldListAndDeleteBarcodeRules() throws Exception {
        BarcodeRuleEntity rule = new BarcodeRuleEntity();
        rule.setId(3L);
        rule.setRuleCode("BR-01");
        rule.setRuleName("物料条码");
        rule.setRuleType("material");
        rule.setRuleFormat("MAT-{date}");
        rule.setStatus("1");
        when(barcodeRuleRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(rule)));
        doNothing().when(barcodeRuleRepository).deleteById(3L);

        mockMvc.perform(get("/api/v1/wms/base/barcode-rules")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].ruleCode").value("BR-01"));

        mockMvc.perform(delete("/api/v1/wms/base/barcode-rules/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("条码规则删除成功"));
    }
}
