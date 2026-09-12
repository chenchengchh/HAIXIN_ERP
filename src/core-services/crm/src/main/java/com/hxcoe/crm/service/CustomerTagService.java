package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.CustomerTagEntity;

import java.util.List;

/**
 * 客户标签服务接口
 */
public interface CustomerTagService {

    /**
     * 创建标签
     *
     * @param tagEntity 标签实体
     * @return 保存后的标签实体
     */
    CustomerTagEntity createTag(CustomerTagEntity tagEntity);

    /**
     * 根据ID查询标签
     *
     * @param id 主键ID
     * @return 标签实体
     */
    CustomerTagEntity getTagById(Long id);

    /**
     * 更新标签
     *
     * @param tagEntity 标签实体
     * @return 更新后的标签实体
     */
    CustomerTagEntity updateTag(CustomerTagEntity tagEntity);

    /**
     * 删除标签
     *
     * @param id 主键ID
     * @return 删除结果
     */
    boolean deleteTag(Long id);

    /**
     * 查询所有标签
     *
     * @return 标签实体列表
     */
    List<CustomerTagEntity> getAllTags();

    /**
     * 根据标签类型查询标签
     *
     * @param tagType 标签类型
     * @return 标签实体列表
     */
    List<CustomerTagEntity> getTagsByType(String tagType);

    /**
     * 根据标签分类查询标签
     *
     * @param tagCategory 标签分类
     * @return 标签实体列表
     */
    List<CustomerTagEntity> getTagsByCategory(String tagCategory);

    /**
     * 为客户添加标签
     *
     * @param customerId 客户ID
     * @param tagId 标签ID
     * @return 添加结果
     */
    boolean addTagToCustomer(Long customerId, Long tagId);

    /**
     * 从客户移除标签
     *
     * @param customerId 客户ID
     * @param tagId 标签ID
     * @return 移除结果
     */
    boolean removeTagFromCustomer(Long customerId, Long tagId);

    /**
     * 为客户设置多个标签
     *
     * @param customerId 客户ID
     * @param tagIds 标签ID列表
     * @return 设置结果
     */
    boolean setTagsToCustomer(Long customerId, List<Long> tagIds);

    /**
     * 查询客户的所有标签
     *
     * @param customerId 客户ID
     * @return 标签实体列表
     */
    List<CustomerTagEntity> getTagsByCustomerId(Long customerId);
}