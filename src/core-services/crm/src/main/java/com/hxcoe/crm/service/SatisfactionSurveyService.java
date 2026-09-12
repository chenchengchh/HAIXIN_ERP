package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.SatisfactionSurvey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 满意度调查服务接口
 */
public interface SatisfactionSurveyService {

    /**
     * 创建满意度调查
     * @param survey 调查数据
     * @return 创建后的调查记录
     */
    SatisfactionSurvey createSurvey(SatisfactionSurvey survey);

    /**
     * 查询客户的调查记录列表
     * @param customerId 客户ID
     * @return 调查记录列表
     */
    List<SatisfactionSurvey> getSurveysByCustomer(Long customerId);

    /**
     * 获取满意度统计数据（总数、平均分、满意数量及占比等）
     * @return 统计数据Map
     */
    Map<String, Object> getStatistics();

    /**
     * 分页查询满意度调查列表（仅查未删除数据，关键字匹配客户名称/反馈意见，支持调查类型筛选，按id倒序）
     * @param keyword 关键字（可选，匹配customerName/feedback）
     * @param surveyType 调查类型（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<SatisfactionSurvey> getSurveyList(String keyword, String surveyType, Pageable pageable);
}
