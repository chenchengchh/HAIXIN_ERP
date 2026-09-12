package com.hxcoe.plm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.plm.entity.ProductEntity;
import com.hxcoe.plm.entity.QualityIssueEntity;
import com.hxcoe.plm.entity.TrialPlanEntity;
import com.hxcoe.plm.entity.TrialReportEntity;
import com.hxcoe.plm.repository.ProductRepository;
import com.hxcoe.plm.repository.QualityIssueRepository;
import com.hxcoe.plm.repository.TrialPlanRepository;
import com.hxcoe.plm.repository.TrialReportRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping({"/plm/trial", "/api/v1/plm/trial"})
public class TrialProductionController {

    @Autowired
    private TrialPlanRepository trialPlanRepository;

    @Autowired
    private TrialReportRepository trialReportRepository;

    @Autowired
    private QualityIssueRepository qualityIssueRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 分页获取试产计划列表。
     */
    @GetMapping("/plans")
    public ApiResponse<PageResult<Map<String, Object>>> getTrialPlans(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));

        Specification<TrialPlanEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeValue = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("planName"), likeValue),
                        cb.like(root.get("planCode"), likeValue)
                ));
            }
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TrialPlanEntity> result = trialPlanRepository.findAll(spec, pageable);
        List<Map<String, Object>> records = result.getContent().stream().map(this::toTrialPlanMap).toList();
        PageResult<Map<String, Object>> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, records);
        return ApiResponse.success(pageResult);
    }

    /**
     * 创建试产计划。
     */
    @PostMapping("/plans")
    public ApiResponse<Map<String, Object>> createTrialPlan(@RequestBody Map<String, Object> body) {
        String name = asString(body.get("name"));
        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error(400, "计划名称不能为空");
        }

        TrialPlanEntity entity = new TrialPlanEntity();
        entity.setProductId(0L);
        entity.setPlanCode(asString(body.get("code")));
        entity.setPlanName(name.trim());
        entity.setProductCode(asString(body.get("productCode")));
        entity.setProductName(asString(body.get("productName")));
        entity.setVersion(asString(body.get("version")));
        entity.setTrialType(asString(body.get("trialType")));
        entity.setTrialQty(asInteger(body.get("trialQty")));
        entity.setDepartmentsJson(toJson(body.get("departments")));
        entity.setStagesJson(toJson(body.get("stages")));
        entity.setResponsiblePerson(asString(body.get("responsiblePerson")));
        entity.setStatus(defaultIfBlank(asString(body.get("status")), "pending"));
        entity.setStartDate(parseLocalDate(asString(body.get("startTime"))));
        entity.setEndDate(parseLocalDate(asString(body.get("endTime"))));
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        TrialPlanEntity saved = trialPlanRepository.save(entity);
        return ApiResponse.success(toTrialPlanMap(saved));
    }

    /**
     * 更新试产计划。
     */
    @PutMapping("/plans/{id}")
    public ApiResponse<Map<String, Object>> updateTrialPlan(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        TrialPlanEntity entity = trialPlanRepository.findById(id).orElse(null);
        if (entity == null) {
            return ApiResponse.error(404, "试产计划不存在");
        }

        if (body.containsKey("code")) entity.setPlanCode(asString(body.get("code")));
        if (body.containsKey("name")) entity.setPlanName(asString(body.get("name")));
        if (body.containsKey("productCode")) entity.setProductCode(asString(body.get("productCode")));
        if (body.containsKey("productName")) entity.setProductName(asString(body.get("productName")));
        if (body.containsKey("version")) entity.setVersion(asString(body.get("version")));
        if (body.containsKey("trialType")) entity.setTrialType(asString(body.get("trialType")));
        if (body.containsKey("trialQty")) entity.setTrialQty(asInteger(body.get("trialQty")));
        if (body.containsKey("departments")) entity.setDepartmentsJson(toJson(body.get("departments")));
        if (body.containsKey("stages")) entity.setStagesJson(toJson(body.get("stages")));
        if (body.containsKey("responsiblePerson")) entity.setResponsiblePerson(asString(body.get("responsiblePerson")));
        if (body.containsKey("status")) entity.setStatus(asString(body.get("status")));
        if (body.containsKey("startTime")) entity.setStartDate(parseLocalDate(asString(body.get("startTime"))));
        if (body.containsKey("endTime")) entity.setEndDate(parseLocalDate(asString(body.get("endTime"))));
        entity.setUpdatedTime(LocalDateTime.now());

        TrialPlanEntity saved = trialPlanRepository.save(entity);
        return ApiResponse.success(toTrialPlanMap(saved));
    }

    /**
     * 删除试产计划。
     */
    @DeleteMapping("/plans/{id}")
    public ApiResponse<Void> deleteTrialPlan(@PathVariable("id") Long id) {
        if (!trialPlanRepository.existsById(id)) {
            return ApiResponse.error(404, "试产计划不存在");
        }
        trialPlanRepository.deleteById(id);
        return ApiResponse.success(null);
    }

    /**
     * 分页获取试产报告列表。
     */
    @GetMapping("/reports")
    public ApiResponse<PageResult<Map<String, Object>>> getTrialReports(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));

        Specification<TrialReportEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeValue = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("reportTitle"), likeValue),
                        cb.like(root.get("reportCode"), likeValue)
                ));
            }
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TrialReportEntity> result = trialReportRepository.findAll(spec, pageable);
        List<Map<String, Object>> records = result.getContent().stream().map(this::toTrialReportMap).toList();
        PageResult<Map<String, Object>> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, records);
        return ApiResponse.success(pageResult);
    }

    /**
     * 创建试产报告。
     */
    @PostMapping("/reports")
    public ApiResponse<Map<String, Object>> createTrialReport(@RequestBody Map<String, Object> body) {
        String title = asString(body.get("reportTitle"));
        if (title == null || title.trim().isEmpty()) {
            title = asString(body.get("title"));
        }
        if (title == null || title.trim().isEmpty()) {
            return ApiResponse.error(400, "报告标题不能为空");
        }

        TrialReportEntity entity = new TrialReportEntity();
        // 优先按planId关联，未传时按planCode解析试产计划
        Long planId = asLong(body.get("planId"));
        if (planId == null) {
            String planCode = asString(body.get("planCode"));
            if (planCode != null) {
                TrialPlanEntity plan = trialPlanRepository.findByPlanCode(planCode).orElse(null);
                planId = plan == null ? null : plan.getId();
            }
        }
        entity.setPlanId(planId);
        entity.setReportCode(asString(body.get("code")));
        entity.setReportTitle(title.trim());
        entity.setContent(asString(body.get("content")));
        entity.setStatus(defaultIfBlank(asString(body.get("status")), "draft"));
        entity.setTrialQty(asInteger(body.get("trialQty")));
        entity.setPassQty(asInteger(body.get("passQty")));
        entity.setYieldRate(asDouble(body.get("yield")));
        entity.setCreateUser(defaultIfBlank(asString(body.get("createUser")), "当前用户"));
        entity.setCreateTime(parseLocalDate(asString(body.get("createTime"))));
        entity.setApproveUser(asString(body.get("approveUser")));
        entity.setApproveTime(parseLocalDate(asString(body.get("approveTime"))));
        entity.setMainIssuesJson(toJson(body.get("mainIssues")));
        entity.setImprovementsJson(toJson(body.get("improvements")));
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        TrialReportEntity saved = trialReportRepository.save(entity);
        return ApiResponse.success(toTrialReportMap(saved));
    }

    /**
     * 更新试产报告。
     */
    @PutMapping("/reports/{id}")
    public ApiResponse<Map<String, Object>> updateTrialReport(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        TrialReportEntity entity = trialReportRepository.findById(id).orElse(null);
        if (entity == null) {
            return ApiResponse.error(404, "试产报告不存在");
        }

        if (body.containsKey("planId")) entity.setPlanId(asLong(body.get("planId")));
        // 前端按计划编码更新时同步刷新计划关联
        if (body.containsKey("planCode")) {
            String updatedPlanCode = asString(body.get("planCode"));
            TrialPlanEntity plan = updatedPlanCode == null ? null
                    : trialPlanRepository.findByPlanCode(updatedPlanCode).orElse(null);
            entity.setPlanId(plan == null ? null : plan.getId());
        }
        if (body.containsKey("code")) entity.setReportCode(asString(body.get("code")));
        if (body.containsKey("reportTitle")) entity.setReportTitle(asString(body.get("reportTitle")));
        if (body.containsKey("title")) entity.setReportTitle(asString(body.get("title")));
        if (body.containsKey("content")) entity.setContent(asString(body.get("content")));
        if (body.containsKey("status")) entity.setStatus(asString(body.get("status")));
        if (body.containsKey("trialQty")) entity.setTrialQty(asInteger(body.get("trialQty")));
        if (body.containsKey("passQty")) entity.setPassQty(asInteger(body.get("passQty")));
        if (body.containsKey("yield")) entity.setYieldRate(asDouble(body.get("yield")));
        if (body.containsKey("createUser")) entity.setCreateUser(asString(body.get("createUser")));
        if (body.containsKey("createTime")) entity.setCreateTime(parseLocalDate(asString(body.get("createTime"))));
        if (body.containsKey("approveUser")) entity.setApproveUser(asString(body.get("approveUser")));
        if (body.containsKey("approveTime")) entity.setApproveTime(parseLocalDate(asString(body.get("approveTime"))));
        if (body.containsKey("mainIssues")) entity.setMainIssuesJson(toJson(body.get("mainIssues")));
        if (body.containsKey("improvements")) entity.setImprovementsJson(toJson(body.get("improvements")));
        entity.setUpdatedTime(LocalDateTime.now());

        TrialReportEntity saved = trialReportRepository.save(entity);
        return ApiResponse.success(toTrialReportMap(saved));
    }

    /**
     * 删除试产报告。
     */
    @DeleteMapping("/reports/{id}")
    public ApiResponse<Void> deleteTrialReport(@PathVariable("id") Long id) {
        if (!trialReportRepository.existsById(id)) {
            return ApiResponse.error(404, "试产报告不存在");
        }
        trialReportRepository.deleteById(id);
        return ApiResponse.success(null);
    }

    /**
     * 分页获取质量问题列表。
     */
    @GetMapping("/quality-issues")
    public ApiResponse<PageResult<Map<String, Object>>> getQualityIssues(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));

        Specification<QualityIssueEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeValue = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("issueTitle"), likeValue),
                        cb.like(root.get("issueCode"), likeValue)
                ));
            }
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<QualityIssueEntity> result = qualityIssueRepository.findAll(spec, pageable);
        List<Map<String, Object>> records = result.getContent().stream().map(this::toQualityIssueMap).toList();
        PageResult<Map<String, Object>> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, records);
        return ApiResponse.success(pageResult);
    }

    /**
     * 创建质量问题。
     */
    @PostMapping("/quality-issues")
    public ApiResponse<Map<String, Object>> createQualityIssue(@RequestBody Map<String, Object> body) {
        String issue = asString(body.get("issue"));
        if (issue == null || issue.trim().isEmpty()) {
            issue = asString(body.get("issueTitle"));
        }
        if (issue == null || issue.trim().isEmpty()) {
            return ApiResponse.error(400, "问题描述不能为空");
        }

        QualityIssueEntity entity = new QualityIssueEntity();
        // 前端提交产品编码时解析为产品ID建立关联，未匹配则置0
        String productCode = asString(body.get("productCode"));
        Long productId = 0L;
        if (productCode != null) {
            productId = productRepository.findByProductCode(productCode)
                    .map(ProductEntity::getId)
                    .orElse(0L);
        }
        entity.setProductId(productId);
        entity.setIssueCode(asString(body.get("code")));
        entity.setIssueTitle(issue.trim());
        entity.setSeverity(defaultIfBlank(asString(body.get("severity")), "medium"));
        entity.setStatus(defaultIfBlank(asString(body.get("status")), "in-progress"));
        entity.setResolution(asString(body.get("resolution")));
        entity.setCreateUser(defaultIfBlank(asString(body.get("createUser")), "当前用户"));
        entity.setCreateTime(parseLocalDate(asString(body.get("createTime"))));
        entity.setResolveUser(asString(body.get("resolveUser")));
        entity.setResolveTime(parseLocalDate(asString(body.get("resolveTime"))));
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        QualityIssueEntity saved = qualityIssueRepository.save(entity);
        return ApiResponse.success(toQualityIssueMap(saved));
    }

    /**
     * 更新质量问题。
     */
    @PutMapping("/quality-issues/{id}")
    public ApiResponse<Map<String, Object>> updateQualityIssue(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        QualityIssueEntity entity = qualityIssueRepository.findById(id).orElse(null);
        if (entity == null) {
            return ApiResponse.error(404, "质量问题不存在");
        }

        if (body.containsKey("code")) entity.setIssueCode(asString(body.get("code")));
        // 前端更新产品编码时同步刷新产品关联
        if (body.containsKey("productCode")) {
            String updatedProductCode = asString(body.get("productCode"));
            Long updatedProductId = updatedProductCode == null ? 0L
                    : productRepository.findByProductCode(updatedProductCode).map(ProductEntity::getId).orElse(0L);
            entity.setProductId(updatedProductId);
        }
        if (body.containsKey("issue")) entity.setIssueTitle(asString(body.get("issue")));
        if (body.containsKey("issueTitle")) entity.setIssueTitle(asString(body.get("issueTitle")));
        if (body.containsKey("severity")) entity.setSeverity(asString(body.get("severity")));
        if (body.containsKey("status")) entity.setStatus(asString(body.get("status")));
        if (body.containsKey("resolution")) entity.setResolution(asString(body.get("resolution")));
        if (body.containsKey("createUser")) entity.setCreateUser(asString(body.get("createUser")));
        if (body.containsKey("createTime")) entity.setCreateTime(parseLocalDate(asString(body.get("createTime"))));
        if (body.containsKey("resolveUser")) entity.setResolveUser(asString(body.get("resolveUser")));
        if (body.containsKey("resolveTime")) entity.setResolveTime(parseLocalDate(asString(body.get("resolveTime"))));
        entity.setUpdatedTime(LocalDateTime.now());

        QualityIssueEntity saved = qualityIssueRepository.save(entity);
        return ApiResponse.success(toQualityIssueMap(saved));
    }

    /**
     * 删除质量问题。
     */
    @DeleteMapping("/quality-issues/{id}")
    public ApiResponse<Void> deleteQualityIssue(@PathVariable("id") Long id) {
        if (!qualityIssueRepository.existsById(id)) {
            return ApiResponse.error(404, "质量问题不存在");
        }
        qualityIssueRepository.deleteById(id);
        return ApiResponse.success(null);
    }

    private Map<String, Object> toTrialPlanMap(TrialPlanEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId() == null ? null : String.valueOf(entity.getId()));
        map.put("code", entity.getPlanCode());
        map.put("name", entity.getPlanName());
        map.put("productCode", entity.getProductCode());
        map.put("productName", entity.getProductName());
        map.put("status", defaultIfBlank(entity.getStatus(), "pending"));
        map.put("trialType", entity.getTrialType());
        map.put("trialQty", entity.getTrialQty());
        map.put("version", entity.getVersion());
        map.put("departments", parseJsonArray(entity.getDepartmentsJson()));
        map.put("stages", parseJsonArray(entity.getStagesJson()));
        map.put("startTime", entity.getStartDate() == null ? null : entity.getStartDate().toString());
        map.put("endTime", entity.getEndDate() == null ? null : entity.getEndDate().toString());
        map.put("responsiblePerson", entity.getResponsiblePerson());
        return map;
    }

    private Map<String, Object> toTrialReportMap(TrialReportEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId() == null ? null : String.valueOf(entity.getId()));
        map.put("code", entity.getReportCode());
        map.put("planId", entity.getPlanId());
        map.put("reportTitle", entity.getReportTitle());
        map.put("status", entity.getStatus());
        map.put("content", entity.getContent());
        map.put("createTime", entity.getCreateTime() == null ? null : entity.getCreateTime().toString());
        map.put("createUser", entity.getCreateUser());
        map.put("approveTime", entity.getApproveTime() == null ? null : entity.getApproveTime().toString());
        map.put("approveUser", entity.getApproveUser());
        map.put("mainIssues", parseJsonArray(entity.getMainIssuesJson()));
        map.put("improvements", parseJsonArray(entity.getImprovementsJson()));
        // 根据关联试产计划回填计划与产品信息，试产数量优先取报告自身记录
        TrialPlanEntity plan = entity.getPlanId() == null ? null
                : trialPlanRepository.findById(entity.getPlanId()).orElse(null);
        if (plan != null) {
            map.put("planCode", plan.getPlanCode());
            map.put("planName", plan.getPlanName());
            map.put("productCode", plan.getProductCode());
            map.put("productName", plan.getProductName());
            map.put("version", plan.getVersion());
            map.put("trialQty", entity.getTrialQty() != null ? entity.getTrialQty() : plan.getTrialQty());
        } else {
            map.put("planCode", null);
            map.put("planName", null);
            map.put("productCode", null);
            map.put("productName", null);
            map.put("version", null);
            map.put("trialQty", entity.getTrialQty());
        }
        map.put("passQty", entity.getPassQty());
        map.put("yield", entity.getYieldRate());
        return map;
    }

    private Map<String, Object> toQualityIssueMap(QualityIssueEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId() == null ? null : String.valueOf(entity.getId()));
        map.put("code", entity.getIssueCode());
        map.put("issue", entity.getIssueTitle());
        map.put("status", entity.getStatus());
        map.put("severity", entity.getSeverity());
        map.put("resolution", entity.getResolution());
        map.put("createTime", entity.getCreateTime() == null ? null : entity.getCreateTime().toString());
        map.put("createUser", entity.getCreateUser());
        map.put("resolveTime", entity.getResolveTime() == null ? null : entity.getResolveTime().toString());
        map.put("resolveUser", entity.getResolveUser());
        // 根据产品ID回填产品名称与编码，供前端展示关联产品
        if (entity.getProductId() != null && entity.getProductId() > 0) {
            ProductEntity product = productRepository.findById(entity.getProductId()).orElse(null);
            if (product != null) {
                map.put("productName", product.getProductName());
                map.put("productCode", product.getProductCode());
                map.put("version", product.getVersion());
            } else {
                map.put("productName", null);
                map.put("productCode", null);
                map.put("version", null);
            }
        } else {
            map.put("productName", null);
            map.put("productCode", null);
            map.put("version", null);
        }
        return map;
    }

    private String asString(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value);
        return text.isEmpty() ? null : text;
    }

    private Integer asInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long asLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 将请求参数安全转换为Double，无法解析时返回null。
     */
    private Double asDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) return defaultValue;
        return value;
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<Object> parseJsonArray(String json) {
        if (json == null || json.trim().isEmpty()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Object>>() {});
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }
}
