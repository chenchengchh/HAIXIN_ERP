package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.RecruitmentDemandEntity;
import com.hxcoe.hr.repository.RecruitmentDemandRepository;
import com.hxcoe.hr.service.RecruitmentDemandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 招聘需求服务实现类
 */
@Service
public class RecruitmentDemandServiceImpl implements RecruitmentDemandService {

    @Autowired
    private RecruitmentDemandRepository recruitmentDemandRepository;

    @Override
    public RecruitmentDemandEntity createRecruitmentDemand(RecruitmentDemandEntity recruitmentDemand) {
        return recruitmentDemandRepository.save(recruitmentDemand);
    }

    @Override
    public RecruitmentDemandEntity updateRecruitmentDemand(Long id, RecruitmentDemandEntity recruitmentDemand) {
        Optional<RecruitmentDemandEntity> existingRecruitmentDemand = recruitmentDemandRepository.findById(id);
        if (existingRecruitmentDemand.isPresent()) {
            recruitmentDemand.setId(id);
            return recruitmentDemandRepository.save(recruitmentDemand);
        } else {
            throw new RuntimeException("招聘需求不存在");
        }
    }

    @Override
    public void deleteRecruitmentDemand(Long id) {
        recruitmentDemandRepository.deleteById(id);
    }

    @Override
    public Optional<RecruitmentDemandEntity> getRecruitmentDemandById(Long id) {
        return recruitmentDemandRepository.findById(id);
    }

    @Override
    public List<RecruitmentDemandEntity> getAllRecruitmentDemands() {
        return recruitmentDemandRepository.findAll();
    }

    @Override
    public Page<RecruitmentDemandEntity> getRecruitmentDemandsByPage(Pageable pageable) {
        return recruitmentDemandRepository.findAll(pageable);
    }

    @Override
    public List<RecruitmentDemandEntity> getRecruitmentDemandsByDepartment(Long departmentId) {
        // 这里需要根据实际情况实现，可能需要使用Specification或者QueryDSL
        // 暂时返回所有招聘需求
        return recruitmentDemandRepository.findAll();
    }

    @Override
    public List<RecruitmentDemandEntity> getRecruitmentDemandsByStatus(String status) {
        // 这里需要根据实际情况实现，可能需要使用Specification或者QueryDSL
        // 暂时返回所有招聘需求
        return recruitmentDemandRepository.findAll();
    }

    @Override
    public List<RecruitmentDemandEntity> searchRecruitmentDemands(String keyword) {
        // 这里需要根据实际情况实现，可能需要使用Specification或者QueryDSL
        // 暂时返回所有招聘需求
        return recruitmentDemandRepository.findAll();
    }

    @Override
    public void batchDeleteRecruitmentDemands(List<Long> ids) {
        recruitmentDemandRepository.deleteAllById(ids);
    }
}