package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.EmployeeEntity;
import com.hxcoe.hr.entity.TransferRecordEntity;
import com.hxcoe.hr.repository.EmployeeRepository;
import com.hxcoe.hr.repository.TransferRecordRepository;
import com.hxcoe.hr.service.HrEventOutboxService;
import com.hxcoe.hr.service.TransferRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 转岗记录Service实现类
 */
@Service
public class TransferRecordServiceImpl implements TransferRecordService {

    /** 调动审批状态：已通过 */
    private static final int STATUS_APPROVED = 1;

    @Autowired
    private TransferRecordRepository transferRecordRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private HrEventOutboxService hrEventOutboxService;

    @Override
    public TransferRecordEntity createTransferRecord(TransferRecordEntity transferRecord) {
        return transferRecordRepository.save(transferRecord);
    }

    @Override
    public TransferRecordEntity getTransferRecordById(Long id) {
        return transferRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("转岗记录不存在: " + id));
    }

    @Override
    @Transactional
    public TransferRecordEntity updateTransferRecord(Long id, TransferRecordEntity transferRecord) {
        TransferRecordEntity existingRecord = getTransferRecordById(id);
        Integer beforeStatus = existingRecord.getStatus();
        // 更新转岗记录字段
        existingRecord.setEmployeeId(transferRecord.getEmployeeId());
        existingRecord.setOldDepartmentId(transferRecord.getOldDepartmentId());
        existingRecord.setNewDepartmentId(transferRecord.getNewDepartmentId());
        existingRecord.setOldPositionId(transferRecord.getOldPositionId());
        existingRecord.setNewPositionId(transferRecord.getNewPositionId());
        existingRecord.setReason(transferRecord.getReason());
        existingRecord.setTransferDate(transferRecord.getTransferDate());
        existingRecord.setStatus(transferRecord.getStatus());
        existingRecord.setRemark(transferRecord.getRemark());
        TransferRecordEntity saved = transferRecordRepository.save(existingRecord);

        // P2-C: 调动审批通过（status 0→1）时同事务入队 OA/ERP 调动事件
        Integer afterStatus = saved.getStatus();
        if (afterStatus != null && afterStatus == STATUS_APPROVED
                && (beforeStatus == null || beforeStatus != STATUS_APPROVED)) {
            EmployeeEntity employee = employeeRepository.findById(saved.getEmployeeId()).orElse(null);
            if (employee != null) {
                hrEventOutboxService.enqueueEmployeeTransferred(saved, employee);
            }
        }
        return saved;
    }

    @Override
    public void deleteTransferRecord(Long id) {
        TransferRecordEntity existingRecord = getTransferRecordById(id);
        transferRecordRepository.delete(existingRecord);
    }

    @Override
    public List<TransferRecordEntity> getAllTransferRecords() {
        return transferRecordRepository.findAll();
    }

    @Override
    public Page<TransferRecordEntity> getTransferRecordsByPage(Pageable pageable) {
        return transferRecordRepository.findAll(pageable);
    }

    @Override
    public List<TransferRecordEntity> getTransferRecordsByEmployeeId(Long employeeId) {
        return transferRecordRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<TransferRecordEntity> getTransferRecordsByStatus(Integer status) {
        return transferRecordRepository.findByStatus(status);
    }
}