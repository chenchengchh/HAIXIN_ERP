package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.CustomerCategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 客户分类服务接口
 */
public interface CustomerCategoryService {

    /**
     * 创建分类
     *
     * @param category 分类实体
     * @return 创建后的分类
     */
    CustomerCategoryEntity createCategory(CustomerCategoryEntity category);

    /**
     * 根据ID查询分类
     *
     * @param id 主键ID
     * @return 分类
     */
    CustomerCategoryEntity getCategoryById(Long id);

    /**
     * 更新分类
     *
     * @param id       主键ID
     * @param category 分类实体
     * @return 更新后的分类，不存在时返回 null
     */
    CustomerCategoryEntity updateCategory(Long id, CustomerCategoryEntity category);

    /**
     * 删除分类（存在子分类时禁止删除）
     *
     * @param id 主键ID
     * @return 删除结果：0成功 1不存在 2存在子分类
     */
    int deleteCategory(Long id);

    /**
     * 分页查询分类列表
     *
     * @param keyword  关键词（名称/编码模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<CustomerCategoryEntity> getCategoryList(String keyword, Pageable pageable);

    /**
     * 查询所有分类（用于树状结构）
     *
     * @return 分类列表
     */
    List<CustomerCategoryEntity> getAllCategories();
}
