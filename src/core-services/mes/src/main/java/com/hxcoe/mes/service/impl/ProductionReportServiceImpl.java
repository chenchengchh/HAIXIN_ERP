package com.hxcoe.mes.service.impl;

import com.hxcoe.common.dto.mes.MesReportingCreateDTO;
import com.hxcoe.common.dto.mes.MesReportingUpdateDTO;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.mes.client.HrWorkTimeClient;
import com.hxcoe.mes.entity.ProductionReportEntity;
import com.hxcoe.mes.repository.ProductionReportRepository;
import com.hxcoe.mes.service.ProductionReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import jakarta.persistence.criteria.Predicate;

/**
 * 生产报工服务实现类
 */
@Service
public class ProductionReportServiceImpl implements ProductionReportService {

    private static final Logger logger = LoggerFactory.getLogger(ProductionReportServiceImpl.class);

    @Autowired
    private ProductionReportRepository productionReportRepository;

    @Autowired(required = false)
    private HrWorkTimeClient hrWorkTimeClient;

    @Override
    public ProductionReportEntity createProductionReport(ProductionReportEntity productionReportEntity) {
        // 设置默认值
        productionReportEntity.setCreateTime(LocalDateTime.now());
        productionReportEntity.setUpdateTime(LocalDateTime.now());
        if (productionReportEntity.getStatus() == null) {
            productionReportEntity.setStatus("reported"); // 默认状态为已上报
        }
        ProductionReportEntity saved = productionReportRepository.save(productionReportEntity);
        // 报工保存成功后，推送工时数据至HR（MES→HR闭环）
        pushWorkReportToHr(saved);
        return saved;
    }

    @Override
    public ProductionReportEntity getProductionReportById(Long id) {
        Optional<ProductionReportEntity> optional = productionReportRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public ProductionReportEntity getProductionReportByNo(String reportNo) {
        if (reportNo == null || reportNo.isBlank()) {
            return null;
        }
        return productionReportRepository.findByReportNo(reportNo);
    }

    @Override
    public ProductionReportEntity updateProductionReport(ProductionReportEntity productionReportEntity) {
        productionReportEntity.setUpdateTime(LocalDateTime.now());
        return productionReportRepository.save(productionReportEntity);
    }

    @Override
    public boolean deleteProductionReport(Long id) {
        if (id == null) {
            return false;
        }
        if (!productionReportRepository.existsById(id)) {
            return false;
        }
        productionReportRepository.deleteById(id);
        return true;
    }

    @Override
    public PageResult<ProductionReportEntity> getProductionReportList(
            Integer page,
            Integer size,
            String reportNo,
            String workOrderNo,
            String operatorName,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Specification<ProductionReportEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (reportNo != null && !reportNo.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("reportNo"), "%" + reportNo + "%"));
            }
            if (workOrderNo != null && !workOrderNo.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("workOrderNo"), "%" + workOrderNo + "%"));
            }
            if (operatorName != null && !operatorName.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("operatorName"), "%" + operatorName + "%"));
            }
            if (status != null && !status.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("createTime"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createTime"), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createTime"), endDate));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        Page<ProductionReportEntity> reportPage = productionReportRepository.findAll(spec, pageable);
        return PageResult.build(reportPage.getTotalElements(), size, page, reportPage.getContent());
    }

    @Override
    public List<ProductionReportEntity> getProductionReportByStatus(String status) {
        if (status == null || status.isBlank()) {
            return new ArrayList<>();
        }
        return productionReportRepository.findByStatus(status);
    }

    @Override
    public ProductionReportEntity updateProductionReportStatus(Long id, String status) {
        ProductionReportEntity entity = getProductionReportById(id);
        if (entity != null) {
            entity.setStatus(status);
            entity.setUpdateTime(LocalDateTime.now());
            return productionReportRepository.save(entity);
        }
        return null;
    }

    @Override
    public boolean verifyProductionReport(Long id) {
        ProductionReportEntity entity = getProductionReportById(id);
        if (entity != null) {
            entity.setStatus("verified");
            entity.setUpdateTime(LocalDateTime.now());
            productionReportRepository.save(entity);
            return true;
        }
        return false;
    }

    @Override
    public boolean approveProductionReport(Long id) {
        ProductionReportEntity entity = getProductionReportById(id);
        if (entity != null) {
            entity.setStatus("approved");
            entity.setUpdateTime(LocalDateTime.now());
            productionReportRepository.save(entity);
            return true;
        }
        return false;
    }

    /**
     * 手动创建报工（B2 路径对齐）。
     *
     * <p>将强类型 {@link MesReportingCreateDTO} 映射为报工实体后落库。
     * reportNo 为空时自动生成，状态为空时默认 reported。
     *
     * @param dto 报工创建 DTO
     * @return 创建后的报工实体
     */
    @Override
    public ProductionReportEntity createManualReport(MesReportingCreateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("报工创建 DTO 不能为空");
        }
        if (dto.getWorkOrderNo() == null || dto.getWorkOrderNo().isBlank()) {
            throw new IllegalArgumentException("workOrderNo 不能为空");
        }
        ProductionReportEntity entity = new ProductionReportEntity();
        // reportNo 为空时自动生成（保留业务可读性）
        if (dto.getReportNo() == null || dto.getReportNo().isBlank()) {
            entity.setReportNo("RPT-" + System.currentTimeMillis());
        } else {
            entity.setReportNo(dto.getReportNo());
        }
        entity.setWorkOrderNo(dto.getWorkOrderNo());
        entity.setStepName(dto.getStepName());
        entity.setWorkstationName(dto.getWorkstationName());
        entity.setOperatorName(dto.getOperatorName());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setGoodQty(dto.getGoodQty());
        entity.setScrapQty(dto.getScrapQty());
        entity.setReworkQty(dto.getReworkQty());
        entity.setWorkingHours(dto.getWorkingHours());
        entity.setMachineHours(dto.getMachineHours());
        entity.setStatus(dto.getStatus() == null ? "reported" : dto.getStatus());
        entity.setRemark(dto.getRemark());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        ProductionReportEntity saved = productionReportRepository.save(entity);
        // 报工保存成功后，推送工时数据至HR（MES→HR闭环）
        pushWorkReportToHr(saved);
        return saved;
    }

    /**
     * 按 ID 增量更新报工（B2 路径对齐）。
     *
     * <p>仅覆盖 DTO 中非空字段，避免误清空已有数据。
     *
     * @param id  报工ID
     * @param dto 报工更新 DTO
     * @return 更新后的报工实体，不存在返回 null
     */
    @Override
    public ProductionReportEntity updateReportById(Long id, MesReportingUpdateDTO dto) {
        if (id == null || dto == null) {
            return null;
        }
        ProductionReportEntity entity = getProductionReportById(id);
        if (entity == null) {
            return null;
        }
        if (dto.getReportNo() != null && !dto.getReportNo().isBlank()) {
            entity.setReportNo(dto.getReportNo());
        }
        if (dto.getWorkOrderNo() != null && !dto.getWorkOrderNo().isBlank()) {
            entity.setWorkOrderNo(dto.getWorkOrderNo());
        }
        if (dto.getOperatorName() != null) {
            entity.setOperatorName(dto.getOperatorName());
        }
        if (dto.getEndTime() != null) {
            entity.setEndTime(dto.getEndTime());
        }
        if (dto.getGoodQty() != null) {
            entity.setGoodQty(dto.getGoodQty());
        }
        if (dto.getScrapQty() != null) {
            entity.setScrapQty(dto.getScrapQty());
        }
        if (dto.getReworkQty() != null) {
            entity.setReworkQty(dto.getReworkQty());
        }
        if (dto.getWorkingHours() != null) {
            entity.setWorkingHours(dto.getWorkingHours());
        }
        if (dto.getMachineHours() != null) {
            entity.setMachineHours(dto.getMachineHours());
        }
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            entity.setRemark(dto.getRemark());
        }
        entity.setUpdateTime(LocalDateTime.now());
        return productionReportRepository.save(entity);
    }

    /**
     * 推送报工工时数据至HR（MES→HR闭环）。
     *
     * <p>报工实体无员工编号字段，employeeNo 置空，employeeName 取操作员姓名；
     * 报工日期取结束时间（缺失时取创建时间）。推送失败仅记录日志，不影响报工主流程。
     *
     * @param report 已保存的生产报工实体
     */
    private void pushWorkReportToHr(ProductionReportEntity report) {
        if (hrWorkTimeClient == null || report == null) {
            return;
        }
        try {
            LocalDateTime reportTime = report.getEndTime() != null ? report.getEndTime() : report.getCreateTime();
            Map<String, Object> body = new HashMap<>();
            body.put("eventId", UUID.randomUUID().toString());
            body.put("employeeNo", null);
            body.put("employeeName", report.getOperatorName());
            body.put("workOrderNo", report.getWorkOrderNo());
            body.put("workHours", report.getWorkingHours());
            body.put("outputQuantity", report.getGoodQty());
            body.put("reportDate", reportTime == null ? null : reportTime.toLocalDate().toString());
            body.put("workstation", report.getWorkstationName());
            hrWorkTimeClient.pushWorkReport(body);
            logger.info("报工工时已推送HR: reportNo={}", report.getReportNo());
        } catch (Exception e) {
            logger.warn("推送HR报工工时失败: reportNo={}, error={}", report.getReportNo(), e.getMessage());
        }
    }
}
