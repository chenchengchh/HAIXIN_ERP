package com.hxcoe.erp.service.impl;

import com.hxcoe.erp.entity.VoucherEntity;
import com.hxcoe.erp.entity.VoucherItemEntity;
import com.hxcoe.erp.repository.VoucherRepository;
import com.hxcoe.erp.repository.VoucherItemRepository;
import com.hxcoe.erp.service.ErpApprovalIntegrationService;
import com.hxcoe.erp.service.VoucherService;
import com.hxcoe.common.result.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 凭证服务实现类
 */
@Service
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private VoucherItemRepository voucherItemRepository;

    /**
     * 创建凭证
     *
     * @param voucherEntity 凭证实体
     * @return 创建结果
     */
    @Override
    @Transactional
    public VoucherEntity createVoucher(VoucherEntity voucherEntity) {
        // 生成凭证编号
        String voucherNo = generateVoucherNo(voucherEntity.getVoucherType());
        voucherEntity.setVoucherNo(voucherNo);

        // 设置默认值
        if (voucherEntity.getStatus() == null) {
            voucherEntity.setStatus("draft");
        }
        if (voucherEntity.getCreatedTime() == null) {
            voucherEntity.setCreatedTime(LocalDateTime.now());
        }
        voucherEntity.setUpdatedTime(LocalDateTime.now());
        if (voucherEntity.getIsDeleted() == null) {
            voucherEntity.setIsDeleted(0);
        }

        // 计算借贷合计
        calculateDebitCreditTotal(voucherEntity);

        // 保存凭证
        VoucherEntity savedVoucher = voucherRepository.save(voucherEntity);

        // 保存凭证分录
        if (voucherEntity.getItems() != null && !voucherEntity.getItems().isEmpty()) {
            for (VoucherItemEntity item : voucherEntity.getItems()) {
                item.setVoucher(savedVoucher);
                voucherItemRepository.save(item);
            }
        }

        return savedVoucher;
    }

    /**
     * 根据ID查询凭证
     *
     * @param id 主键ID
     * @return 查询结果
     */
    @Override
    public VoucherEntity getVoucherById(Long id) {
        Optional<VoucherEntity> optional = voucherRepository.findById(id);
        return optional.orElse(null);
    }

    /**
     * 根据凭证编号查询凭证
     *
     * @param voucherNo 凭证编号
     * @return 查询结果
     */
    @Override
    public VoucherEntity getVoucherByNo(String voucherNo) {
        Optional<VoucherEntity> optional = voucherRepository.findByVoucherNo(voucherNo);
        return optional.orElse(null);
    }

    /**
     * 更新凭证
     *
     * @param voucherEntity 凭证实体
     * @return 更新结果
     */
    @Override
    @Transactional
    public VoucherEntity updateVoucher(VoucherEntity voucherEntity) {
        // 检查凭证是否存在
        VoucherEntity existingVoucher = voucherRepository.findById(voucherEntity.getId()).orElse(null);
        if (existingVoucher == null) {
            return null;
        }

        // 更新基本信息
        existingVoucher.setVoucherDate(voucherEntity.getVoucherDate());
        existingVoucher.setVoucherType(voucherEntity.getVoucherType());
        existingVoucher.setSummary(voucherEntity.getSummary());
        // 仅当显式传入状态时才更新：前端编辑表单不再提交status，
        // 状态由OA审批流驱动（提交审批/审批回调/过账），避免绕过审批直接改状态或置空
        if (voucherEntity.getStatus() != null && !voucherEntity.getStatus().isBlank()) {
            existingVoucher.setStatus(voucherEntity.getStatus());
        }
        existingVoucher.setUpdatedTime(LocalDateTime.now());

        // 计算借贷合计
        calculateDebitCreditTotal(voucherEntity);
        existingVoucher.setDebitTotal(voucherEntity.getDebitTotal());
        existingVoucher.setCreditTotal(voucherEntity.getCreditTotal());

        // 删除旧的凭证分录
        voucherItemRepository.deleteByVoucherId(voucherEntity.getId());

        // 保存新的凭证分录
        if (voucherEntity.getItems() != null && !voucherEntity.getItems().isEmpty()) {
            for (VoucherItemEntity item : voucherEntity.getItems()) {
                item.setVoucher(existingVoucher);
                voucherItemRepository.save(item);
            }
        }

        // 更新凭证
        return voucherRepository.save(existingVoucher);
    }

    /**
     * 删除凭证
     *
     * @param id 主键ID
     * @return 删除结果
     */
    @Override
    @Transactional
    public boolean deleteVoucher(Long id) {
        // 检查凭证是否存在
        Optional<VoucherEntity> optional = voucherRepository.findById(id);
        if (optional.isPresent()) {
            // 删除凭证分录
            voucherItemRepository.deleteByVoucherId(id);
            // 删除凭证
            voucherRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 分页查询凭证列表
     *
     * @param page 当前页码
     * @param size 每页条数
     * @param voucherType 凭证类型
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @Override
    public PageResult<VoucherEntity> getVoucherList(Integer page, Integer size, String voucherType, String status, LocalDateTime startDate, LocalDateTime endDate) {
        // 创建分页请求
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "voucherDate", "id"));

        // 构建查询条件
        Specification<VoucherEntity> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 逻辑删除条件
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));

            // 凭证类型条件
            if (voucherType != null && !voucherType.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("voucherType"), voucherType));
            }

            // 状态条件
            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            // 日期范围条件
            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("voucherDate"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("voucherDate"), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("voucherDate"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<VoucherEntity> pageResult = voucherRepository.findAll(specification, pageable);

        // 转换为自定义分页结果
        return PageResult.build(
                pageResult.getTotalElements(),
                pageResult.getSize(),
                pageResult.getNumber() + 1,
                pageResult.getContent()
        );
    }

    /**
     * 审核凭证
     *
     * @param id 主键ID
     * @return 审核结果
     */
    @Override
    @Transactional
    public VoucherEntity approveVoucher(Long id) {
        VoucherEntity voucherEntity = getVoucherById(id);
        if (voucherEntity != null) {
            voucherEntity.setStatus("approved");
            voucherEntity.setUpdatedTime(LocalDateTime.now());
            return voucherRepository.save(voucherEntity);
        }
        return null;
    }

    /**
     * 过账凭证
     *
     * @param id 主键ID
     * @return 过账结果
     */
    @Override
    @Transactional
    public VoucherEntity postVoucher(Long id) {
        VoucherEntity voucherEntity = getVoucherById(id);
        if (voucherEntity != null) {
            voucherEntity.setStatus("posted");
            voucherEntity.setUpdatedTime(LocalDateTime.now());
            return voucherRepository.save(voucherEntity);
        }
        return null;
    }

    /**
     * 拒绝凭证
     *
     * @param id 主键ID
     * @return 拒绝结果
     */
    @Override
    @Transactional
    public VoucherEntity rejectVoucher(Long id) {
        VoucherEntity voucherEntity = getVoucherById(id);
        if (voucherEntity != null) {
            voucherEntity.setStatus("rejected");
            voucherEntity.setUpdatedTime(LocalDateTime.now());
            return voucherRepository.save(voucherEntity);
        }
        return null;
    }

    /**
     * 提交凭证
     *
     * @param id 主键ID
     * @return 提交结果
     */
    @Override
    @Transactional
    public VoucherEntity submitVoucher(Long id) {
        VoucherEntity voucherEntity = getVoucherById(id);
        if (voucherEntity != null) {
            voucherEntity.setStatus("submitted");
            voucherEntity.setUpdatedTime(LocalDateTime.now());
            return voucherRepository.save(voucherEntity);
        }
        return null;
    }

    @Autowired
    private ErpApprovalIntegrationService approvalIntegrationService;

    /**
     * 提交凭证并发起OA统一审批。
     * <p>凭证状态置为submitted（审批中），同时向OA提交审批申请；
     * OA审批完成后回调 /api/v1/erp/approval/callback 更新凭证状态。</p>
     *
     * @param id            凭证ID
     * @param initiatorId   发起人ID
     * @param initiatorName 发起人名称
     * @return 提交结果
     */
    @Override
    @Transactional
    public VoucherEntity submitVoucherForApproval(Long id, Long initiatorId, String initiatorName) {
        VoucherEntity voucherEntity = getVoucherById(id);
        if (voucherEntity == null) {
            return null;
        }
        // 仅草稿/已拒绝状态允许提交审批，防止重复提交
        if (!"draft".equals(voucherEntity.getStatus()) && !"rejected".equals(voucherEntity.getStatus())) {
            return voucherEntity;
        }
        voucherEntity.setStatus("submitted");
        voucherEntity.setUpdatedTime(LocalDateTime.now());
        VoucherEntity saved = voucherRepository.save(voucherEntity);

        // 向OA统一审批中心提交审批申请（失败不阻断本地提交，记录日志）
        approvalIntegrationService.submitVoucherApproval(
                saved.getId(), saved.getVoucherNo(), saved.getVoucherType(),
                saved.getDebitTotal(), saved.getCreditTotal(), saved.getSummary(),
                initiatorId, initiatorName);
        return saved;
    }

    /**
     * 生成凭证编号
     *
     * @param voucherType 凭证类型
     * @return 凭证编号
     */
    @Override
    public String generateVoucherNo(String voucherType) {
        // 凭证类型前缀
        String prefix;
        switch (voucherType) {
            case "receipt":
                prefix = "SK";
                break;
            case "payment":
                prefix = "FK";
                break;
            case "transfer":
                prefix = "ZZ";
                break;
            case "journal":
            default:
                prefix = "JZ";
                break;
        }

        // 日期格式：YYYYMMDD
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        // 生成随机数（4位）
        int random = (int) (Math.random() * 9000) + 1000;

        // 组合生成凭证编号
        return String.format("%s%s%s", prefix, dateStr, random);
    }

    /**
     * 计算借贷合计
     *
     * @param voucherEntity 凭证实体
     */
    private void calculateDebitCreditTotal(VoucherEntity voucherEntity) {
        BigDecimal debitTotal = BigDecimal.ZERO;
        BigDecimal creditTotal = BigDecimal.ZERO;

        if (voucherEntity.getItems() != null && !voucherEntity.getItems().isEmpty()) {
            for (VoucherItemEntity item : voucherEntity.getItems()) {
                debitTotal = debitTotal.add(item.getDebitAmount() != null ? item.getDebitAmount() : BigDecimal.ZERO);
                creditTotal = creditTotal.add(item.getCreditAmount() != null ? item.getCreditAmount() : BigDecimal.ZERO);
            }
        }

        voucherEntity.setDebitTotal(debitTotal);
        voucherEntity.setCreditTotal(creditTotal);
    }
}
