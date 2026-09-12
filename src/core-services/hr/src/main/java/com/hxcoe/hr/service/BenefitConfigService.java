package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.BenefitConfigEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 福利配置Service接口
 */
public interface BenefitConfigService {

    /**
     * 创建福利配置
     * @param benefitConfig 福利配置实体
     * @return 福利配置实体
     */
    BenefitConfigEntity createBenefitConfig(BenefitConfigEntity benefitConfig);

    /**
     * 根据ID查询福利配置
     * @param id 福利配置ID
     * @return 福利配置实体
     */
    BenefitConfigEntity getBenefitConfigById(Long id);

    /**
     * 更新福利配置
     * @param id 福利配置ID
     * @param benefitConfig 福利配置实体
     * @return 福利配置实体
     */
    BenefitConfigEntity updateBenefitConfig(Long id, BenefitConfigEntity benefitConfig);

    /**
     * 删除福利配置
     * @param id 福利配置ID
     */
    void deleteBenefitConfig(Long id);

    /**
     * 查询所有福利配置
     * @return 福利配置列表
     */
    List<BenefitConfigEntity> getAllBenefitConfigs();

    /**
     * 分页查询福利配置
     * @param pageable 分页参数
     * @return 福利配置分页列表
     */
    Page<BenefitConfigEntity> getBenefitConfigsByPage(Pageable pageable);
}
