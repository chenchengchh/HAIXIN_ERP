package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.CustomerTagEntity;
import com.hxcoe.crm.entity.CustomerTagRelationEntity;
import com.hxcoe.crm.repository.CustomerTagRepository;
import com.hxcoe.crm.repository.CustomerTagRelationRepository;
import com.hxcoe.crm.service.CustomerTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 客户标签服务实现类
 */
@Service
public class CustomerTagServiceImpl implements CustomerTagService {

    @Autowired
    private CustomerTagRepository tagRepository;

    @Autowired
    private CustomerTagRelationRepository tagRelationRepository;

    @Override
    public CustomerTagEntity createTag(CustomerTagEntity tagEntity) {
        // 设置默认值
        if (tagEntity.getSortOrder() == null) {
            // 获取当前最大排序值，设置为当前最大+1
            List<CustomerTagEntity> allTags = tagRepository.findAll();
            int maxSortOrder = allTags.stream()
                    .mapToInt(tag -> tag.getSortOrder() != null ? tag.getSortOrder() : 0)
                    .max()
                    .orElse(0);
            tagEntity.setSortOrder(maxSortOrder + 1);
        }
        return tagRepository.save(tagEntity);
    }

    @Override
    public CustomerTagEntity getTagById(Long id) {
        return tagRepository.findById(id).orElse(null);
    }

    @Override
    public CustomerTagEntity updateTag(CustomerTagEntity tagEntity) {
        return tagRepository.save(tagEntity);
    }

    @Override
    public boolean deleteTag(Long id) {
        CustomerTagEntity tag = tagRepository.findById(id).orElse(null);
        if (tag != null) {
            // 删除标签关联关系
            tagRelationRepository.deleteByTagId(id);
            // 删除标签
            tagRepository.delete(tag);
            return true;
        }
        return false;
    }

    @Override
    public List<CustomerTagEntity> getAllTags() {
        return tagRepository.findAll();
    }

    @Override
    public List<CustomerTagEntity> getTagsByType(String tagType) {
        return tagRepository.findByTagType(tagType);
    }

    @Override
    public List<CustomerTagEntity> getTagsByCategory(String tagCategory) {
        return tagRepository.findByTagCategory(tagCategory);
    }

    @Override
    public boolean addTagToCustomer(Long customerId, Long tagId) {
        // 检查关系是否已存在
        CustomerTagRelationEntity existingRelation = tagRelationRepository.findByCustomerIdAndTagId(customerId, tagId);
        if (existingRelation == null) {
            // 创建新关系
            CustomerTagRelationEntity relation = new CustomerTagRelationEntity();
            relation.setCustomerId(customerId);
            relation.setTagId(tagId);
            relation.setCreateTime(LocalDateTime.now());
            tagRelationRepository.save(relation);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeTagFromCustomer(Long customerId, Long tagId) {
        CustomerTagRelationEntity relation = tagRelationRepository.findByCustomerIdAndTagId(customerId, tagId);
        if (relation != null) {
            tagRelationRepository.delete(relation);
            return true;
        }
        return false;
    }

    @Override
    public boolean setTagsToCustomer(Long customerId, List<Long> tagIds) {
        // 先删除现有标签关系
        tagRelationRepository.deleteByCustomerId(customerId);
        
        // 添加新标签关系
        LocalDateTime now = LocalDateTime.now();
        List<CustomerTagRelationEntity> relations = tagIds.stream()
                .map(tagId -> {
                    CustomerTagRelationEntity relation = new CustomerTagRelationEntity();
                    relation.setCustomerId(customerId);
                    relation.setTagId(tagId);
                    relation.setCreateTime(now);
                    return relation;
                })
                .collect(Collectors.toList());
        tagRelationRepository.saveAll(relations);
        return true;
    }

    @Override
    public List<CustomerTagEntity> getTagsByCustomerId(Long customerId) {
        // 获取客户标签关系
        List<CustomerTagRelationEntity> relations = tagRelationRepository.findByCustomerId(customerId);
        // 获取标签ID列表
        List<Long> tagIds = relations.stream()
                .map(CustomerTagRelationEntity::getTagId)
                .collect(Collectors.toList());
        // 根据标签ID查询标签详情
        return tagRepository.findAllById(tagIds);
    }
}