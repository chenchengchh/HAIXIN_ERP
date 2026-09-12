package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.BenefitConfigEntity;
import com.hxcoe.hr.repository.BenefitConfigRepository;
import com.hxcoe.hr.service.BenefitConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 福利配置Service实现类
 */
@Service
public class BenefitConfigServiceImpl implements BenefitConfigService {

    @Autowired
    private BenefitConfigRepository benefitConfigRepository;

    @Override
    public BenefitConfigEntity createBenefitConfig(BenefitConfigEntity benefitConfig) {
        return benefitConfigRepository.save(benefitConfig);
    }

    @Override
    public BenefitConfigEntity getBenefitConfigById(Long id) {
        return benefitConfigRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("福利配置不存在: " + id));
    }

    @Override
    public BenefitConfigEntity updateBenefitConfig(Long id, BenefitConfigEntity benefitConfig) {
        BenefitConfigEntity existingConfig = getBenefitConfigById(id);
        // 更新福利配置字段
        existingConfig.setBenefitName(benefitConfig.getBenefitName());
        existingConfig.setBenefitType(benefitConfig.getBenefitType());
        existingConfig.setStandardAmount(benefitConfig.getStandardAmount());
        existingConfig.setFrequency(benefitConfig.getFrequency());
        existingConfig.setStatus(benefitConfig.getStatus());
        existingConfig.setDescription(benefitConfig.getDescription());
        existingConfig.setRemark(benefitConfig.getRemark());
        return benefitConfigRepository.save(existingConfig);
    }

    @Override
    public void deleteBenefitConfig(Long id) {
        BenefitConfigEntity existingConfig = getBenefitConfigById(id);
        benefitConfigRepository.delete(existingConfig);
    }

    @Override
    public List<BenefitConfigEntity> getAllBenefitConfigs() {
        return benefitConfigRepository.findAll();
    }

    @Override
    public Page<BenefitConfigEntity> getBenefitConfigsByPage(Pageable pageable) {
        return benefitConfigRepository.findAll(pageable);
    }
}
