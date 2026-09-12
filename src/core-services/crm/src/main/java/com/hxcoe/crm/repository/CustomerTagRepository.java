package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.CustomerTagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 客户标签Repository接口
 */
@Repository
public interface CustomerTagRepository extends JpaRepository<CustomerTagEntity, Long>, JpaSpecificationExecutor<CustomerTagEntity> {

    /**
     * 根据标签类型查询标签
     *
     * @param tagType 标签类型
     * @return 标签列表
     */
    List<CustomerTagEntity> findByTagType(String tagType);

    /**
     * 根据标签分类查询标签
     *
     * @param tagCategory 标签分类
     * @return 标签列表
     */
    List<CustomerTagEntity> findByTagCategory(String tagCategory);

    /**
     * 根据标签名称查询标签
     *
     * @param tagName 标签名称
     * @return 标签
     */
    CustomerTagEntity findByTagName(String tagName);
}