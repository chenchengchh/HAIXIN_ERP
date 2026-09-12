package com.hxcoe.hr.service.impl;

import com.hxcoe.hr.entity.AttendanceRuleEntity;
import com.hxcoe.hr.repository.AttendanceRuleRepository;
import com.hxcoe.hr.service.AttendanceRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 考勤规则Service实现类
 */
@Service
public class AttendanceRuleServiceImpl implements AttendanceRuleService {

    @Autowired
    private AttendanceRuleRepository attendanceRuleRepository;

    @Override
    public AttendanceRuleEntity createRule(AttendanceRuleEntity rule) {
        return attendanceRuleRepository.save(rule);
    }

    @Override
    public AttendanceRuleEntity getRuleById(Long id) {
        return attendanceRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("考勤规则不存在: " + id));
    }

    @Override
    @Transactional
    public AttendanceRuleEntity updateRule(Long id, AttendanceRuleEntity rule) {
        AttendanceRuleEntity existingRule = getRuleById(id);
        // 更新考勤规则字段
        existingRule.setRuleName(rule.getRuleName());
        existingRule.setWorkStartTime(rule.getWorkStartTime());
        existingRule.setWorkEndTime(rule.getWorkEndTime());
        existingRule.setLateTolerance(rule.getLateTolerance());
        existingRule.setEarlyLeaveTolerance(rule.getEarlyLeaveTolerance());
        existingRule.setApplicableDepartments(rule.getApplicableDepartments());
        existingRule.setStatus(rule.getStatus());
        existingRule.setRemark(rule.getRemark());
        return attendanceRuleRepository.save(existingRule);
    }

    @Override
    public void deleteRule(Long id) {
        AttendanceRuleEntity existingRule = getRuleById(id);
        attendanceRuleRepository.delete(existingRule);
    }

    @Override
    public List<AttendanceRuleEntity> getAllRules() {
        return attendanceRuleRepository.findAll();
    }

    @Override
    public Page<AttendanceRuleEntity> getRulesByPage(Pageable pageable) {
        return attendanceRuleRepository.findAll(pageable);
    }
}
