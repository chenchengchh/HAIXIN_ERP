package com.hxcoe.erp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.erp.dto.integration.PoFactRequest;
import com.hxcoe.erp.entity.FinanceTaskEntity;
import com.hxcoe.erp.entity.FinanceTaskRuleEntity;
import com.hxcoe.erp.entity.VoucherEntity;
import com.hxcoe.erp.repository.FinanceTaskLogRepository;
import com.hxcoe.erp.repository.FinanceTaskRepository;
import com.hxcoe.erp.repository.FinanceTaskRuleRepository;
import com.hxcoe.erp.support.FinanceTaskStatus;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;

class FinanceTaskProcessorServiceTest {

    @Test
    void tryProcessOneShouldCreateVoucherForReceiptCompletedWithWildcardRule() throws Exception {
        FinanceTaskRepository financeTaskRepository = mock(FinanceTaskRepository.class);
        FinanceTaskRuleRepository financeTaskRuleRepository = mock(FinanceTaskRuleRepository.class);
        FinanceTaskLogRepository financeTaskLogRepository = mock(FinanceTaskLogRepository.class);
        VoucherService voucherService = mock(VoucherService.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        FinanceTaskProcessorService service = new FinanceTaskProcessorService();
        ReflectionTestUtils.setField(service, "financeTaskRepository", financeTaskRepository);
        ReflectionTestUtils.setField(service, "financeTaskRuleRepository", financeTaskRuleRepository);
        ReflectionTestUtils.setField(service, "financeTaskLogRepository", financeTaskLogRepository);
        ReflectionTestUtils.setField(service, "voucherService", voucherService);
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);

        FinanceTaskEntity task = new FinanceTaskEntity();
        task.setId(2L);
        task.setFactType("RECEIPT_COMPLETED");
        task.setSourceNo("PO-001");
        task.setRefNo("RCPT-001");
        task.setPayloadJson("{\"fact\":{}}");
        task.setStatus(FinanceTaskStatus.PENDING);

        PoFactRequest req = new PoFactRequest();
        PoFactRequest.PoFactPayload payload = new PoFactRequest.PoFactPayload();
        payload.setFactType("RECEIPT_COMPLETED");
        payload.setPoNo("PO-001");
        payload.setRefNo("RCPT-001");
        req.setFact(payload);

        FinanceTaskRuleEntity rule = new FinanceTaskRuleEntity();
        rule.setFactType("RECEIPT_COMPLETED");
        rule.setResult("*");
        rule.setActionType(FinanceTaskProcessorService.ACTION_CREATE_VOUCHER);
        rule.setVoucherType("journal");
        rule.setEnabled(1);

        VoucherEntity voucher = new VoucherEntity();
        voucher.setId(99L);

        when(financeTaskRepository.updateStatusIfIn(anyLong(), anyList(), anyString())).thenReturn(1);
        when(financeTaskRepository.findById(2L)).thenReturn(Optional.of(task));
        when(objectMapper.readValue(anyString(), eq(PoFactRequest.class))).thenReturn(req);
        when(financeTaskRuleRepository.findFirstByFactTypeAndEnabledAndResultInOrderByIdAsc(
                eq("RECEIPT_COMPLETED"), eq(1), eq(List.of("*")))).thenReturn(Optional.of(rule));
        when(voucherService.createVoucher(any(VoucherEntity.class))).thenReturn(voucher);

        boolean processed = service.tryProcessOne(2L);

        assertTrue(processed);
        assertEquals(FinanceTaskStatus.CONFIRMED, task.getStatus());
        assertEquals(99L, task.getVoucherId());
        assertNull(task.getLastError());
        assertNotNull(task.getProcessedTime());
        verify(financeTaskRepository).save(task);
        verify(financeTaskLogRepository).save(any());
    }

    @Test
    void tryProcessOneShouldConfirmOnlyForFailedIqc() throws Exception {
        FinanceTaskRepository financeTaskRepository = mock(FinanceTaskRepository.class);
        FinanceTaskRuleRepository financeTaskRuleRepository = mock(FinanceTaskRuleRepository.class);
        FinanceTaskLogRepository financeTaskLogRepository = mock(FinanceTaskLogRepository.class);
        VoucherService voucherService = mock(VoucherService.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        FinanceTaskProcessorService service = new FinanceTaskProcessorService();
        ReflectionTestUtils.setField(service, "financeTaskRepository", financeTaskRepository);
        ReflectionTestUtils.setField(service, "financeTaskRuleRepository", financeTaskRuleRepository);
        ReflectionTestUtils.setField(service, "financeTaskLogRepository", financeTaskLogRepository);
        ReflectionTestUtils.setField(service, "voucherService", voucherService);
        ReflectionTestUtils.setField(service, "objectMapper", objectMapper);

        FinanceTaskEntity task = new FinanceTaskEntity();
        task.setId(1L);
        task.setFactType("IQC_COMPLETED");
        task.setResult("FAIL");
        task.setSourceNo("PO-002");
        task.setRefNo("QC-001");
        task.setPayloadJson("{\"fact\":{}}");
        task.setStatus(FinanceTaskStatus.FAILED);

        PoFactRequest req = new PoFactRequest();
        PoFactRequest.PoFactPayload payload = new PoFactRequest.PoFactPayload();
        payload.setFactType("IQC_COMPLETED");
        payload.setPoNo("PO-002");
        payload.setRefNo("QC-001");
        payload.setResult("FAIL");
        req.setFact(payload);

        FinanceTaskRuleEntity rule = new FinanceTaskRuleEntity();
        rule.setFactType("IQC_COMPLETED");
        rule.setResult("FAIL");
        rule.setActionType(FinanceTaskProcessorService.ACTION_CONFIRM_ONLY);
        rule.setEnabled(1);

        when(financeTaskRepository.updateStatusIfIn(anyLong(), anyList(), anyString())).thenReturn(1);
        when(financeTaskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(objectMapper.readValue(anyString(), eq(PoFactRequest.class))).thenReturn(req);
        when(financeTaskRuleRepository.findFirstByFactTypeAndEnabledAndResultInOrderByIdAsc(
                eq("IQC_COMPLETED"), eq(1), eq(List.of("FAIL", "*")))).thenReturn(Optional.of(rule));

        boolean processed = service.tryProcessOne(1L);

        assertTrue(processed);
        assertEquals(FinanceTaskStatus.CONFIRMED, task.getStatus());
        assertNull(task.getVoucherId());
        assertNull(task.getLastError());
        assertNotNull(task.getProcessedTime());
        verify(voucherService, never()).createVoucher(any());
        verify(financeTaskRepository).save(task);
        verify(financeTaskLogRepository).save(any());
    }
}
