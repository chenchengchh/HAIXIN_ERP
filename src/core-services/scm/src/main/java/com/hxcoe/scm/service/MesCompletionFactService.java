package com.hxcoe.scm.service;

import com.hxcoe.common.dto.scm.MesCompletionFactDTO;
import com.hxcoe.scm.entity.ScmProductionCompletionFactEntity;
import com.hxcoe.scm.repository.ScmProductionCompletionFactRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SCM 接收 MES 完工事实服务（B6 闭环消费端）。
 *
 * <p>MES 工单完工后通过 Outbox 推送完工事件，SCM 收到后落 scm_production_completion_fact 表，
 * 形成制造回流到供应链侧，闭环生产链。
 *
 * <p>幂等策略：按 idempotencyKey 去重，同一完工事件重复推送不会重复落库。
 */
@Slf4j
@Service
public class MesCompletionFactService {

    @Autowired
    private ScmProductionCompletionFactRepository completionFactRepository;

    /**
     * 应用 MES 完工事实（幂等落库）。
     *
     * @param dto MES 完工事实 DTO
     * @return 已存在或新创建的完工事实实体（dto 非法时返回 null）
     */
    @Transactional
    public ScmProductionCompletionFactEntity applyMesCompletion(MesCompletionFactDTO dto) {
        if (dto == null || dto.getErpProductionNo() == null || dto.getErpProductionNo().isBlank()) {
            log.warn("SCM 接收 MES 完工事实：erpProductionNo 为空，丢弃 eventId={}", dto == null ? null : dto.getEventId());
            return null;
        }
        if (dto.getWorkOrderNo() == null || dto.getWorkOrderNo().isBlank()) {
            log.warn("SCM 接收 MES 完工事实：workOrderNo 为空，丢弃 erpProductionNo={}", dto.getErpProductionNo());
            return null;
        }

        String idempotencyKey = dto.getIdempotencyKey();
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            idempotencyKey = "MES:COMPLETION:" + dto.getErpProductionNo() + ":" + dto.getWorkOrderNo();
        }

        // 幂等：同一完工事件只落一次
        Optional<ScmProductionCompletionFactEntity> existing = completionFactRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("SCM 接收 MES 完工事实命中幂等，返回已有记录 idempotencyKey={} erpProductionNo={}",
                    idempotencyKey, dto.getErpProductionNo());
            return existing.get();
        }

        ScmProductionCompletionFactEntity entity = new ScmProductionCompletionFactEntity();
        entity.setEventId(dto.getEventId());
        entity.setEventKey(dto.getEventKey());
        entity.setIdempotencyKey(idempotencyKey);
        entity.setErpProductionNo(dto.getErpProductionNo());
        entity.setWorkOrderNo(dto.getWorkOrderNo());
        entity.setProductCode(dto.getProductCode());
        entity.setProductName(dto.getProductName());
        entity.setPlanQuantity(dto.getPlanQuantity());
        entity.setCompletedQuantity(dto.getCompletedQuantity());
        entity.setCompletedTime(dto.getCompletedTime());
        entity.setScmOrderStatus("COMPLETED");
        entity.setSourceSystem(dto.getSourceSystem() == null ? "mes-service" : dto.getSourceSystem());

        ScmProductionCompletionFactEntity saved = completionFactRepository.save(entity);
        log.info("SCM 落 MES 完工事实成功 erpProductionNo={} workOrderNo={} completedQty={} eventId={}",
                dto.getErpProductionNo(), dto.getWorkOrderNo(), dto.getCompletedQuantity(), dto.getEventId());
        return saved;
    }

    /**
     * 多条件分页查询完工事实（供前端 F2 页面调用）。
     *
     * <p>所有筛选条件均为可选，为空/空白时不参与查询；非空时按 LIKE 模糊匹配（业务键）或精确匹配（状态）。
     * 结果按完工时间倒序排列，便于业务方查看最新完工事实。
     *
     * @param erpProductionNo ERP 生产单号（模糊匹配，可空）
     * @param workOrderNo     MES 工单号（模糊匹配，可空）
     * @param scmOrderStatus  SCM 侧订单状态（精确匹配，可空）
     * @param completedTimeFrom 完工时间下限（含，可空）
     * @param completedTimeTo   完工时间上限（含，可空）
     * @param pageable        分页参数
     * @return 完工事实分页结果
     */
    @Transactional(readOnly = true)
    public Page<ScmProductionCompletionFactEntity> findCompletionFacts(
            String erpProductionNo,
            String workOrderNo,
            String scmOrderStatus,
            LocalDateTime completedTimeFrom,
            LocalDateTime completedTimeTo,
            Pageable pageable) {

        // 动态拼接查询条件，所有条件为空时跳过
        Specification<ScmProductionCompletionFactEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (erpProductionNo != null && !erpProductionNo.isBlank()) {
                predicates.add(cb.like(root.get("erpProductionNo"), "%" + erpProductionNo.trim() + "%"));
            }
            if (workOrderNo != null && !workOrderNo.isBlank()) {
                predicates.add(cb.like(root.get("workOrderNo"), "%" + workOrderNo.trim() + "%"));
            }
            if (scmOrderStatus != null && !scmOrderStatus.isBlank()) {
                predicates.add(cb.equal(root.get("scmOrderStatus"), scmOrderStatus.trim()));
            }
            if (completedTimeFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("completedTime"), completedTimeFrom));
            }
            if (completedTimeTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("completedTime"), completedTimeTo));
            }
            // 排序由 Controller 层 Pageable 统一控制（completedTime DESC），此处不再重复设置
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return completionFactRepository.findAll(spec, pageable);
    }
}
