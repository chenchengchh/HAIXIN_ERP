package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.NcRegistrationEntity;
import com.hxcoe.qms.repository.NcRegistrationRepository;
import com.hxcoe.qms.service.NcRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 不合格品登记服务实现
 */
@Service
public class NcRegistrationServiceImpl implements NcRegistrationService {

    @Autowired
    private NcRegistrationRepository ncRegistrationRepository;

    /**
     * 分页查询不合格品登记
     *
     * @param registrationNo 登记编号（模糊）
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<NcRegistrationEntity>> page(String registrationNo, String status, Pageable pageable) {
        Specification<NcRegistrationEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (registrationNo != null && !registrationNo.isBlank()) {
                predicates.add(cb.like(root.get("registrationNo"), "%" + registrationNo.trim() + "%"));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<NcRegistrationEntity> page = ncRegistrationRepository.findAll(specification, pageable);
        PageResult<NcRegistrationEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    /**
     * 获取登记详情
     *
     * @param id 登记ID
     * @return 登记详情
     */
    @Override
    public Result<NcRegistrationEntity> getById(Long id) {
        NcRegistrationEntity entity = ncRegistrationRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("不合格品登记不存在");
        }
        return Result.success(entity);
    }

    /**
     * 创建登记
     *
     * @param entity 登记数据
     * @return 创建结果
     */
    @Override
    public Result<NcRegistrationEntity> create(NcRegistrationEntity entity) {
        if (entity.getRegistrationNo() == null || entity.getRegistrationNo().isBlank()) {
            entity.setRegistrationNo("NCR-" + System.currentTimeMillis());
        } else if (ncRegistrationRepository.findByRegistrationNo(entity.getRegistrationNo().trim()).isPresent()) {
            return Result.error("登记编号已存在");
        }
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("registered");
        }
        if (entity.getRegistrationTime() == null) {
            entity.setRegistrationTime(LocalDateTime.now());
        }
        if (entity.getDiscoveryTime() == null) {
            entity.setDiscoveryTime(LocalDateTime.now());
        }
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        NcRegistrationEntity saved = ncRegistrationRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 更新登记
     *
     * @param id 登记ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @Override
    public Result<NcRegistrationEntity> update(Long id, NcRegistrationEntity entity) {
        NcRegistrationEntity existing = ncRegistrationRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("不合格品登记不存在");
        }
        entity.setId(id);
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedTime(LocalDateTime.now());
        NcRegistrationEntity saved = ncRegistrationRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 删除登记
     *
     * @param id 登记ID
     * @return 删除结果
     */
    @Override
    public Result<Void> delete(Long id) {
        if (!ncRegistrationRepository.existsById(id)) {
            return Result.error("不合格品登记不存在");
        }
        ncRegistrationRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 提交评审
     *
     * @param id 登记ID
     * @return 提交结果
     */
    @Override
    public Result<Void> submitReview(Long id) {
        NcRegistrationEntity entity = ncRegistrationRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("不合格品登记不存在");
        }
        entity.setStatus("reviewed");
        entity.setReviewStatus("pending");
        entity.setUpdatedTime(LocalDateTime.now());
        ncRegistrationRepository.save(entity);
        return Result.success();
    }
}
