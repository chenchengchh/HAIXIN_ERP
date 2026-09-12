package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.SatisfactionSurvey;
import com.hxcoe.crm.repository.SatisfactionSurveyRepository;
import com.hxcoe.crm.service.SatisfactionSurveyService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 满意度调查服务实现类
 */
@Service
public class SatisfactionSurveyServiceImpl implements SatisfactionSurveyService {

    @Autowired
    private SatisfactionSurveyRepository satisfactionSurveyRepository;

    /**
     * 创建满意度调查，默认调查类型为 SATISFACTION
     * @param survey 调查数据
     * @return 创建后的调查记录
     */
    @Override
    public SatisfactionSurvey createSurvey(SatisfactionSurvey survey) {
        if (survey.getSurveyType() == null || survey.getSurveyType().isEmpty()) {
            survey.setSurveyType("SATISFACTION");
        }
        return satisfactionSurveyRepository.save(survey);
    }

    /**
     * 查询客户的调查记录列表
     * @param customerId 客户ID
     * @return 调查记录列表
     */
    @Override
    public List<SatisfactionSurvey> getSurveysByCustomer(Long customerId) {
        return satisfactionSurveyRepository.findByCustomerId(customerId);
    }

    /**
     * 获取满意度统计数据（总数、平均分、满意数量及满意占比）
     * @return 统计数据Map
     */
    @Override
    public Map<String, Object> getStatistics() {
        long totalCount = satisfactionSurveyRepository.count();
        Double avgScore = satisfactionSurveyRepository.findAverageScore();
        long satisfiedCount = satisfactionSurveyRepository.countByScoreGreaterThanEqual(4);
        double satisfiedRate = totalCount > 0 ? (double) satisfiedCount / totalCount * 100 : 0;

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalCount", totalCount);
        statistics.put("avgScore", avgScore != null ? Math.round(avgScore * 100.0) / 100.0 : 0);
        statistics.put("satisfiedCount", satisfiedCount);
        statistics.put("satisfiedRate", Math.round(satisfiedRate * 100.0) / 100.0);
        return statistics;
    }

    /**
     * 分页查询满意度调查列表，仅查询未删除数据，按id倒序
     * @param keyword 关键字（可选，模糊匹配客户名称/反馈意见）
     * @param surveyType 调查类型（可选）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Page<SatisfactionSurvey> getSurveyList(String keyword, String surveyType, Pageable pageable) {
        Specification<SatisfactionSurvey> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 仅查询未删除数据
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.or(
                        cb.like(root.get("customerName"), "%" + keyword + "%"),
                        cb.like(root.get("feedback"), "%" + keyword + "%")
                ));
            }
            if (surveyType != null && !surveyType.isBlank()) {
                predicates.add(cb.equal(root.get("surveyType"), surveyType));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return satisfactionSurveyRepository.findAll(spec, pageable);
    }
}
