package com.hxcoe.erp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.erp.client.ApsResourceLoadClient;
import com.hxcoe.erp.client.ApsProductionPlanClient;
import com.hxcoe.erp.client.ApsScheduleDetailClient;
import com.hxcoe.erp.client.ApsScheduleResultClient;
import com.hxcoe.erp.client.ApsSchedulingClient;
import com.hxcoe.erp.client.EamAssetClient;
import com.hxcoe.erp.client.MesReportingClient;
import com.hxcoe.erp.client.MesWorkOrderClient;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.dto.production.CapacityDataDto;
import com.hxcoe.erp.dto.production.CapacityPlanningResultDto;
import com.hxcoe.erp.repository.ApsOrderStatusRepository;
import com.hxcoe.erp.service.ProductionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductionController.class)
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE"
})
public class ProductionCapacityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductionService productionService;

    @MockBean
    private ApsResourceLoadClient apsResourceLoadClient;

    @MockBean
    private ApsSchedulingClient apsSchedulingClient;

    @MockBean
    private EamAssetClient eamAssetClient;

    @MockBean
    private MesWorkOrderClient mesWorkOrderClient;

    @MockBean
    private MesReportingClient mesReportingClient;

    @MockBean
    private ApsProductionPlanClient apsProductionPlanClient;

    @MockBean
    private ApsScheduleResultClient apsScheduleResultClient;

    @MockBean
    private ApsScheduleDetailClient apsScheduleDetailClient;

    @MockBean
    private ApsOrderStatusRepository apsOrderStatusRepository;

    @Test
    void shouldReturnCapacityPageFromApsResourceLoad() throws Exception {
        CapacityDataDto dto = new CapacityDataDto();
        dto.setDepartmentName("生产部");
        dto.setWorkCenterName("组装车间");
        dto.setResourceName("组装线1");
        dto.setUsedCapacity(new BigDecimal("80"));
        dto.setAvailableCapacity(new BigDecimal("20"));
        dto.setUtilizationRate(new BigDecimal("80"));
        dto.setPeriod("day");
        dto.setPeriodDate("2026-02-01");
        dto.setRemark("");

        when(productionService.getCapacity(any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(PageResult.build(1L, 10, 1, List.of(dto)));

        mockMvc.perform(get("/api/v1/erp/production/capacity")
                        .param("page", "1")
                        .param("size", "10")
                        .param("period", "day"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].department_name").value("生产部"))
                .andExpect(jsonPath("$.data.list[0].work_center_name").value("组装车间"))
                .andExpect(jsonPath("$.data.list[0].resource_name").value("组装线1"))
                .andExpect(jsonPath("$.data.list[0].used_capacity").value(80))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    void shouldRunAndQueryCapacityPlanningResults() throws Exception {
        CapacityPlanningResultDto dto = new CapacityPlanningResultDto();
        dto.setDepartmentName("生产部");
        dto.setWorkCenterName("加工车间");
        dto.setResourceName("加工中心A");
        dto.setPlannedLoad(new BigDecimal("71.5"));
        dto.setAvailableCapacity(new BigDecimal("35"));
        dto.setCapacityGap(new BigDecimal("36.5"));
        dto.setSuggestionType("overtime");
        dto.setSuggestionContent("产能不足，建议安排加班或增加生产班次");
        dto.setRemark("");

        when(apsSchedulingClient.execute(any())).thenReturn(com.hxcoe.common.result.Result.success("ok"));
        when(productionService.runCapacityPlanning(any()))
                .thenReturn(PageResult.build(1L, 10, 1, List.of(dto)));
        when(productionService.getCapacityPlanningResults(any(), any()))
                .thenReturn(PageResult.build(1L, 10, 1, List.of(dto)));

        Map<String, Object> body = Map.of(
                "planning_period", "day",
                "start_date", "2026-02-01",
                "end_date", "2026-02-07",
                "consider_overtime", true,
                "consider_outsource", false,
                "capacity_buffer_rate", 10
        );

        mockMvc.perform(post("/api/v1/erp/production/capacity-planning/run")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.list[0].suggestion_type").value("overtime"));

        mockMvc.perform(get("/api/v1/erp/production/capacity-planning/results")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].resource_name").value("加工中心A"));
    }
}
