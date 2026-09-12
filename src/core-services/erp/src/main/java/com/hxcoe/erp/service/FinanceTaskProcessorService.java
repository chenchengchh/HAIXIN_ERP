package com.hxcoe.erp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.erp.dto.integration.PoFactRequest;
import com.hxcoe.erp.entity.FinanceTaskEntity;
import com.hxcoe.erp.entity.FinanceTaskLogEntity;
import com.hxcoe.erp.entity.FinanceTaskRuleEntity;
import com.hxcoe.erp.entity.VoucherEntity;
import com.hxcoe.erp.repository.FinanceTaskLogRepository;
import com.hxcoe.erp.repository.FinanceTaskRepository;
import com.hxcoe.erp.repository.FinanceTaskRuleRepository;
import com.hxcoe.erp.support.FinanceTaskStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class FinanceTaskProcessorService {

    public static final String ACTION_CREATE_VOUCHER = "CREATE_VOUCHER";
    public static final String ACTION_CONFIRM_ONLY = "CONFIRM_ONLY";

    @Autowired
    private FinanceTaskRepository financeTaskRepository;

    @Autowired
    private FinanceTaskRuleRepository financeTaskRuleRepository;

    @Autowired
    private FinanceTaskLogRepository financeTaskLogRepository;

    @Autowired
    private VoucherService voucherService;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public int tryProcessPendingBatch() {
        List<FinanceTaskEntity> tasks = financeTaskRepository.findTop50ByStatusInOrderByCreatedTimeAsc(
                List.of(FinanceTaskStatus.PENDING, FinanceTaskStatus.FAILED)
        );
        int processed = 0;
        for (FinanceTaskEntity task : tasks) {
            if (task == null) {
                continue;
            }
            if (FinanceTaskStatus.CONFIRMED.equalsIgnoreCase(task.getStatus())) {
                continue;
            }
            tryProcessOne(task.getId());
            processed++;
        }
        return processed;
    }

    @Transactional
    public boolean tryProcessOne(Long taskId) {
        if (taskId == null) {
            return false;
        }
        int claimed = financeTaskRepository.updateStatusIfIn(
                taskId,
                List.of(FinanceTaskStatus.PENDING, FinanceTaskStatus.FAILED),
                FinanceTaskStatus.PROCESSING
        );
        if (claimed != 1) {
            return false;
        }

        FinanceTaskEntity task = financeTaskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return false;
        }

        PoFactRequest req;
        try {
            req = task.getPayloadJson() == null || task.getPayloadJson().isBlank()
                    ? null
                    : objectMapper.readValue(task.getPayloadJson(), PoFactRequest.class);
        } catch (Exception ex) {
            markFailed(task, "payload解析失败:" + ex.getMessage());
            return false;
        }

        if (req == null || req.getFact() == null) {
            markFailed(task, "payload为空");
            return false;
        }

        String factType = task.getFactType();
        String result = task.getResult();
        List<String> resultCandidates = result == null || result.isBlank() ? List.of("*") : List.of(result, "*");

        Optional<FinanceTaskRuleEntity> ruleOpt = financeTaskRuleRepository
                .findFirstByFactTypeAndEnabledAndResultInOrderByIdAsc(factType, 1, resultCandidates);
        if (ruleOpt.isEmpty()) {
            markFailed(task, "无匹配处理规则 factType=" + factType + " result=" + result);
            return false;
        }

        FinanceTaskRuleEntity rule = ruleOpt.get();
        String actionType = rule.getActionType();

        try {
            if (ACTION_CREATE_VOUCHER.equals(actionType)) {
                VoucherEntity voucher = new VoucherEntity();
                voucher.setVoucherDate(LocalDateTime.now());
                voucher.setVoucherType(rule.getVoucherType() == null || rule.getVoucherType().isBlank() ? "journal" : rule.getVoucherType());
                voucher.setSummary(buildSummary(task));
                voucher.setStatus("draft");
                voucher.setAttachmentCount(0);
                voucher.setCreatedBy("system");
                voucher.setUpdatedBy("system");
                voucher.setIsDeleted(0);
                VoucherEntity saved = voucherService.createVoucher(voucher);
                task.setVoucherId(saved == null ? null : saved.getId());
                appendLog(task.getId(), ACTION_CREATE_VOUCHER, saved == null ? "凭证创建失败" : ("voucherId=" + saved.getId()));
            } else if (ACTION_CONFIRM_ONLY.equals(actionType)) {
                appendLog(task.getId(), ACTION_CONFIRM_ONLY, "已确认");
            } else {
                markFailed(task, "未知actionType=" + actionType);
                return false;
            }

            task.setStatus(FinanceTaskStatus.CONFIRMED);
            task.setLastError(null);
            task.setProcessedTime(LocalDateTime.now());
            financeTaskRepository.save(task);
            return true;
        } catch (Exception ex) {
            markFailed(task, ex.getMessage());
            return false;
        }
    }

    private void markFailed(FinanceTaskEntity task, String error) {
        int next = task.getRetryCount() == null ? 1 : task.getRetryCount() + 1;
        task.setRetryCount(next);
        task.setStatus(FinanceTaskStatus.FAILED);
        task.setLastError(error);
        financeTaskRepository.save(task);
        appendLog(task.getId(), FinanceTaskStatus.FAILED, error);
        log.warn("ERP财务待办处理失败 taskId={} factType={} sourceNo={} idempotencyKey={} retry={} error={}",
                task.getId(), task.getFactType(), task.getSourceNo(), task.getIdempotencyKey(), next, error);
    }

    private void appendLog(Long taskId, String action, String message) {
        if (taskId == null) {
            return;
        }
        FinanceTaskLogEntity logEntity = new FinanceTaskLogEntity();
        logEntity.setTaskId(taskId);
        logEntity.setAction(action);
        logEntity.setMessage(message);
        financeTaskLogRepository.save(logEntity);
    }

    private String buildSummary(FinanceTaskEntity task) {
        String sourceNo = task.getSourceNo() == null ? "" : task.getSourceNo();
        String refNo = task.getRefNo() == null ? "" : task.getRefNo();
        String factType = task.getFactType() == null ? "" : task.getFactType();
        return "AUTO:" + factType + ":" + sourceNo + (refNo.isBlank() ? "" : (":" + refNo));
    }
}
