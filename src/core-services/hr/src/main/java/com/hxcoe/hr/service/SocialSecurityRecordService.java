package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.SocialSecurityRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 社保公积金记录Service接口
 */
public interface SocialSecurityRecordService {

    /**
     * 创建社保公积金记录
     * @param record 社保公积金记录实体
     * @return 社保公积金记录实体
     */
    SocialSecurityRecordEntity createRecord(SocialSecurityRecordEntity record);

    /**
     * 根据ID查询社保公积金记录
     * @param id 社保公积金记录ID
     * @return 社保公积金记录实体
     */
    SocialSecurityRecordEntity getRecordById(Long id);

    /**
     * 更新社保公积金记录
     * @param id 社保公积金记录ID
     * @param record 社保公积金记录实体
     * @return 社保公积金记录实体
     */
    SocialSecurityRecordEntity updateRecord(Long id, SocialSecurityRecordEntity record);

    /**
     * 删除社保公积金记录
     * @param id 社保公积金记录ID
     */
    void deleteRecord(Long id);

    /**
     * 查询全部社保公积金记录
     * @return 社保公积金记录列表
     */
    List<SocialSecurityRecordEntity> getAllRecords();

    /**
     * 分页查询社保公积金记录
     * @param pageable 分页参数
     * @return 社保公积金记录分页列表
     */
    Page<SocialSecurityRecordEntity> getRecordsByPage(Pageable pageable);

    /**
     * 按月份为所有在职员工计算并生成社保公积金记录
     * @param month 缴费月份（YYYY-MM）
     * @return 生成记录条数
     */
    int calculateByMonth(String month);

    /**
     * 按月份申报社保公积金记录（将未申报记录置为已申报）
     * @param month 缴费月份（YYYY-MM）
     * @return 更新记录条数
     */
    int declareByMonth(String month);
}
