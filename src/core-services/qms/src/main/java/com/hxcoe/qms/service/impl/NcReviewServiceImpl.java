package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.NcReviewEntity;
import com.hxcoe.qms.repository.NcReviewRepository;
import com.hxcoe.qms.service.NcReviewService;
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
 * 不合格品评审服务实现
 */
@Service
public class NcReviewServiceImpl implements NcReviewService {

    @Autowired
    private NcReviewRepository ncReviewRepository;

    /**
     * 分页查询不合格品评审
     *
     * @param registrationNo 登记编号（模糊）
     * @param reviewStatus 评审状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<NcReviewEntity>> page(String registrationNo, String reviewStatus, Pageable pageable) {
        Specification<NcReviewEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (registrationNo != null && !registrationNo.isBlank()) {
                predicates.add(cb.like(root.get("registrationNo"), "%" + registrationNo.trim() + "%"));
            }
            if (reviewStatus != null && !reviewStatus.isBlank()) {
                predicates.add(cb.equal(root.get("reviewStatus"), reviewStatus.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<NcReviewEntity> page = ncReviewRepository.findAll(specification, pageable);
        PageResult<NcReviewEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    /**
     * 获取评审详情
     *
     * @param id 评审ID
     * @return 评审详情
     */
    @Override
    public Result<NcReviewEntity> getById(Long id) {
        NcReviewEntity entity = ncReviewRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("不合格品评审不存在");
        }
        return Result.success(entity);
    }

    /**
     * 创建评审
     *
     * @param entity 评审数据
     * @return 创建结果
     */
    @Override
    public Result<NcReviewEntity> create(NcReviewEntity entity) {
        if (entity.getReviewStatus() == null || entity.getReviewStatus().isBlank()) {
            entity.setReviewStatus("pending");
        }
        if (entity.getReviewTime() == null) {
            entity.setReviewTime(LocalDateTime.now());
        }
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        NcReviewEntity saved = ncReviewRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 更新评审
     *
     * @param id 评审ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @Override
    public Result<NcReviewEntity> update(Long id, NcReviewEntity entity) {
        NcReviewEntity existing = ncReviewRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("不合格品评审不存在");
        }
        entity.setId(id);
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedTime(LocalDateTime.now());
        NcReviewEntity saved = ncReviewRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 审批评审
     *
     * @param id 评审ID
     * @param reviewStatus 审批状态
     * @param reviewOpinion 审批意见
     * @return 处理结果
     */
    @Override
    public Result<Void> approve(Long id, String reviewStatus, String reviewOpinion) {
        NcReviewEntity entity = ncReviewRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("不合格品评审不存在");
        }
        if (reviewStatus == null || reviewStatus.isBlank()) {
            return Result.error("评审状态不能为空");
        }
        entity.setReviewStatus(reviewStatus);
        if (reviewOpinion != null) {
            entity.setReviewOpinion(reviewOpinion);
        }
        entity.setUpdatedTime(LocalDateTime.now());
        ncReviewRepository.save(entity);
        return Result.success();
    }

    /**
     * 批量审批评审
     *
     * @param ids 评审ID列表
     * @param reviewStatus 审批状态
     * @param reviewOpinion 审批意见
     * @return 处理结果
     */
    @Override
    public Result<Void> batchApprove(List<Long> ids, String reviewStatus, String reviewOpinion) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("审批ID列表不能为空");
        }
        for (Long id : ids) {
            approve(id, reviewStatus, reviewOpinion);
        }
        return Result.success();
    }
}
