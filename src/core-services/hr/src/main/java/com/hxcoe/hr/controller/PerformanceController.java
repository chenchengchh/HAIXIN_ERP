package com.hxcoe.hr.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.hr.entity.EmployeeEntity;
import com.hxcoe.hr.entity.PerformanceAppraisalEntity;
import com.hxcoe.hr.entity.PerformanceObjectiveEntity;
import com.hxcoe.hr.repository.EmployeeRepository;
import com.hxcoe.hr.repository.PerformanceAppraisalRepository;
import com.hxcoe.hr.repository.PerformanceObjectiveRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 绩效管理控制器
 */
@RestController
@RequestMapping("/api/v1/hr/performance")
@Tag(name = "绩效管理", description = "绩效目标与绩效评估相关接口")
public class PerformanceController {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceController.class);

    @Autowired
    private PerformanceObjectiveRepository objectiveRepository;

    @Autowired
    private PerformanceAppraisalRepository appraisalRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * 查询员工绩效目标
     * @param employeeId 员工ID（可选，为空则查询全部）
     * @return 绩效目标列表
     */
    @GetMapping("/objectives")
    @Operation(summary = "查询绩效目标", description = "按员工ID查询绩效目标列表")
    public Result<List<PerformanceObjectiveEntity>> getObjectives(@RequestParam(required = false) Long employeeId) {
        logger.info("查询绩效目标, employeeId={}", employeeId);
        List<PerformanceObjectiveEntity> list = employeeId == null
                ? objectiveRepository.findAll()
                : objectiveRepository.findByEmployee_Id(employeeId);
        return Result.success(list);
    }

    /**
     * 创建绩效目标
     * @param body 目标数据（employeeId/objectiveContent/targetValue/weight/startDate/endDate/status）
     * @return 创建结果
     */
    @PostMapping("/objectives")
    @Operation(summary = "创建绩效目标", description = "为员工创建绩效目标")
    public Result<PerformanceObjectiveEntity> createObjective(@RequestBody Map<String, Object> body) {
        logger.info("创建绩效目标: {}", body);
        PerformanceObjectiveEntity entity = new PerformanceObjectiveEntity();
        EmployeeEntity employee = resolveEmployee(body.get("employeeId"));
        if (employee == null) {
            return Result.fail("员工不存在");
        }
        entity.setEmployee(employee);
        entity.setObjectiveContent(asString(body.get("objectiveContent")));
        entity.setTargetValue(asString(body.get("targetValue")));
        entity.setWeight(asDouble(body.get("weight")));
        entity.setStartDate(asLocalDate(body.get("startDate")));
        entity.setEndDate(asLocalDate(body.get("endDate")));
        entity.setStatus(asString(body.get("status"), "ACTIVE"));
        entity.setRemark(asString(body.get("remark")));
        PerformanceObjectiveEntity saved = objectiveRepository.save(entity);
        return Result.success("成功", saved);
    }

    /**
     * 查询员工绩效评估
     * @param employeeId 员工ID（可选，为空则查询全部）
     * @return 绩效评估列表
     */
    @GetMapping("/appraisals")
    @Operation(summary = "查询绩效评估", description = "按员工ID查询绩效评估列表")
    public Result<List<PerformanceAppraisalEntity>> getAppraisals(@RequestParam(required = false) Long employeeId) {
        logger.info("查询绩效评估, employeeId={}", employeeId);
        List<PerformanceAppraisalEntity> list = employeeId == null
                ? appraisalRepository.findAll()
                : appraisalRepository.findByEmployee_Id(employeeId);
        return Result.success(list);
    }

    /**
     * 创建绩效评估
     * @param body 评估数据（employeeId/appraisalPeriod/objectiveScore/competencyScore/totalScore/appraisalStatus/appraiserId/appraisalNotes）
     * @return 创建结果
     */
    @PostMapping("/appraisals")
    @Operation(summary = "创建绩效评估", description = "为员工创建绩效评估")
    public Result<PerformanceAppraisalEntity> createAppraisal(@RequestBody Map<String, Object> body) {
        logger.info("创建绩效评估: {}", body);
        PerformanceAppraisalEntity entity = new PerformanceAppraisalEntity();
        EmployeeEntity employee = resolveEmployee(body.get("employeeId"));
        if (employee == null) {
            return Result.fail("员工不存在");
        }
        entity.setEmployee(employee);
        EmployeeEntity appraiser = resolveEmployee(body.get("appraiserId"));
        if (appraiser != null) {
            entity.setAppraiser(appraiser);
        }
        entity.setAppraisalPeriod(asString(body.get("appraisalPeriod")));
        entity.setObjectiveScore(asDouble(body.get("objectiveScore")));
        entity.setCompetencyScore(asDouble(body.get("competencyScore")));
        // 总分缺省时按目标分与能力分平均值计算
        Double totalScore = asDouble(body.get("totalScore"));
        if (totalScore == null && entity.getObjectiveScore() != null && entity.getCompetencyScore() != null) {
            totalScore = (entity.getObjectiveScore() + entity.getCompetencyScore()) / 2;
        }
        entity.setTotalScore(totalScore);
        entity.setAppraisalStatus(asString(body.get("appraisalStatus"), "PENDING"));
        entity.setRemark(asString(body.getOrDefault("appraisalNotes", body.get("remark"))));
        PerformanceAppraisalEntity saved = appraisalRepository.save(entity);
        return Result.success("成功", saved);
    }

    /**
     * 解析员工ID为员工实体引用
     * @param idValue 员工ID值
     * @return 员工实体，不存在时返回 null
     */
    private EmployeeEntity resolveEmployee(Object idValue) {
        Long id = asLong(idValue);
        if (id == null) {
            return null;
        }
        return employeeRepository.findById(id).orElse(null);
    }

    /**
     * 对象转字符串
     */
    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * 对象转字符串（带默认值）
     */
    private String asString(Object value, String defaultValue) {
        String str = asString(value);
        return str == null || str.isBlank() ? defaultValue : str;
    }

    /**
     * 对象转 Long
     */
    private Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 对象转 Double
     */
    private Double asDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 对象转 LocalDate（支持 yyyy-MM-dd 字符串）
     */
    private LocalDate asLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        try {
            String str = String.valueOf(value);
            return str.length() >= 10 ? LocalDate.parse(str.substring(0, 10)) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
