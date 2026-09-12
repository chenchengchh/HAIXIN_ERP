package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.BenefitRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 福利发放记录Service接口
 */
public interface BenefitRecordService {

    /**
     * 创建福利发放记录
     * @param benefitRecord 福利发放记录实体
     * @return 福利发放记录实体
     */
    BenefitRecordEntity createBenefitRecord(BenefitRecordEntity benefitRecord);

    /**
     * 根据ID查询福利发放记录
     * @param id 福利发放记录ID
     * @return 福利发放记录实体
     */
    BenefitRecordEntity getBenefitRecordById(Long id);

    /**
     * 更新福利发放记录
     * @param id 福利发放记录ID
     * @param benefitRecord 福利发放记录实体
     * @return 福利发放记录实体
     */
    BenefitRecordEntity updateBenefitRecord(Long id, BenefitRecordEntity benefitRecord);

    /**
     * 删除福利发放记录
     * @param id 福利发放记录ID
     */
    void deleteBenefitRecord(Long id);

    /**
     * 查询所有福利发放记录
     * @return 福利发放记录列表
     */
    List<BenefitRecordEntity> getAllBenefitRecords();

    /**
     * 分页查询福利发放记录
     * @param pageable 分页参数
     * @return 福利发放记录分页列表
     */
    Page<BenefitRecordEntity> getBenefitRecordsByPage(Pageable pageable);

    /**
     * 为指定福利配置生成发放记录（为所有在职员工生成，已存在的跳过）
     * @param benefitId 福利配置ID
     * @return 生成的发放记录条数
     */
    int distributeByBenefitId(Long benefitId);
}
