package com.hxcoe.wms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.wms.entity.AsnEntity;
import com.hxcoe.wms.entity.OutboundOrderEntity;
import com.hxcoe.wms.entity.WaveEntity;
import com.hxcoe.wms.repository.OutboundOrderRepository;
import com.hxcoe.wms.repository.PickingTaskRepository;
import com.hxcoe.wms.service.AsnService;
import com.hxcoe.wms.service.OutboundOrderService;
import com.hxcoe.wms.service.WaveService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        InboundController.class,
        OutboundOrderController.class,
        WaveController.class
})
@TestPropertySource(properties = {
        "spring.jackson.property-naming-strategy=SNAKE_CASE"
})
class WmsMainFlowApiResponseWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AsnService asnService;

    @MockBean
    private OutboundOrderService outboundOrderService;

    @MockBean
    private WaveService waveService;

    @MockBean
    private OutboundOrderRepository outboundOrderRepository;

    @MockBean
    private PickingTaskRepository pickingTaskRepository;

    @MockBean(name = "jpaMappingContext")
    private JpaMetamodelMappingContext jpaMappingContext;

    @Test
    void shouldReturnInboundPageResult() throws Exception {
        AsnEntity asn = new AsnEntity();
        asn.setId(1L);
        asn.setAsnNo("ASN-001");
        asn.setDeliveryNoteNo("DN-001");
        asn.setStatus("CREATED");
        when(asnService.getAsns(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(asn)));

        mockMvc.perform(get("/api/v1/wms/inbound/asn")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].asn_no").value("ASN-001"));
    }

    @Test
    void shouldReturn400WhenInboundScanBarcodeMissing() throws Exception {
        mockMvc.perform(post("/api/v1/wms/inbound/asn/1/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("barcode不能为空"));
    }

    @Test
    void shouldReturnOutboundPageAnd404ForMissingDetail() throws Exception {
        OutboundOrderEntity order = new OutboundOrderEntity();
        order.setId(2L);
        order.setOrderNo("OUT-001");
        order.setType("SALES");
        order.setCustomerName("客户A");
        order.setStatus("CREATED");
        order.setCreatedTime(LocalDateTime.of(2026, 5, 27, 9, 0));
        when(outboundOrderService.getOutboundOrders(any(org.springframework.data.domain.Pageable.class), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(order)));
        when(outboundOrderService.getOutboundOrderById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/wms/outbound/orders")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].outboundNo").value("OUT-001"));

        mockMvc.perform(get("/api/v1/wms/outbound/orders/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("订单不存在"));
    }

    @Test
    void shouldReturn400WhenOutboundApproveFails() throws Exception {
        when(outboundOrderService.approveOutboundOrder(3L)).thenReturn(null);

        mockMvc.perform(put("/api/v1/wms/outbound/orders/3/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("审核失败"));
    }

    @Test
    void shouldReturnWaveList404AndAllocateSuccess() throws Exception {
        WaveEntity wave = new WaveEntity();
        wave.setId(5L);
        wave.setWaveNo("WAVE-001");
        wave.setStatus("CREATED");
        wave.setCreatedBy("admin");
        when(waveService.getWaves(any(org.springframework.data.domain.Pageable.class), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(wave)));
        when(waveService.getWaveById(9L)).thenReturn(Optional.empty());
        when(waveService.allocate(anyLong(), any(), any())).thenAnswer(invocation -> {
            WaveEntity allocated = new WaveEntity();
            allocated.setId(invocation.getArgument(0));
            allocated.setWaveNo("WAVE-002");
            allocated.setStatus("CREATED");
            allocated.setAssignedTo(invocation.getArgument(1));
            allocated.setRemark(invocation.getArgument(2));
            return allocated;
        });
        when(outboundOrderRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(List.of());
        when(pickingTaskRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(List.of());
        doNothing().when(outboundOrderRepository).flush();

        mockMvc.perform(get("/api/v1/wms/outbound/waves")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list[0].waveNo").value("WAVE-001"));

        mockMvc.perform(get("/api/v1/wms/outbound/waves/9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.msg").value("波次不存在"));

        mockMvc.perform(put("/api/v1/wms/outbound/waves/5/allocate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("assignedTo", "u01", "remark", "批量分配"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.assignedTo").value("u01"));
    }
}
