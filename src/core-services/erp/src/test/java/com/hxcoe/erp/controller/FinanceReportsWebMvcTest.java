package com.hxcoe.erp.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.erp.client.EamAssetClient;
import com.hxcoe.erp.entity.FinanceEntity;
import com.hxcoe.erp.entity.FixedAssetEntity;
import com.hxcoe.erp.entity.FixedAssetEamMappingEntity;
import com.hxcoe.erp.repository.CostAccountingRecordRepository;
import com.hxcoe.erp.repository.FinanceRepository;
import com.hxcoe.erp.repository.FixedAssetEamMappingRepository;
import com.hxcoe.erp.service.FinanceService;
import com.hxcoe.erp.service.FixedAssetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FinanceController.class)
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE"
})
public class FinanceReportsWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinanceService financeService;

    @MockBean
    private CostAccountingRecordRepository costAccountingRecordRepository;

    @MockBean
    private FinanceRepository financeRepository;

    @MockBean
    private FixedAssetService fixedAssetService;

    @MockBean
    private FixedAssetEamMappingRepository fixedAssetEamMappingRepository;

    @MockBean
    private EamAssetClient eamAssetClient;

    @Test
    void shouldReturnReceivableReports() throws Exception {
        FinanceEntity e = new FinanceEntity();
        e.setFinanceNo("F001");
        e.setTransactionType(1);
        e.setAmount(new BigDecimal("100"));
        e.setTransactionDate(LocalDateTime.now().minusDays(10));
        e.setCreatedBy("客户A");
        e.setIsDeleted(0);
        when(financeRepository.findAll(any(Specification.class))).thenReturn(List.of(e));

        mockMvc.perform(get("/api/v1/erp/finance/receivable/reports/aging"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.distribution").isArray());

        mockMvc.perform(get("/api/v1/erp/finance/receivable/reports/forecast"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.points").isArray());

        mockMvc.perform(get("/api/v1/erp/finance/receivable/reports/credit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldReturnPayableReports() throws Exception {
        FinanceEntity e = new FinanceEntity();
        e.setFinanceNo("P001");
        e.setTransactionType(2);
        e.setAmount(new BigDecimal("200"));
        e.setTransactionDate(LocalDateTime.now().minusDays(40));
        e.setCreatedBy("供应商A");
        e.setIsDeleted(0);
        when(financeRepository.findAll(any(Specification.class))).thenReturn(List.of(e));

        mockMvc.perform(get("/api/v1/erp/finance/payable/reports/plan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.points").isArray());

        mockMvc.perform(get("/api/v1/erp/finance/payable/reports/reconciliation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());

        mockMvc.perform(get("/api/v1/erp/finance/payable/reports/aging"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.distribution").isArray());
    }

    @Test
    void shouldLinkAndGetFixedAssetEam() throws Exception {
        FixedAssetEntity fa = new FixedAssetEntity();
        fa.setId(1L);
        when(fixedAssetService.getFixedAssetById(anyLong())).thenReturn(fa);
        when(fixedAssetEamMappingRepository.findByFixedAssetId(anyLong())).thenReturn(Optional.empty());
        when(fixedAssetEamMappingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(eamAssetClient.getById(anyLong())).thenReturn(Result.success(Map.of("id", 9, "code", "A-9")));

        mockMvc.perform(post("/api/v1/erp/finance/fixed-assets/1/eam/link")
                        .contentType("application/json")
                        .content("{\"eam_asset_id\":9}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.eam_asset_id").value(9));

        FixedAssetEamMappingEntity mapping = new FixedAssetEamMappingEntity();
        mapping.setFixedAssetId(1L);
        mapping.setEamAssetId(9L);
        when(fixedAssetEamMappingRepository.findByFixedAssetId(anyLong())).thenReturn(Optional.of(mapping));

        mockMvc.perform(get("/api/v1/erp/finance/fixed-assets/1/eam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.eam_asset").exists());
    }

    @Test
    void shouldReturnFixedAssetApiResponses() throws Exception {
        FixedAssetEntity asset = new FixedAssetEntity();
        asset.setId(1L);
        asset.setAssetNo("FA-1");
        asset.setAssetName("设备资产");
        when(fixedAssetService.getFixedAssetById(1L)).thenReturn(asset);
        when(fixedAssetService.getFixedAssetById(9L)).thenReturn(null);
        when(fixedAssetService.deleteFixedAsset(9L)).thenReturn(false);

        mockMvc.perform(get("/api/v1/erp/finance/fixed-assets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.asset_no").value("FA-1"));

        mockMvc.perform(get("/api/v1/erp/finance/fixed-assets/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("固定资产不存在"));

        mockMvc.perform(delete("/api/v1/erp/finance/fixed-assets/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("固定资产不存在"));
    }
}
