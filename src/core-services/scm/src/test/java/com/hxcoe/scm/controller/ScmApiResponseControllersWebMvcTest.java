package com.hxcoe.scm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.CrmClient;
import com.hxcoe.scm.client.dto.crm.SalesOrderDTO;
import com.hxcoe.scm.client.dto.crm.SalesOrderItemDTO;
import com.hxcoe.scm.entity.DemandForecastEntity;
import com.hxcoe.scm.entity.ForecastConfigEntity;
import com.hxcoe.scm.entity.ForecastVersionEntity;
import com.hxcoe.scm.entity.MrpPlanEntity;
import com.hxcoe.scm.repository.ForecastRepository;
import com.hxcoe.scm.repository.ForecastVersionRepository;
import com.hxcoe.scm.service.ControlTowerService;
import com.hxcoe.scm.service.ControlTowerTrendService;
import com.hxcoe.scm.service.ForecastConfigService;
import com.hxcoe.scm.service.ForecastService;
import com.hxcoe.scm.service.MrpEngineService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        ForecastController.class,
        ForecastConfigController.class,
        ForecastDashboardController.class,
        ControlTowerController.class,
        MrpController.class
})
class ScmApiResponseControllersWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ForecastService forecastService;

    @MockBean
    private ForecastConfigService forecastConfigService;

    @MockBean
    private ForecastRepository forecastRepository;

    @MockBean
    private ForecastVersionRepository forecastVersionRepository;

    @MockBean
    private CrmClient crmClient;

    @MockBean
    private ControlTowerService controlTowerService;

    @MockBean
    private ControlTowerTrendService controlTowerTrendService;

    @MockBean
    private MrpEngineService mrpEngineService;

    @Test
    void shouldReturnForecastListApiResponse() throws Exception {
        ForecastVersionEntity version = new ForecastVersionEntity();
        version.setId(1L);
        version.setPeriod("2026-05");
        version.setVersionNo(1);
        version.setStatus("PUBLISHED");
        when(forecastService.getForecastVersions("2026-05")).thenReturn(List.of(version));

        DemandForecastEntity row = new DemandForecastEntity();
        row.setId(1L);
        row.setPeriod("2026-05");
        row.setProductCode("P-001");
        row.setProductName("产品A");
        when(forecastService.getForecastList(any(), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(row)));

        mockMvc.perform(get("/api/v1/scm/forecast")
                        .param("page", "1")
                        .param("size", "10")
                        .param("period", "2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].productCode").value("P-001"));
    }

    @Test
    void shouldReturn400WhenForecastUpdateInvalid() throws Exception {
        DemandForecastEntity payload = new DemandForecastEntity();
        payload.setManualAdjustment(java.math.BigDecimal.ONE);
        when(forecastService.updateForecast(anyLong(), any(DemandForecastEntity.class)))
                .thenThrow(new IllegalStateException("版本已发布，不能修改"));

        mockMvc.perform(put("/api/v1/scm/forecast/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("版本已发布，不能修改"));
    }

    @Test
    void shouldReturnForecastConfigAndDashboardSummary() throws Exception {
        ForecastConfigEntity config = new ForecastConfigEntity();
        config.setId(1L);
        config.setModelType("ARIMA");
        when(forecastConfigService.getConfig()).thenReturn(config);

        ForecastVersionEntity published = new ForecastVersionEntity();
        published.setId(1L);
        published.setStatus("PUBLISHED");
        ForecastVersionEntity draft = new ForecastVersionEntity();
        draft.setId(2L);
        draft.setStatus("DRAFT");
        when(forecastVersionRepository.findByPeriodOrderByVersionNoDesc("2026-05"))
                .thenReturn(List.of(published, draft));

        mockMvc.perform(get("/api/v1/scm/forecast/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.modelType").value("ARIMA"));

        mockMvc.perform(get("/api/v1/scm/forecast/dashboard/status-summary")
                        .param("period", "2026-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.counts.PUBLISHED").value(1))
                .andExpect(jsonPath("$.data.counts.DRAFT").value(1));
    }

    @Test
    void shouldReturnControlTowerKpis() throws Exception {
        when(controlTowerService.getKpis()).thenReturn(Map.of("onTimeRate", 96, "inventoryTurnover", 7.2));

        mockMvc.perform(get("/api/v1/scm/control-tower/kpis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.onTimeRate").value(96));
    }

    @Test
    void shouldReturnForecastTrendWhenCrmReturnsApiResponse200() throws Exception {
        ForecastVersionEntity version = new ForecastVersionEntity();
        version.setId(1L);
        when(forecastVersionRepository.findTopByPeriodAndStatusOrderByVersionNoDesc("2026-05", "PUBLISHED"))
                .thenReturn(Optional.of(version));

        DemandForecastEntity forecast = new DemandForecastEntity();
        forecast.setFinalForecast(new BigDecimal("80"));
        when(forecastRepository.findByVersionId(1L)).thenReturn(List.of(forecast));

        SalesOrderItemDTO item = new SalesOrderItemDTO();
        item.setQuantity(new BigDecimal("12"));
        SalesOrderDTO order = new SalesOrderDTO();
        order.setDeliveryDate(LocalDate.of(2026, 5, 15));
        order.setItems(List.of(item));
        when(crmClient.getSalesOrders(null, 1, 100))
                .thenReturn(Result.error(200, "请求成功", PageResult.build(1L, 100, 1, List.of(order))));

        mockMvc.perform(get("/api/v1/scm/forecast/dashboard/trend")
                        .param("period", "2026-05")
                        .param("months", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.actual[0]").value(12))
                .andExpect(jsonPath("$.data.forecast[0]").value(80));
    }

    @Test
    void shouldReturnMrpHistoryAnd404WhenResultMissing() throws Exception {
        MrpPlanEntity plan = new MrpPlanEntity();
        plan.setId(1L);
        plan.setRunNo("MRP-001");
        plan.setRunName("五月运算");
        plan.setRunDate(LocalDateTime.of(2026, 5, 1, 8, 0));
        when(mrpEngineService.getMrpHistory(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(plan)));
        when(mrpEngineService.confirmMrpResult(anyLong(), any())).thenReturn(null);

        mockMvc.perform(get("/api/v1/scm/mrp/history")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].runNo").value("MRP-001"));

        mockMvc.perform(put("/api/v1/scm/mrp/results/9/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("结果不存在"));
    }
}
