package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.qms.client.MesClient;
import com.hxcoe.qms.client.SrmPerformanceClient;
import com.hxcoe.qms.client.dto.mes.InspectionResultDTO;
import com.hxcoe.qms.event.QualityInspectionCompletedEvent;
import com.hxcoe.qms.entity.QualityInspectionEntity;
import com.hxcoe.qms.repository.QualityInspectionRepository;
import com.hxcoe.qms.service.QualityInspectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class QualityInspectionServiceImpl implements QualityInspectionService {

    private static final Logger logger = LoggerFactory.getLogger(QualityInspectionServiceImpl.class);

    @Autowired
    private QualityInspectionRepository inspectionRepository;

    @Autowired
    private MesClient mesClient;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private com.hxcoe.qms.service.ScmIqcSyncService scmIqcSyncService;

    @Autowired(required = false)
    private SrmPerformanceClient srmPerformanceClient;

    @Override
    public Result<QualityInspectionEntity> createInspection(QualityInspectionEntity inspection) {
        // 生成检验单号
        if (inspection.getInspectionCode() == null) {
            inspection.setInspectionCode("INS-" + System.currentTimeMillis());
        }
        if (inspection.getStatus() == null) {
            inspection.setStatus("PENDING");
        }
        if (inspection.getInspectionDate() == null) {
            inspection.setInspectionDate(LocalDateTime.now());
        }
        inspection.setCreatedTime(LocalDateTime.now());
        QualityInspectionEntity savedInspection = inspectionRepository.save(inspection);
        return Result.success(savedInspection);
    }

    @Override
    public Result<QualityInspectionEntity> updateInspection(Long id, QualityInspectionEntity inspection) {
        QualityInspectionEntity existingInspection = inspectionRepository.findById(id).orElse(null);
        if (existingInspection == null) {
            return Result.error("质量检验记录不存在");
        }
        String beforeStatus = existingInspection.getStatus();
        existingInspection.setUpdatedTime(LocalDateTime.now());
        if (inspection.getInspectionResult() != null && !inspection.getInspectionResult().isBlank()) {
            existingInspection.setInspectionResult(inspection.getInspectionResult());
        }
        if (inspection.getInspector() != null) {
            existingInspection.setInspector(inspection.getInspector());
        }
        if (inspection.getRemark() != null) {
            existingInspection.setRemark(inspection.getRemark());
        }
        if (inspection.getInspectionStandard() != null) {
            existingInspection.setInspectionStandard(inspection.getInspectionStandard());
        }

        // 如果更新了结果，且状态变为COMPLETED，则回调
        String result = existingInspection.getInspectionResult();
        if (result != null &&
            ("PASS".equalsIgnoreCase(result) || "FAIL".equalsIgnoreCase(result)) &&
            !"COMPLETED".equalsIgnoreCase(existingInspection.getStatus())) {

            existingInspection.setStatus("COMPLETED");
            
            // 回调MES (仅当来源为生产时)
            if ("PRODUCTION".equalsIgnoreCase(existingInspection.getSourceType())) {
                try {
                    InspectionResultDTO resultDTO = new InspectionResultDTO();
                    resultDTO.setSourceNo(existingInspection.getSourceNo());
                    resultDTO.setResult(existingInspection.getInspectionResult());
                    // 简化：全部合格或全部不合格
                    if ("PASS".equalsIgnoreCase(existingInspection.getInspectionResult())) {
                        resultDTO.setQualifiedQuantity(existingInspection.getQuantity());
                        resultDTO.setUnqualifiedQuantity(BigDecimal.ZERO);
                    } else {
                        resultDTO.setQualifiedQuantity(BigDecimal.ZERO);
                        resultDTO.setUnqualifiedQuantity(existingInspection.getQuantity());
                    }
                    
                    mesClient.submitInspectionResult(resultDTO);
                    logger.info("质检结果已回调MES: {}", existingInspection.getInspectionCode());
                } catch (Exception e) {
                    logger.error("回调MES失败: {}", e.getMessage());
                }
            }
            if ("PURCHASE".equalsIgnoreCase(existingInspection.getSourceType())) {
                scmIqcSyncService.enqueueIqcCompletedIfAbsent(existingInspection);
                // IQC检验结果确定后，推送至SRM回写供应商质量绩效
                pushInspectionResultToSrm(existingInspection);
            }
        }

        QualityInspectionEntity updatedInspection = inspectionRepository.save(existingInspection);
        if ("PURCHASE".equalsIgnoreCase(updatedInspection.getSourceType())
                && "COMPLETED".equalsIgnoreCase(updatedInspection.getStatus())
                && !"COMPLETED".equalsIgnoreCase(beforeStatus)) {
            eventPublisher.publishEvent(new QualityInspectionCompletedEvent(updatedInspection.getId()));
        }
        return Result.success(updatedInspection);
    }

    @Override
    public Result<Void> deleteInspection(Long id) {
        if (!inspectionRepository.existsById(id)) {
            return Result.error("质量检验记录不存在");
        }
        inspectionRepository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<QualityInspectionEntity> getInspectionById(Long id) {
        QualityInspectionEntity inspection = inspectionRepository.findById(id).orElse(null);
        if (inspection == null) {
            return Result.error("质量检验记录不存在");
        }
        return Result.success(inspection);
    }

    @Override
    public Result<PageResult<QualityInspectionEntity>> getInspectionsByPage(Pageable pageable) {
        Page<QualityInspectionEntity> page = inspectionRepository.findAll(pageable);
        PageResult<QualityInspectionEntity> pageResult = PageResult.build(
            page.getTotalElements(),
            (int) page.getSize(),
            (int) (page.getNumber() + 1),
            page.getContent()
        );
        return Result.success(pageResult);
    }

    /**
     * 推送IQC检验结果至SRM供应商绩效（QMS→SRM闭环）。
     *
     * <p>QMS检验单无供应商字段，supplierId/supplierName 置空，附带 poNo（来源采购单号），
     * 由SRM端按采购订单兜底解析供应商。推送失败仅记录日志，不影响检验主流程。
     *
     * @param inspection 已完成判定的IQC检验单
     */
    private void pushInspectionResultToSrm(QualityInspectionEntity inspection) {
        if (srmPerformanceClient == null) {
            return;
        }
        try {
            boolean passed = "PASS".equalsIgnoreCase(inspection.getInspectionResult());
            BigDecimal quantity = inspection.getQuantity();
            Map<String, Object> body = new HashMap<>();
            body.put("eventId", UUID.randomUUID().toString());
            body.put("supplierId", null);
            body.put("supplierName", null);
            body.put("poNo", inspection.getSourceNo());
            body.put("materialCode", inspection.getProductCode());
            body.put("inspectionNo", inspection.getInspectionCode());
            body.put("result", passed ? "pass" : "fail");
            body.put("inspectionDate", inspection.getInspectionDate() == null
                    ? null : inspection.getInspectionDate().toLocalDate().toString());
            body.put("batchQuantity", quantity);
            // 简化口径：合格则不良数为0，不合格则整批判退
            body.put("rejectedQuantity", passed ? BigDecimal.ZERO : quantity);
            srmPerformanceClient.pushInspectionResult(body);
            logger.info("IQC检验结果已推送SRM供应商绩效: {}", inspection.getInspectionCode());
        } catch (Exception e) {
            logger.warn("推送SRM供应商绩效失败: inspectionCode={}, error={}",
                    inspection.getInspectionCode(), e.getMessage());
        }
    }
}
