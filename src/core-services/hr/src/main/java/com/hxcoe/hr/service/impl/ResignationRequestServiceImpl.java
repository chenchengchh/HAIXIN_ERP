package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.EmployeeEntity;
import com.hxcoe.hr.entity.ResignationRequestEntity;
import com.hxcoe.hr.repository.EmployeeRepository;
import com.hxcoe.hr.repository.ResignationRequestRepository;
import com.hxcoe.hr.service.HrEventOutboxService;
import com.hxcoe.hr.service.ResignationRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 离职申请Service实现类
 */
@Service
public class ResignationRequestServiceImpl implements ResignationRequestService {

    /** 离职审批状态：已批准 */
    private static final int STATUS_APPROVED = 1;

    @Autowired
    private ResignationRequestRepository resignationRequestRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private HrEventOutboxService hrEventOutboxService;

    @Override
    public ResignationRequestEntity createResignationRequest(ResignationRequestEntity resignationRequest) {
        return resignationRequestRepository.save(resignationRequest);
    }

    @Override
    public ResignationRequestEntity getResignationRequestById(Long id) {
        return resignationRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("离职申请不存在: " + id));
    }

    @Override
    @Transactional
    public ResignationRequestEntity updateResignationRequest(Long id, ResignationRequestEntity resignationRequest) {
        ResignationRequestEntity existingRequest = getResignationRequestById(id);
        Integer beforeStatus = existingRequest.getStatus();
        // 更新离职申请字段
        existingRequest.setEmployeeId(resignationRequest.getEmployeeId());
        existingRequest.setResignationType(resignationRequest.getResignationType());
        existingRequest.setReason(resignationRequest.getReason());
        existingRequest.setApplyDate(resignationRequest.getApplyDate());
        existingRequest.setExpectedResignDate(resignationRequest.getExpectedResignDate());
        existingRequest.setActualResignDate(resignationRequest.getActualResignDate());
        existingRequest.setStatus(resignationRequest.getStatus());
        existingRequest.setRemark(resignationRequest.getRemark());
        ResignationRequestEntity saved = resignationRequestRepository.save(existingRequest);

        // P2-C: 离职审批通过（status 0→1）时同事务入队 OA/ERP 离职事件
        Integer afterStatus = saved.getStatus();
        if (afterStatus != null && afterStatus == STATUS_APPROVED
                && (beforeStatus == null || beforeStatus != STATUS_APPROVED)) {
            EmployeeEntity employee = employeeRepository.findById(saved.getEmployeeId()).orElse(null);
            if (employee != null) {
                hrEventOutboxService.enqueueEmployeeResigned(saved, employee);
            }
        }
        return saved;
    }

    @Override
    public void deleteResignationRequest(Long id) {
        ResignationRequestEntity existingRequest = getResignationRequestById(id);
        resignationRequestRepository.delete(existingRequest);
    }

    @Override
    public List<ResignationRequestEntity> getAllResignationRequests() {
        return resignationRequestRepository.findAll();
    }

    @Override
    public Page<ResignationRequestEntity> getResignationRequestsByPage(Pageable pageable) {
        return resignationRequestRepository.findAll(pageable);
    }

    @Override
    public List<ResignationRequestEntity> getResignationRequestsByEmployeeId(Long employeeId) {
        return resignationRequestRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<ResignationRequestEntity> getResignationRequestsByStatus(Integer status) {
        return resignationRequestRepository.findByStatus(status);
    }
}