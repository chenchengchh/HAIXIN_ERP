package com.hxcoe.erp.service.impl;

import com.hxcoe.erp.entity.ProductionEntity;
import com.hxcoe.erp.repository.ProductionRepository;
import com.hxcoe.erp.service.MesWorkOrderOutboxService;
import com.hxcoe.erp.service.ProductionService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.erp.client.ApsResourceLoadClient;
import com.hxcoe.erp.dto.production.CapacityDataDto;
import com.hxcoe.erp.dto.production.CapacityPlanningResultDto;
import com.hxcoe.erp.support.ResultDataExtractor;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 生产服务实现类
 */
@Service
public class ProductionServiceImpl implements ProductionService {

    @Autowired
    private ProductionRepository productionRepository;

    @Autowired
    private ApsResourceLoadClient apsResourceLoadClient;

    @Autowired
    private MesWorkOrderOutboxService mesWorkOrderOutboxService;

    private volatile List<CapacityPlanningResultDto> lastCapacityPlanningResults = List.of();

    @Override
    public ProductionEntity createProduction(ProductionEntity productionEntity) {
        // 设置默认值
        if (productionEntity.getProductionNo() == null || productionEntity.getProductionNo().isBlank()) {
            productionEntity.setProductionNo("MO-" + System.currentTimeMillis());
        }
        if (productionEntity.getIsDeleted() == null) {
            productionEntity.setIsDeleted(0);
        }
        if (productionEntity.getCreatedTime() == null) {
            productionEntity.setCreatedTime(LocalDateTime.now());
        }
        if (productionEntity.getUpdatedTime() == null) {
            productionEntity.setUpdatedTime(LocalDateTime.now());
        }
        if (productionEntity.getCreatedBy() == null || productionEntity.getCreatedBy().isBlank()) {
            productionEntity.setCreatedBy("system");
        }
        if (productionEntity.getUpdatedBy() == null || productionEntity.getUpdatedBy().isBlank()) {
            productionEntity.setUpdatedBy(productionEntity.getCreatedBy());
        }
        if (productionEntity.getCompletedQuantity() == null) {
            productionEntity.setCompletedQuantity(0);
        }
        if (productionEntity.getProductionStatus() == null) {
            productionEntity.setProductionStatus(0);
        }
        ProductionEntity saved = productionRepository.save(productionEntity);

        // B1 修复：生产单创建后通过 Outbox 异步触发 MES 工单创建（同事务，保证原子性）
        mesWorkOrderOutboxService.enqueueProductionCreated(saved);

        return saved;
    }

    @Override
    public ProductionEntity getProductionById(Long id) {
        return productionRepository.findById(id).orElse(null);
    }

    @Override
    public ProductionEntity getProductionByNo(String productionNo) {
        return productionRepository.findByProductionNo(productionNo);
    }

    @Override
    public ProductionEntity updateProduction(ProductionEntity productionEntity) {
        // 更新时间
        productionEntity.setUpdatedTime(LocalDateTime.now());
        return productionRepository.save(productionEntity);
    }

    @Override
    public boolean deleteProduction(Long id) {
        ProductionEntity productionEntity = productionRepository.findById(id).orElse(null);
        if (productionEntity != null) {
            // 逻辑删除
            productionEntity.setIsDeleted(1);
            productionEntity.setUpdatedTime(LocalDateTime.now());
            productionRepository.save(productionEntity);
            return true;
        }
        return false;
    }

    @Override
    public PageResult<ProductionEntity> getProductionList(Integer page, Integer size, String productionNo, String productCode, String productName, String workshop, String productionLine, Integer productionStatus, LocalDateTime startDate, LocalDateTime endDate) {
        // 构建分页请求
        Pageable pageable = PageRequest.of(page - 1, size);

        // 构建查询条件
        Specification<ProductionEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 排除逻辑删除
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), 0));

            // 生产单号条件
            if (productionNo != null && !productionNo.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("productionNo"), "%" + productionNo + "%"));
            }

            // 产品编码条件
            if (productCode != null && !productCode.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("productCode"), productCode));
            }

            if (productName != null && !productName.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("productName"), "%" + productName + "%"));
            }

            // 车间条件
            if (workshop != null && !workshop.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("workshop"), workshop));
            }

            if (productionLine != null && !productionLine.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("productionLine"), productionLine));
            }

            // 生产状态条件
            if (productionStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("productionStatus"), productionStatus));
            }

            // 日期范围条件
            if (startDate != null && endDate != null) {
                predicates.add(criteriaBuilder.between(root.get("planStartTime"), startDate, endDate));
            } else if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("planStartTime"), startDate));
            } else if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("planStartTime"), endDate));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<ProductionEntity> productionPage = productionRepository.findAll(spec, pageable);

        // 构建分页结果
        return PageResult.build(
                productionPage.getTotalElements(),
                size,
                page,
                productionPage.getContent()
        );
    }

    @Override
    public List<ProductionEntity> getProductionByStatus(Integer productionStatus) {
        return productionRepository.findByProductionStatus(productionStatus);
    }

    @Override
    public List<ProductionEntity> getProductionByProductCode(String productCode) {
        return productionRepository.findByProductCode(productCode);
    }

    @Override
    public boolean startProduction(Long id) {
        ProductionEntity productionEntity = productionRepository.findById(id).orElse(null);
        if (productionEntity != null && productionEntity.getProductionStatus() != null) {
            // 只有待生产或已暂停状态可以开始生产
            if (productionEntity.getProductionStatus() == 0 || productionEntity.getProductionStatus() == 1 || productionEntity.getProductionStatus() == 4) {
                productionEntity.setProductionStatus(2); // 生产中
                productionEntity.setActualStartTime(LocalDateTime.now());
                productionEntity.setUpdatedTime(LocalDateTime.now());
                productionRepository.save(productionEntity);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean pauseProduction(Long id) {
        ProductionEntity productionEntity = productionRepository.findById(id).orElse(null);
        if (productionEntity != null && productionEntity.getProductionStatus() != null) {
            // 只有生产中状态可以暂停
            if (productionEntity.getProductionStatus() == 2) {
                productionEntity.setProductionStatus(4); // 已暂停
                productionEntity.setUpdatedTime(LocalDateTime.now());
                productionRepository.save(productionEntity);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean resumeProduction(Long id) {
        ProductionEntity productionEntity = productionRepository.findById(id).orElse(null);
        if (productionEntity != null && productionEntity.getProductionStatus() != null) {
            // 只有已暂停状态可以恢复生产
            if (productionEntity.getProductionStatus() == 4) {
                productionEntity.setProductionStatus(2); // 生产中
                productionEntity.setUpdatedTime(LocalDateTime.now());
                productionRepository.save(productionEntity);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean completeProduction(Long id) {
        ProductionEntity productionEntity = productionRepository.findById(id).orElse(null);
        if (productionEntity != null && productionEntity.getProductionStatus() != null) {
            // 只有待生产、生产中或已暂停状态可以完成生产
            if (productionEntity.getProductionStatus() == 0 || productionEntity.getProductionStatus() == 1 || productionEntity.getProductionStatus() == 2 || productionEntity.getProductionStatus() == 4) {
                productionEntity.setProductionStatus(3); // 已完成
                // 如果还没有开始生产，设置实际开始时间
                if (productionEntity.getActualStartTime() == null) {
                    productionEntity.setActualStartTime(LocalDateTime.now());
                }
                productionEntity.setActualEndTime(LocalDateTime.now());
                // 设置已完成数量等于生产数量
                productionEntity.setCompletedQuantity(productionEntity.getProductionQuantity());
                productionEntity.setUpdatedTime(LocalDateTime.now());
                productionRepository.save(productionEntity);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean cancelProduction(Long id) {
        ProductionEntity productionEntity = productionRepository.findById(id).orElse(null);
        if (productionEntity != null && productionEntity.getProductionStatus() != null) {
            // 只有待生产、生产中或已暂停状态可以取消生产
            if (productionEntity.getProductionStatus() == 0 || productionEntity.getProductionStatus() == 1 || productionEntity.getProductionStatus() == 2 || productionEntity.getProductionStatus() == 4) {
                productionEntity.setProductionStatus(5); // 已取消
                productionEntity.setUpdatedTime(LocalDateTime.now());
                productionRepository.save(productionEntity);
                return true;
            }
        }
        return false;
    }

    @Override
    public PageResult<CapacityDataDto> getCapacity(Integer page, Integer size, String departmentName, String workCenterName, String resourceName, String period, String startDate, String endDate) {
        List<CapacityDataDto> all = queryCapacityRows(resourceName, period, startDate, endDate);

        int p = Math.max(page == null ? 1 : page, 1);
        int s = Math.max(size == null ? 10 : size, 1);
        int from = Math.min((p - 1) * s, all.size());
        int to = Math.min(from + s, all.size());
        List<CapacityDataDto> slice = all.subList(from, to);
        return PageResult.build((long) all.size(), s, p, slice);
    }

    @Override
    public PageResult<CapacityPlanningResultDto> runCapacityPlanning(Map<String, Object> params) {
        List<CapacityPlanningResultDto> results = buildCapacityPlanningResults(params);
        lastCapacityPlanningResults = results;
        int s = Math.max(results.size(), 1);
        return PageResult.build((long) results.size(), s, 1, results);
    }

    @Override
    public PageResult<CapacityPlanningResultDto> getCapacityPlanningResults(Integer page, Integer size) {
        List<CapacityPlanningResultDto> all = lastCapacityPlanningResults != null ? lastCapacityPlanningResults : List.of();
        int p = Math.max(page == null ? 1 : page, 1);
        int s = Math.max(size == null ? 10 : size, 1);
        int from = Math.min((p - 1) * s, all.size());
        int to = Math.min(from + s, all.size());
        List<CapacityPlanningResultDto> slice = all.subList(from, to);
        return PageResult.build((long) all.size(), s, p, slice);
    }

    @Override
    public void updateStatus(Long id, String status, Double actualQuantity) {
        ProductionEntity entity = productionRepository.findById(id).orElse(null);
        if (entity == null) return;

        // 映射外部状态
        if ("STARTED".equals(status)) {
            entity.setProductionStatus(2);
            if (entity.getActualStartTime() == null) entity.setActualStartTime(LocalDateTime.now());
        } else if ("COMPLETED".equals(status)) {
            entity.setProductionStatus(3);
            if (entity.getActualEndTime() == null) entity.setActualEndTime(LocalDateTime.now());
            if (actualQuantity != null) entity.setCompletedQuantity(actualQuantity.intValue());
        }

        productionRepository.save(entity);
    }

    /**
     * 按生产单号更新生产状态（B4 修复）。
     *
     * <p>查找逻辑：按 productionNo 查询生产单，找不到则返回 false（MES 侧记日志但不阻断）。
     * 状态映射复用 {@link #updateStatus} 的语义。
     *
     * @param productionNo    生产单号
     * @param status          外部状态（STARTED/COMPLETED）
     * @param actualQuantity  实际完工数量
     * @return 是否找到并更新成功
     */
    @Override
    public boolean updateStatusByNo(String productionNo, String status, Double actualQuantity) {
        if (productionNo == null || productionNo.isBlank()) {
            return false;
        }
        ProductionEntity entity = productionRepository.findByProductionNo(productionNo);
        if (entity == null) {
            return false;
        }
        if ("STARTED".equals(status)) {
            entity.setProductionStatus(2);
            if (entity.getActualStartTime() == null) entity.setActualStartTime(LocalDateTime.now());
        } else if ("COMPLETED".equals(status)) {
            entity.setProductionStatus(3);
            if (entity.getActualEndTime() == null) entity.setActualEndTime(LocalDateTime.now());
            if (actualQuantity != null) entity.setCompletedQuantity(actualQuantity.intValue());
        } else {
            return false;
        }
        entity.setUpdatedTime(LocalDateTime.now());
        productionRepository.save(entity);
        return true;
    }

    /**
     * 查询 APS 资源负载并映射为产能数据。
     *
     * @param resourceName 资源名称（模糊匹配）
     * @param period 统计周期（day/week/month），将透传给 APS timeScale
     * @param startDate 开始日期（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）
     * @param endDate 结束日期（yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss）
     * @return 产能数据列表（未分页）
     */
    private List<CapacityDataDto> queryCapacityRows(String resourceName, String period, String startDate, String endDate) {
        Result<Object> res = apsResourceLoadClient.list(null, null, startDate, endDate, period);
        List<Map<String, Object>> rows = ResultDataExtractor.asListOfMap(res != null ? res.getData() : null);
        if (rows.isEmpty()) return List.of();

        LocalDateTime start = parseDateTime(startDate, true);
        LocalDateTime end = parseDateTime(endDate, false);
        List<CapacityDataDto> out = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            if (!matchesDateRange(row, start, end)) continue;
            String rn = str(row, "resourceName", "resource_name");
            if (resourceName != null && !resourceName.isBlank()) {
                if (rn == null || !rn.toLowerCase().contains(resourceName.toLowerCase())) continue;
            }

            CapacityDataDto dto = new CapacityDataDto();
            String dep = str(row, "departmentName", "department_name");
            String wc = str(row, "workCenterName", "work_center_name");
            if (dep.isBlank()) dep = "生产部";
            if (wc.isBlank()) wc = rn;

            dto.setDepartmentName(dep);
            dto.setWorkCenterName(wc);
            dto.setResourceName(rn);

            BigDecimal used = toDecimal(value(row, "usedCapacity", "used_capacity"));
            BigDecimal total = toDecimal(value(row, "totalCapacity", "total_capacity"));
            BigDecimal available = toDecimal(value(row, "availableCapacity", "available_capacity"));
            if (total == null) {
                if (used != null && available != null) total = used.add(available);
            }
            dto.setUsedCapacity(used);
            dto.setAvailableCapacity(available);

            dto.setUtilizationRate(calcRate(row, used, total));
            dto.setPeriod(str(row, "timePeriod", "time_period").isBlank() ? (period == null ? "" : period) : str(row, "timePeriod", "time_period"));
            dto.setPeriodDate(extractPeriodDate(row));
            dto.setRemark("");
            out.add(dto);
        }

        return out;
    }

    /**
     * 基于 APS 资源负载计算产能规划建议（不依赖排程明细结果）。
     *
     * @param params 产能规划参数
     * @return 产能规划结果列表
     */
    private List<CapacityPlanningResultDto> buildCapacityPlanningResults(Map<String, Object> params) {
        if (params == null) return List.of();
        String period = str(params, "planning_period", "planningPeriod");
        String startDate = str(params, "start_date", "startDate");
        String endDate = str(params, "end_date", "endDate");
        boolean considerOvertime = toBoolean(value(params, "consider_overtime", "considerOvertime"));
        boolean considerOutsource = toBoolean(value(params, "consider_outsource", "considerOutsource"));
        BigDecimal bufferRate = toDecimal(value(params, "capacity_buffer_rate", "capacityBufferRate"));
        if (bufferRate == null) bufferRate = BigDecimal.ZERO;

        List<CapacityDataDto> capacityRows = queryCapacityRows(null, period, startDate, endDate);
        if (capacityRows.isEmpty()) return List.of();

        List<CapacityPlanningResultDto> out = new ArrayList<>();
        for (CapacityDataDto c : capacityRows) {
            BigDecimal used = c.getUsedCapacity() != null ? c.getUsedCapacity() : BigDecimal.ZERO;
            BigDecimal available = c.getAvailableCapacity() != null ? c.getAvailableCapacity() : BigDecimal.ZERO;
            BigDecimal planned = used.multiply(BigDecimal.ONE.add(bufferRate.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP)));
            BigDecimal gap = planned.subtract(available);

            CapacityPlanningResultDto dto = new CapacityPlanningResultDto();
            dto.setDepartmentName(c.getDepartmentName());
            dto.setWorkCenterName(c.getWorkCenterName());
            dto.setResourceName(c.getResourceName());
            dto.setPlannedLoad(planned.setScale(2, RoundingMode.HALF_UP));
            dto.setAvailableCapacity(available.setScale(2, RoundingMode.HALF_UP));
            dto.setCapacityGap(gap.setScale(2, RoundingMode.HALF_UP));

            fillSuggestion(dto, gap, considerOvertime, considerOutsource);
            dto.setRemark("");
            out.add(dto);
        }

        return out;
    }

    /**
     * 按缺口和策略生成建议类型与建议内容。
     *
     * @param dto 结果 DTO
     * @param gap 产能缺口（正数表示缺口，负数表示富余）
     * @param considerOvertime 是否考虑加班
     * @param considerOutsource 是否考虑外协
     */
    private static void fillSuggestion(CapacityPlanningResultDto dto, BigDecimal gap, boolean considerOvertime, boolean considerOutsource) {
        if (dto == null) return;
        if (gap == null || gap.compareTo(BigDecimal.ZERO) <= 0) {
            dto.setSuggestionType("normal");
            dto.setSuggestionContent("产能充足，无需特殊处理");
            return;
        }
        if (considerOvertime) {
            dto.setSuggestionType("overtime");
            dto.setSuggestionContent("产能不足，建议安排加班或增加生产班次");
            return;
        }
        if (considerOutsource) {
            dto.setSuggestionType("outsource");
            dto.setSuggestionContent("产能不足，建议外协分担产能压力");
            return;
        }
        dto.setSuggestionType("hire");
        dto.setSuggestionContent("产能不足，建议补充人员或新增设备资源");
    }

    private static Object value(Map<String, Object> row, String k1, String k2) {
        if (row == null) return null;
        Object v = row.get(k1);
        if (v == null) v = row.get(k2);
        return v;
    }

    private static String str(Map<String, Object> row, String k1, String k2) {
        Object v = value(row, k1, k2);
        return v == null ? "" : String.valueOf(v);
    }

    private static BigDecimal toDecimal(Object v) {
        if (v == null) return null;
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Number n) return new BigDecimal(String.valueOf(n));
        try { return new BigDecimal(String.valueOf(v)); } catch (Exception e) { return null; }
    }

    private static boolean toBoolean(Object v) {
        if (v == null) return false;
        if (v instanceof Boolean b) return b;
        if (v instanceof Number n) return n.intValue() != 0;
        String s = String.valueOf(v).trim().toLowerCase();
        return "true".equals(s) || "1".equals(s) || "yes".equals(s) || "y".equals(s);
    }

    private static BigDecimal calcRate(Map<String, Object> row, BigDecimal used, BigDecimal total) {
        BigDecimal loadRate = toDecimal(value(row, "loadRate", "load_rate"));
        if (loadRate != null) {
            if (loadRate.compareTo(BigDecimal.ONE) <= 0) return loadRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
            return loadRate.setScale(2, RoundingMode.HALF_UP);
        }
        if (used == null || total == null || total.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return used.divide(total, 6, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    private static String extractPeriodDate(Map<String, Object> row) {
        String startTime = str(row, "startTime", "start_time");
        if (!startTime.isBlank()) {
            int t = startTime.indexOf('T');
            if (t > 0) return startTime.substring(0, t);
            int sp = startTime.indexOf(' ');
            if (sp > 0) return startTime.substring(0, sp);
            if (startTime.length() >= 10) return startTime.substring(0, 10);
        }
        return "";
    }

    private static boolean matchesDateRange(Map<String, Object> row, LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) return true;
        LocalDateTime rowStart = parseDateTime(str(row, "startTime", "start_time"), true);
        if (rowStart == null) return true;
        if (start != null && rowStart.isBefore(start)) return false;
        if (end != null && rowStart.isAfter(end)) return false;
        return true;
    }

    private static LocalDateTime parseDateTime(String v, boolean isStart) {
        if (v == null || v.isBlank()) return null;
        String s = v.trim();
        try {
            if (s.length() == 10) {
                LocalDate d = LocalDate.parse(s);
                return isStart ? d.atStartOfDay() : d.atTime(23, 59, 59);
            }
            if (s.contains("T")) {
                String normalized = s;
                int dot = normalized.indexOf('.');
                if (dot > 0) {
                    int tz = normalized.indexOf('Z', dot);
                    int plus = normalized.indexOf('+', dot);
                    int minus = normalized.indexOf('-', dot);
                    int cut = -1;
                    if (tz > 0) cut = tz;
                    if (plus > 0) cut = cut < 0 ? plus : Math.min(cut, plus);
                    if (minus > dot) cut = cut < 0 ? minus : Math.min(cut, minus);
                    if (cut > 0) normalized = normalized.substring(0, dot) + normalized.substring(cut);
                }
                if (normalized.endsWith("Z") || normalized.matches(".*[+-]\\d\\d:\\d\\d$")) {
                    return java.time.OffsetDateTime.parse(normalized).toLocalDateTime();
                }
                return LocalDateTime.parse(normalized);
            }
            if (s.length() >= 19) {
                return LocalDateTime.parse(s.replace(' ', 'T'));
            }
        } catch (DateTimeParseException ignore) {
        }
        return null;
    }
}
