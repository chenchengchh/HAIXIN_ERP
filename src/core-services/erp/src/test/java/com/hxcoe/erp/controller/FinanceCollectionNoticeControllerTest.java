package com.hxcoe.erp.controller;

import com.hxcoe.erp.client.EamAssetClient;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FinanceController.class)
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE"
})
public class FinanceCollectionNoticeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinanceService financeService;

    @MockBean
    private CostAccountingRecordRepository costAccountingRecordRepository;

    @MockBean
    private FixedAssetService fixedAssetService;

    @MockBean
    private FinanceRepository financeRepository;

    @MockBean
    private FixedAssetEamMappingRepository fixedAssetEamMappingRepository;

    @MockBean
    private EamAssetClient eamAssetClient;

    @Test
    void shouldSendCollectionNotice() throws Exception {
        mockMvc.perform(post("/api/v1/erp/finance/receivable/1/collection-notice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.finance_id").value(1))
                .andExpect(jsonPath("$.data.notice_id").exists());
    }
}
