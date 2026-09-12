package com.hxcoe.oa.iam.service;

import com.hxcoe.common.dto.hr.EmployeeEventDTO;
import com.hxcoe.oa.iam.entity.OaUserAccountEntity;
import com.hxcoe.oa.iam.repository.OaUserAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * OA 侧 HR 员工事件同步服务。
 * <p>接收 HR 员工入职/调动/离职事件，同步 OA 账号启用/停用状态，
 * 完成 HR→OA 事件落地闭环。</p>
 */
@Slf4j
@Service
public class OaHrEventSyncService {

    /** 账号状态：启用 */
    private static final String ACCOUNT_ACTIVE = "ACTIVE";
    /** 账号状态：停用 */
    private static final String ACCOUNT_DISABLED = "DISABLED";

    /** HR 事件类型：入职 */
    private static final String EVENT_ONBOARDED = "ONBOARDED";
    /** HR 事件类型：调动 */
    private static final String EVENT_TRANSFERRED = "TRANSFERRED";
    /** HR 事件类型：离职 */
    private static final String EVENT_RESIGNED = "RESIGNED";

    @Autowired
    private OaUserAccountRepository oaUserAccountRepository;

    /**
     * 应用 HR 员工事件，同步 OA 账号状态。
     * <p>入职→启用账号；离职→停用账号；调动→记录日志（账号无部门字段，无需更新）。
     * 账号不存在时记录日志并返回 true（不视为失败）。</p>
     *
     * @param event HR 员工事件
     * @return true=处理完成
     */
    @Transactional
    public boolean applyEmployeeEvent(EmployeeEventDTO event) {
        if (event == null || event.getEmployee() == null) {
            return false;
        }
        EmployeeEventDTO.EmployeePayload payload = event.getEmployee();
        Long employeeId = payload.getEmployeeId();
        String eventType = event.getEventType();

        if (employeeId == null) {
            log.warn("HR员工事件 OA：employeeId为空，跳过 eventId={}", event.getEventId());
            return false;
        }

        // 调动事件：账号无部门字段，仅记录日志
        if (EVENT_TRANSFERRED.equals(eventType)) {
            log.info("HR员工事件 OA 调动 employeeId={} employeeNo={} eventId={}", employeeId, payload.getEmployeeNo(), event.getEventId());
            return true;
        }

        Optional<OaUserAccountEntity> optional = oaUserAccountRepository.findByEmployeeId(employeeId);
        if (optional.isEmpty()) {
            log.info("HR员工事件 OA：账号不存在 employeeId={} eventType={}，跳过 eventId={}", employeeId, eventType, event.getEventId());
            return true;
        }

        OaUserAccountEntity account = optional.get();
        if (EVENT_ONBOARDED.equals(eventType)) {
            account.setStatus(ACCOUNT_ACTIVE);
        } else if (EVENT_RESIGNED.equals(eventType)) {
            account.setStatus(ACCOUNT_DISABLED);
        } else {
            log.warn("HR员工事件 OA：未知eventType={}，跳过 eventId={}", eventType, event.getEventId());
            return true;
        }
        oaUserAccountRepository.save(account);
        log.info("HR员工事件 OA 同步成功 eventType={} employeeId={} employeeNo={} accountStatus={} eventId={}",
                eventType, employeeId, payload.getEmployeeNo(), account.getStatus(), event.getEventId());
        return true;
    }
}
