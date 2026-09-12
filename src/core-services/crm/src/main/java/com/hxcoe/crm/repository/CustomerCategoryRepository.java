package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.CustomerCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 客户分类Repository接口
 */
@Repository
public interface CustomerCategoryRepository extends JpaRepository<CustomerCategoryEntity, Long>, JpaSpecificationExecutor<CustomerCategoryEntity> {

    /**
     * 根据父分类ID查询子分类
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<CustomerCategoryEntity> findByParentId(Long parentId);

    /**
     * 根据分类编码查询分类
     *
     * @param categoryCode 分类编码
     * @return 分类
     */
    CustomerCategoryEntity findByCategoryCode(String categoryCode);
}
