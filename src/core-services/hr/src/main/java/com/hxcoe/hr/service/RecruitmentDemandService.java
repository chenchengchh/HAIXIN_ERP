package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.RecruitmentDemandEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 招聘需求服务接口
 */
public interface RecruitmentDemandService {
    
    /**
     * 创建招聘需求
     * 
     * @param recruitmentDemand 招聘需求实体
     * @return 创建后的招聘需求
     */
    RecruitmentDemandEntity createRecruitmentDemand(RecruitmentDemandEntity recruitmentDemand);
    
    /**
     * 更新招聘需求
     * 
     * @param id 招聘需求ID
     * @param recruitmentDemand 招聘需求实体
     * @return 更新后的招聘需求
     */
    RecruitmentDemandEntity updateRecruitmentDemand(Long id, RecruitmentDemandEntity recruitmentDemand);
    
    /**
     * 删除招聘需求
     * 
     * @param id 招聘需求ID
     */
    void deleteRecruitmentDemand(Long id);
    
    /**
     * 根据ID查询招聘需求
     * 
     * @param id 招聘需求ID
     * @return 招聘需求
     */
    Optional<RecruitmentDemandEntity> getRecruitmentDemandById(Long id);
    
    /**
     * 查询所有招聘需求
     * 
     * @return 招聘需求列表
     */
    List<RecruitmentDemandEntity> getAllRecruitmentDemands();
    
    /**
     * 分页查询招聘需求
     * 
     * @param pageable 分页参数
     * @return 招聘需求分页列表
     */
    Page<RecruitmentDemandEntity> getRecruitmentDemandsByPage(Pageable pageable);
    
    /**
     * 根据部门ID查询招聘需求
     * 
     * @param departmentId 部门ID
     * @return 招聘需求列表
     */
    List<RecruitmentDemandEntity> getRecruitmentDemandsByDepartment(Long departmentId);
    
    /**
     * 根据状态查询招聘需求
     * 
     * @param status 状态
     * @return 招聘需求列表
     */
    List<RecruitmentDemandEntity> getRecruitmentDemandsByStatus(String status);
    
    /**
     * 搜索招聘需求
     * 
     * @param keyword 关键词
     * @return 招聘需求列表
     */
    List<RecruitmentDemandEntity> searchRecruitmentDemands(String keyword);
    
    /**
     * 批量删除招聘需求
     * 
     * @param ids 招聘需求ID列表
     */
    void batchDeleteRecruitmentDemands(List<Long> ids);
}