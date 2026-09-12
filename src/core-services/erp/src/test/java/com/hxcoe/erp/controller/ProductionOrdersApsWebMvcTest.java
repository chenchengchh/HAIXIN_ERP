package com.hxcoe.erp.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.erp.client.*;
import com.hxcoe.erp.entity.ApsOrderStatusEntity;
import com.hxcoe.erp.repository.ApsOrderStatusRepository;
import com.hxcoe.erp.service.ProductionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductionController.class)
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE"
})
public class ProductionOrdersApsWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductionService productionService;

    @MockBean
    private MesWorkOrderClient mesWorkOrderClient;

    @MockBean
    private MesReportingClient mesReportingClient;

    @MockBean
    private ApsResourceLoadClient apsResourceLoadClient;

    @MockBean
    private ApsSchedulingClient apsSchedulingClient;

    @MockBean
    private ApsProductionPlanClient apsProductionPlanClient;

    @MockBean
    private ApsScheduleResultClient apsScheduleResultClient;

    @MockBean
    private ApsScheduleDetailClient apsScheduleDetailClient;

    @MockBean
    private EamAssetClient eamAssetClient;

    @MockBean
    private ApsOrderStatusRepository apsOrderStatusRepository;

    @Test
    void shouldListOrdersFromAps() throws Exception {
        Map<String, Object> page = Map.of(
                "total", 1,
                "page", 1,
                "size", 10,
                "list", List.of(Map.of(
                        "id", 1,
                        "planNo", "PLAN-001",
                        "productCode", "P1",
                        "productName", "产品1",
                        "quantity", 10,
                        "status", "CREATED",
                        "createdTime", String.valueOf(LocalDateTime.now())
                ))
        );
        when(apsProductionPlanClient.page(eq(1), eq(10))).thenReturn(Result.success(page));
        when(apsOrderStatusRepository.findByPlanId(eq(1L))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/erp/production/orders")
                        .param("page", "1")
                        .param("size", "10")
                        .param("source", "aps"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].source").value("aps"))
                .andExpect(jsonPath("$.data.list[0].order_no").value("PLAN-001"));
    }

    @Test
    void shouldReleaseApsOrderToMes() throws Exception {
        when(apsScheduleResultClient.listByPlan(eq(1L))).thenReturn(Result.success(List.of(Map.of("id", 99))));
        when(apsScheduleResultClient.release(eq(99L))).thenReturn(Result.success());
        when(apsOrderStatusRepository.findByPlanId(eq(1L))).thenReturn(Optional.empty());
        when(apsOrderStatusRepository.save(any(ApsOrderStatusEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/v1/erp/production/orders/1/release"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.plan_id").value(1))
                .andExpect(jsonPath("$.data.schedule_result_id").value(99))
                .andExpect(jsonPath("$.data.status").value("RELEASED"));
    }
}
