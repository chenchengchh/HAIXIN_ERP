package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.CustomerFollowUpEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 客户跟进记录服务接口
 */
public interface CustomerFollowUpService {

    /**
     * 创建跟进记录
     *
     * @param followUpEntity 跟进记录实体
     * @return 保存后的跟进记录实体
     */
    CustomerFollowUpEntity createFollowUp(CustomerFollowUpEntity followUpEntity);

    /**
     * 根据ID查询跟进记录
     *
     * @param id 主键ID
     * @return 跟进记录实体
     */
    CustomerFollowUpEntity getFollowUpById(Long id);

    /**
     * 更新跟进记录
     *
     * @param followUpEntity 跟进记录实体
     * @return 更新后的跟进记录实体
     */
    CustomerFollowUpEntity updateFollowUp(CustomerFollowUpEntity followUpEntity);

    /**
     * 删除跟进记录
     *
     * @param id 主键ID
     * @return 删除结果
     */
    boolean deleteFollowUp(Long id);

    /**
     * 根据客户ID查询跟进记录列表
     *
     * @param customerId 客户ID
     * @return 跟进记录实体列表
     */
    List<CustomerFollowUpEntity> getFollowUpsByCustomerId(Long customerId);

    /**
     * 根据跟进人ID查询跟进记录列表
     *
     * @param followUpUserId 跟进人ID
     * @return 跟进记录实体列表
     */
    List<CustomerFollowUpEntity> getFollowUpsByFollowUpUserId(Long followUpUserId);

    /**
     * 查询我的待跟进客户
     *
     * @param followUpUserId 跟进人ID
     * @return 待跟进客户的跟进记录列表
     */
    List<CustomerFollowUpEntity> getMyPendingFollowUps(Long followUpUserId);

    /**
     * 全量分页查询跟进记录（支持客户名称模糊、跟进方式、跟进时间范围筛选，按id倒序，并填充客户名称）
     *
     * @param customerName 客户名称（可选，模糊匹配）
     * @param followUpType 跟进方式（可选）
     * @param startTime    跟进时间起（可选）
     * @param endTime      跟进时间止（可选）
     * @param pageable     分页参数
     * @return 跟进记录分页结果
     */
    Page<CustomerFollowUpEntity> getFollowUpList(String customerName, String followUpType,
                                                 LocalDateTime startTime, LocalDateTime endTime,
                                                 Pageable pageable);
}