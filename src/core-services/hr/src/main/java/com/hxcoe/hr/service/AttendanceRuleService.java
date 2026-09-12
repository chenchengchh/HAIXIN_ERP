package com.hxcoe.hr.service;

import com.hxcoe.hr.entity.AttendanceRuleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 考勤规则Service接口
 */
public interface AttendanceRuleService {

    /**
     * 创建考勤规则
     * @param rule 考勤规则实体
     * @return 考勤规则实体
     */
    AttendanceRuleEntity createRule(AttendanceRuleEntity rule);

    /**
     * 根据ID查询考勤规则
     * @param id 考勤规则ID
     * @return 考勤规则实体
     */
    AttendanceRuleEntity getRuleById(Long id);

    /**
     * 更新考勤规则
     * @param id 考勤规则ID
     * @param rule 考勤规则实体
     * @return 考勤规则实体
     */
    AttendanceRuleEntity updateRule(Long id, AttendanceRuleEntity rule);

    /**
     * 删除考勤规则
     * @param id 考勤规则ID
     */
    void deleteRule(Long id);

    /**
     * 查询全部考勤规则
     * @return 考勤规则列表
     */
    List<AttendanceRuleEntity> getAllRules();

    /**
     * 分页查询考勤规则
     * @param pageable 分页参数
     * @return 考勤规则分页列表
     */
    Page<AttendanceRuleEntity> getRulesByPage(Pageable pageable);
}
