package com.hxcoe.erp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.erp.entity.VoucherEntity;
import com.hxcoe.erp.service.VoucherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = VoucherController.class)
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE"
})
public class VoucherControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VoucherService voucherService;

    @Test
    void shouldListVouchers() throws Exception {
        VoucherEntity v = new VoucherEntity();
        v.setId(1L);
        v.setVoucherNo("V202602050001");
        v.setVoucherType("journal");
        v.setVoucherDate(LocalDateTime.now());
        v.setStatus("draft");
        v.setDebitTotal(new BigDecimal("10"));
        v.setCreditTotal(new BigDecimal("10"));
        PageResult<VoucherEntity> page = PageResult.build(1L, 10, 1, List.of(v));
        when(voucherService.getVoucherList(anyInt(), anyInt(), any(), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/erp/finance/vouchers")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].voucher_no").value("V202602050001"));
    }

    @Test
    void shouldSubmitApprovePostRejectVoucher() throws Exception {
        VoucherEntity v = new VoucherEntity();
        v.setId(1L);
        v.setVoucherNo("V202602050001");
        v.setStatus("submitted");
        when(voucherService.submitVoucher(anyLong())).thenReturn(v);
        when(voucherService.approveVoucher(anyLong())).thenReturn(v);
        when(voucherService.postVoucher(anyLong())).thenReturn(v);
        when(voucherService.rejectVoucher(anyLong())).thenReturn(v);

        mockMvc.perform(post("/api/v1/erp/finance/vouchers/1/submit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/v1/erp/finance/vouchers/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/v1/erp/finance/vouchers/1/post"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(post("/api/v1/erp/finance/vouchers/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldReturn404WhenVoucherMissing() throws Exception {
        VoucherEntity payload = new VoucherEntity();
        payload.setVoucherNo("V404");
        when(voucherService.updateVoucher(any(VoucherEntity.class))).thenReturn(null);

        mockMvc.perform(put("/api/v1/erp/finance/vouchers/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("凭证不存在"));
    }
}
