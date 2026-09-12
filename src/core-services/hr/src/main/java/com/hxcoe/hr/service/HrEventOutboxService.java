package com.hxcoe.hr.service;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.dto.hr.EmployeeEventDTO;
import com.hxcoe.common.result.Result;
import com.hxcoe.hr.client.ErpHrEventClient;
import com.hxcoe.hr.client.OaHrEventClient;
import com.hxcoe.hr.entity.EmployeeEntity;
import com.hxcoe.hr.entity.IntegrationOutboxEntity;
import com.hxcoe.hr.entity.ResignationRequestEntity;
import com.hxcoe.hr.entity.TransferRecordEntity;
import com.hxcoe.hr.repository.EmployeeRepository;
import com.hxcoe.hr.repository.IntegrationOutboxRepository;
import com.hxcoe.hr.repository.ResignationRequestRepository;
import com.hxcoe.hr.repository.TransferRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * HR 员工事件 Outbox 服务。
 * <p>员工入职/调动/离职审批通过后幂等入队 OA/ERP 两条事件，由重试任务异步投递，
 * OA 同步账号启停、ERP 同步员工主数据镜像，完成 HR 事件落地闭环。</p>
 */
@Slf4j
@Service
public class HrEventOutboxService {

    /** 事件业务类型 */
    public static final String BIZ_ONBOARDED = "ONBOARDED";
    public static final String BIZ_TRANSFERRED = "TRANSFERRED";
    public static final String BIZ_RESIGNED = "RESIGNED";

    /** 下游后缀 */
    private static final String SUFFIX_OA = "_OA";
    private static final String SUFFIX_ERP = "_ERP";

    @Autowired
    private IntegrationOutboxRepository outboxRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TransferRecordRepository transferRecordRepository;

    @Autowired
    private ResignationRequestRepository resignationRequestRepository;

    @Autowired(required = false)
    private OaHrEventClient oaHrEventClient;

    @Autowired(required = false)
    private ErpHrEventClient erpHrEventClient;

    /**
     * 员工入职事件入队（创建员工时调用）。
     *
     * @param employee 员工实体
     */
    @Transactional
    public void enqueueEmployeeOnboarded(EmployeeEntity employee) {
        if (employee == null || employee.getId() == null || employee.getEmployeeCode() == null) {
            return;
        }
        String refNo = employee.getEmployeeCode();
        enqueueIfAbsent(BIZ_ONBOARDED + SUFFIX_OA, refNo, employee.getId(), employee.getEmployeeCode());
        enqueueIfAbsent(BIZ_ONBOARDED + SUFFIX_ERP, refNo, employee.getId(), employee.getEmployeeCode());
    }

    /**
     * 员工调动事件入队（调动审批通过时调用）。
     *
     * @param transfer  调动记录
     * @param employee  员工实体
     */
    @Transactional
    public void enqueueEmployeeTransferred(TransferRecordEntity transfer, EmployeeEntity employee) {
        if (transfer == null || transfer.getId() == null || employee == null || employee.getEmployeeCode() == null) {
            return;
        }
        String refNo = employee.getEmployeeCode() + ":" + transfer.getId();
        enqueueIfAbsent(BIZ_TRANSFERRED + SUFFIX_OA, refNo, transfer.getId(), employee.getEmployeeCode());
        enqueueIfAbsent(BIZ_TRANSFERRED + SUFFIX_ERP, refNo, transfer.getId(), employee.getEmployeeCode());
    }

    /**
     * 员工离职事件入队（离职审批通过时调用）。
     *
     * @param resignation 离职申请
     * @param employee    员工实体
     */
    @Transactional
    public void enqueueEmployeeResigned(ResignationRequestEntity resignation, EmployeeEntity employee) {
        if (resignation == null || resignation.getId() == null || employee == null || employee.getEmployeeCode() == null) {
            return;
        }
        String refNo = employee.getEmployeeCode() + ":" + resignation.getId();
        enqueueIfAbsent(BIZ_RESIGNED + SUFFIX_OA, refNo, resignation.getId(), employee.getEmployeeCode());
        enqueueIfAbsent(BIZ_RESIGNED + SUFFIX_ERP, refNo, resignation.getId(), employee.getEmployeeCode());
    }

    /**
     * 幂等入队单条 Outbox 事件。
     *
     * @param eventType    事件类型
     * @param refNo        业务引用号
     * @param entityId     实体 ID
     * @param partitionKey 分区键（员工编号）
     */
    private void enqueueIfAbsent(String eventType, String refNo, Long entityId, String partitionKey) {
        outboxRepository.findByEventTypeAndRefNo(eventType, refNo).orElseGet(() -> {
            IntegrationOutboxEntity e = new IntegrationOutboxEntity();
            e.setEventType(eventType);
            e.setRefNo(refNo);
            e.setEntityId(entityId);
            String eventId = UUID.randomUUID().toString();
            e.setEventId(eventId);
            e.setTraceId(eventId);
            e.setProducer("hr-service");
            e.setEventVersion(1);
            e.setPartitionKey(partitionKey);
            e.setIdempotencyKey(eventType + ":" + refNo);
            e.setStatus("PENDING");
            e.setRetryCount(0);
            e.setNextRetryAt(LocalDateTime.now());
            return outboxRepository.save(e);
        });
    }

    /**
     * 发送单条 Outbox 事件到对应下游（OA/ERP）。
     *
     * @param outbox Outbox 任务实体
     */
    @Transactional
    public void trySendOne(IntegrationOutboxEntity outbox) {
        if (outbox == null) {
            return;
        }
        EmployeeEventDTO event = buildEvent(outbox);
        if (event == null) {
            markFailed(outbox, "构造事件DTO失败（员工不存在）");
            return;
        }

        String eventType = outbox.getEventType();
        try {
            boolean success;
            if (eventType.endsWith(SUFFIX_OA)) {
                if (oaHrEventClient == null) {
                    markFailed(outbox, "OA事件客户端不可用");
                    return;
                }
                Result<java.util.Map<String, Object>> res = oaHrEventClient.employeeEvent(event);
                success = res != null && ResponseStatusAdapter.isSuccess(res.getCode());
                if (!success) {
                    markFailed(outbox, res == null ? "OA返回为空" : ("OA失败:" + res.getMsg()));
                    return;
                }
            } else if (eventType.endsWith(SUFFIX_ERP)) {
                if (erpHrEventClient == null) {
                    markFailed(outbox, "ERP事件客户端不可用");
                    return;
                }
                ApiResponse<java.util.Map<String, Object>> res = erpHrEventClient.employeeEvent(event);
                success = res != null && ResponseStatusAdapter.isSuccess(res.getCode());
                if (!success) {
                    markFailed(outbox, res == null ? "ERP返回为空" : ("ERP失败:" + res.getMessage()));
                    return;
                }
            } else {
                markFailed(outbox, "未知eventType: " + eventType);
                return;
            }
            outbox.setStatus("SENT");
            outbox.setLastError(null);
            outbox.setNextRetryAt(null);
            outboxRepository.save(outbox);
            log.info("HR->{} 员工事件投递成功 eventId={} eventType={} employeeNo={}",
                    eventType, outbox.getEventId(), eventType, outbox.getPartitionKey());
        } catch (Exception ex) {
            markFailed(outbox, ex.getMessage());
        }
    }

    /**
     * 批量扫描 PENDING/FAILED 任务并发送。
     *
     * @return 本轮处理条数
     */
    @Transactional
    public int trySendPendingBatch() {
        List<IntegrationOutboxEntity> list = outboxRepository.findTop50ByStatusInAndNextRetryAtBeforeOrderByNextRetryAtAsc(
                List.of("PENDING", "FAILED"),
                LocalDateTime.now()
        );
        int processed = 0;
        for (IntegrationOutboxEntity e : list) {
            if ("SENT".equalsIgnoreCase(e.getStatus())) {
                continue;
            }
            trySendOne(e);
            processed++;
        }
        return processed;
    }

    /**
     * 根据 Outbox 任务构造员工事件 DTO。
     * <p>根据 eventType 前缀决定查询哪个业务实体。</p>
     *
     * @param outbox Outbox 任务
     * @return 事件 DTO（员工不存在时返回 null）
     */
    private EmployeeEventDTO buildEvent(IntegrationOutboxEntity outbox) {
        String eventType = outbox.getEventType();
        String bizType = eventType.replace(SUFFIX_OA, "").replace(SUFFIX_ERP, "");
        Long entityId = outbox.getEntityId();
        EmployeeEntity employee = null;
        String reason = null;

        switch (bizType) {
            case BIZ_ONBOARDED:
                employee = employeeRepository.findById(entityId).orElse(null);
                break;
            case BIZ_TRANSFERRED:
                TransferRecordEntity transfer = transferRecordRepository.findById(entityId).orElse(null);
                if (transfer != null) {
                    employee = employeeRepository.findById(transfer.getEmployeeId()).orElse(null);
                    reason = transfer.getReason();
                }
                break;
            case BIZ_RESIGNED:
                ResignationRequestEntity resignation = resignationRequestRepository.findById(entityId).orElse(null);
                if (resignation != null) {
                    employee = employeeRepository.findById(resignation.getEmployeeId()).orElse(null);
                    reason = resignation.getReason();
                }
                break;
            default:
                return null;
        }
        if (employee == null) {
            return null;
        }

        EmployeeEventDTO event = new EmployeeEventDTO();
        event.setEventId(outbox.getEventId());
        event.setTraceId(outbox.getTraceId());
        event.setEventKey(bizType + ":" + outbox.getRefNo());
        event.setIdempotencyKey(outbox.getIdempotencyKey());
        event.setProducer(outbox.getProducer());
        event.setEventVersion(outbox.getEventVersion());
        event.setPartitionKey(outbox.getPartitionKey());
        event.setEventTime(LocalDateTime.now());
        event.setEventType(bizType);

        EmployeeEventDTO.EmployeePayload payload = new EmployeeEventDTO.EmployeePayload();
        payload.setEmployeeId(employee.getId());
        payload.setEmployeeNo(employee.getEmployeeCode());
        payload.setEmployeeName(employee.getName());
        payload.setDepartmentId(employee.getDepartmentId());
        payload.setPositionId(employee.getPositionId());
        payload.setStatus(employee.getStatus());
        payload.setReason(reason);
        if (employee.getHireDate() != null) {
            payload.setHireDate(employee.getHireDate().atStartOfDay());
        }
        if (employee.getLeaveDate() != null) {
            payload.setLeaveDate(employee.getLeaveDate().atStartOfDay());
        }
        event.setEmployee(payload);
        return event;
    }

    /**
     * 标记任务失败并设置退避重试时间。
     *
     * @param outbox Outbox 任务
     * @param error  错误信息
     */
    private void markFailed(IntegrationOutboxEntity outbox, String error) {
        int next = outbox.getRetryCount() == null ? 1 : outbox.getRetryCount() + 1;
        outbox.setRetryCount(next);
        outbox.setStatus("FAILED");
        outbox.setLastError(error);
        outbox.setNextRetryAt(LocalDateTime.now().plusSeconds(Math.min(600L, 5L * next)));
        outboxRepository.save(outbox);
        log.warn("HR员工事件投递失败 eventId={} traceId={} eventType={} refNo={} retry={} error={}",
                outbox.getEventId(), outbox.getTraceId(), outbox.getEventType(), outbox.getRefNo(), next, error);
    }
}
