package com.hxcoe.hr.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.hr.entity.EmployeeEntity;
import com.hxcoe.hr.entity.PayrollRecordEntity;
import com.hxcoe.hr.entity.SalaryStructureEntity;
import com.hxcoe.hr.repository.EmployeeRepository;
import com.hxcoe.hr.repository.PayrollRecordRepository;
import com.hxcoe.hr.repository.SalaryStructureRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 薪酬结构与薪资计算控制器
 */
@RestController
@RequestMapping("/api/v1/hr/payroll")
@Tag(name = "薪酬管理", description = "薪酬结构与薪资计算相关接口")
public class SalaryStructureController {

    private static final Logger logger = LoggerFactory.getLogger(SalaryStructureController.class);

    @Autowired
    private SalaryStructureRepository salaryStructureRepository;

    @Autowired
    private PayrollRecordRepository payrollRecordRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * 获取所有薪酬结构
     * @return 薪酬结构列表
     */
    @GetMapping("/salary-structures")
    @Operation(summary = "获取所有薪酬结构", description = "获取所有薪酬结构列表")
    public Result<List<SalaryStructureEntity>> getAllSalaryStructures() {
        logger.info("获取所有薪酬结构");
        return Result.success(salaryStructureRepository.findAll());
    }

    /**
     * 创建薪酬结构
     * @param salaryStructure 薪酬结构数据
     * @return 创建结果
     */
    @PostMapping("/salary-structures")
    @Operation(summary = "创建薪酬结构", description = "创建新的薪酬结构")
    public Result<SalaryStructureEntity> createSalaryStructure(@RequestBody SalaryStructureEntity salaryStructure) {
        logger.info("创建薪酬结构: {}", salaryStructure.getName());
        if (salaryStructure.getStatus() == null) {
            salaryStructure.setStatus("ACTIVE");
        }
        SalaryStructureEntity saved = salaryStructureRepository.save(salaryStructure);
        return Result.success("成功", saved);
    }

    /**
     * 按月份计算薪资
     * 为所有在职员工按启用的薪酬结构生成（或更新）该月薪资记录
     * @param body 请求体，包含 month（格式 yyyy-MM）
     * @return 计算结果（生成记录数）
     */
    @PostMapping("/calculate")
    @Operation(summary = "计算薪资", description = "按月份为所有在职员工计算薪资")
    public Result<Map<String, Object>> calculatePayroll(@RequestBody Map<String, String> body) {
        String month = body == null ? null : body.get("month");
        logger.info("计算薪资, month={}", month);
        if (month == null || month.isBlank()) {
            return Result.fail("month 不能为空");
        }

        // 取第一个启用的薪酬结构作为默认薪酬基准
        SalaryStructureEntity structure = salaryStructureRepository.findAll().stream()
                .filter(s -> "ACTIVE".equalsIgnoreCase(s.getStatus()) || "1".equals(s.getStatus()))
                .findFirst()
                .orElse(null);

        List<EmployeeEntity> employees = employeeRepository.findAll();
        int created = 0;
        int updated = 0;
        for (EmployeeEntity emp : employees) {
            if (emp == null || emp.getId() == null) {
                continue;
            }
            PayrollRecordEntity record = payrollRecordRepository.findByEmployeeIdAndMonth(emp.getId(), month);
            if (record == null) {
                record = new PayrollRecordEntity();
                record.setEmployeeId(emp.getId());
                record.setMonth(month);
                record.setStatus(0);
                created++;
            } else {
                updated++;
            }
            BigDecimal basic = structure != null && structure.getBasicSalary() != null ? structure.getBasicSalary() : BigDecimal.ZERO;
            BigDecimal bonus = structure != null && structure.getBonus() != null ? structure.getBonus() : BigDecimal.ZERO;
            BigDecimal allowance = structure != null && structure.getAllowance() != null ? structure.getAllowance() : BigDecimal.ZERO;
            BigDecimal deduction = structure != null && structure.getDeduction() != null ? structure.getDeduction() : BigDecimal.ZERO;
            record.setBasicSalary(basic);
            record.setBonus(bonus);
            record.setAllowance(allowance);
            record.setDeduction(deduction);
            if (record.getPerformanceSalary() == null) {
                record.setPerformanceSalary(BigDecimal.ZERO);
            }
            // 实发工资 = 基本 + 绩效 + 奖金 + 补贴 - 扣减
            record.setActualSalary(basic.add(record.getPerformanceSalary()).add(bonus).add(allowance).subtract(deduction));
            payrollRecordRepository.save(record);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("month", month);
        result.put("created", created);
        result.put("updated", updated);
        result.put("total", created + updated);
        return Result.success("成功", result);
    }
}
