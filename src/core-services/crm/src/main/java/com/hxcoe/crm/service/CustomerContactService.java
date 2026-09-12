package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.CustomerContactEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 客户联系人服务接口
 */
public interface CustomerContactService {

    /**
     * 创建联系人
     *
     * @param contactEntity 联系人实体
     * @return 保存后的联系人实体
     */
    CustomerContactEntity createContact(CustomerContactEntity contactEntity);

    /**
     * 根据ID查询联系人
     *
     * @param id 主键ID
     * @return 联系人实体
     */
    CustomerContactEntity getContactById(Long id);

    /**
     * 更新联系人
     *
     * @param contactEntity 联系人实体
     * @return 更新后的联系人实体
     */
    CustomerContactEntity updateContact(CustomerContactEntity contactEntity);

    /**
     * 删除联系人
     *
     * @param id 主键ID
     * @return 删除结果
     */
    boolean deleteContact(Long id);

    /**
     * 根据客户ID查询联系人列表
     *
     * @param customerId 客户ID
     * @return 联系人实体列表
     */
    List<CustomerContactEntity> getContactsByCustomerId(Long customerId);

    /**
     * 根据客户ID查询主要联系人
     *
     * @param customerId 客户ID
     * @return 主要联系人
     */
    CustomerContactEntity getPrimaryContactByCustomerId(Long customerId);

    /**
     * 设置主要联系人
     *
     * @param contactId 联系人ID
     * @param customerId 客户ID
     * @return 设置结果
     */
    boolean setPrimaryContact(Long contactId, Long customerId);

    /**
     * 全量分页查询联系人（支持客户名称/联系人姓名/职位模糊筛选，按id倒序，并填充客户名称）
     *
     * @param customerName 客户名称（可选，模糊匹配）
     * @param contactName  联系人姓名（可选，模糊匹配）
     * @param position     职位（可选，模糊匹配）
     * @param pageable     分页参数
     * @return 联系人分页结果
     */
    Page<CustomerContactEntity> getContactList(String customerName, String contactName, String position, Pageable pageable);
}