package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.TransferRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 转岗记录Service接口
 */
public interface TransferRecordService {

    /**
     * 创建转岗记录
     * @param transferRecord 转岗记录实体
     * @return 转岗记录实体
     */
    TransferRecordEntity createTransferRecord(TransferRecordEntity transferRecord);

    /**
     * 根据ID查询转岗记录
     * @param id 转岗记录ID
     * @return 转岗记录实体
     */
    TransferRecordEntity getTransferRecordById(Long id);

    /**
     * 更新转岗记录
     * @param id 转岗记录ID
     * @param transferRecord 转岗记录实体
     * @return 转岗记录实体
     */
    TransferRecordEntity updateTransferRecord(Long id, TransferRecordEntity transferRecord);

    /**
     * 删除转岗记录
     * @param id 转岗记录ID
     */
    void deleteTransferRecord(Long id);

    /**
     * 查询所有转岗记录
     * @return 转岗记录列表
     */
    List<TransferRecordEntity> getAllTransferRecords();

    /**
     * 分页查询转岗记录
     * @param pageable 分页参数
     * @return 转岗记录分页列表
     */
    Page<TransferRecordEntity> getTransferRecordsByPage(Pageable pageable);

    /**
     * 根据员工ID查询转岗记录
     * @param employeeId 员工ID
     * @return 转岗记录列表
     */
    List<TransferRecordEntity> getTransferRecordsByEmployeeId(Long employeeId);

    /**
     * 根据状态查询转岗记录
     * @param status 状态
     * @return 转岗记录列表
     */
    List<TransferRecordEntity> getTransferRecordsByStatus(Integer status);
}