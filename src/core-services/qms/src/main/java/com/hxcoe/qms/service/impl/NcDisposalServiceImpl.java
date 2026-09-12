package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.NcDisposalEntity;
import com.hxcoe.qms.repository.NcDisposalRepository;
import com.hxcoe.qms.service.NcDisposalService;
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
 * 不合格品处理服务实现
 */
@Service
public class NcDisposalServiceImpl implements NcDisposalService {

    @Autowired
    private NcDisposalRepository ncDisposalRepository;

    /**
     * 分页查询不合格品处理
     *
     * @param registrationNo 登记编号（模糊）
     * @param disposalStatus 处理状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<NcDisposalEntity>> page(String registrationNo, String disposalStatus, Pageable pageable) {
        Specification<NcDisposalEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (registrationNo != null && !registrationNo.isBlank()) {
                predicates.add(cb.like(root.get("registrationNo"), "%" + registrationNo.trim() + "%"));
            }
            if (disposalStatus != null && !disposalStatus.isBlank()) {
                predicates.add(cb.equal(root.get("disposalStatus"), disposalStatus.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<NcDisposalEntity> page = ncDisposalRepository.findAll(specification, pageable);
        PageResult<NcDisposalEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    /**
     * 获取处理详情
     *
     * @param id 处理ID
     * @return 处理详情
     */
    @Override
    public Result<NcDisposalEntity> getById(Long id) {
        NcDisposalEntity entity = ncDisposalRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("不合格品处理不存在");
        }
        return Result.success(entity);
    }

    /**
     * 创建处理
     *
     * @param entity 处理数据
     * @return 创建结果
     */
    @Override
    public Result<NcDisposalEntity> create(NcDisposalEntity entity) {
        if (entity.getDisposalStatus() == null || entity.getDisposalStatus().isBlank()) {
            entity.setDisposalStatus("pending");
        }
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        NcDisposalEntity saved = ncDisposalRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 更新处理
     *
     * @param id 处理ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @Override
    public Result<NcDisposalEntity> update(Long id, NcDisposalEntity entity) {
        NcDisposalEntity existing = ncDisposalRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("不合格品处理不存在");
        }
        entity.setId(id);
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedTime(LocalDateTime.now());
        NcDisposalEntity saved = ncDisposalRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 开始处理
     *
     * @param id 处理ID
     * @return 处理结果
     */
    @Override
    public Result<Void> start(Long id) {
        NcDisposalEntity entity = ncDisposalRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("不合格品处理不存在");
        }
        entity.setDisposalStatus("processing");
        entity.setStartTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        ncDisposalRepository.save(entity);
        return Result.success();
    }

    /**
     * 完成处理
     *
     * @param id 处理ID
     * @param processResult 处理结果
     * @return 处理结果
     */
    @Override
    public Result<Void> complete(Long id, String processResult) {
        NcDisposalEntity entity = ncDisposalRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("不合格品处理不存在");
        }
        entity.setDisposalStatus("completed");
        entity.setProcessResult(processResult);
        entity.setEndTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        ncDisposalRepository.save(entity);
        return Result.success();
    }

    /**
     * 批量开始处理
     *
     * @param ids 处理ID列表
     * @return 处理结果
     */
    @Override
    public Result<Void> batchStart(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.error("处理ID列表不能为空");
        }
        for (Long id : ids) {
            start(id);
        }
        return Result.success();
    }
}
