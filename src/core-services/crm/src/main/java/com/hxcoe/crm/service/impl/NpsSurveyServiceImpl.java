package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.NpsSurvey;
import com.hxcoe.crm.repository.NpsSurveyRepository;
import com.hxcoe.crm.service.NpsSurveyService;
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
 * NPS调查服务实现类
 */
@Service
public class NpsSurveyServiceImpl implements NpsSurveyService {

    @Autowired
    private NpsSurveyRepository npsSurveyRepository;

    /**
     * 提交NPS调查
     * @param survey NPS调查数据
     * @return 创建后的NPS记录
     */
    @Override
    public NpsSurvey createSurvey(NpsSurvey survey) {
        return npsSurveyRepository.save(survey);
    }

    /**
     * 获取NPS得分（推荐者占比 - 贬损者占比）及分类统计
     * 评分 >= 9 为推荐者，<= 6 为贬损者，其余为被动者
     * @return NPS统计数据Map
     */
    @Override
    public Map<String, Object> getNpsScore() {
        long totalCount = npsSurveyRepository.count();
        long promoters = npsSurveyRepository.countByScoreGreaterThanEqual(9);
        long detractors = npsSurveyRepository.countByScoreLessThanEqual(6);
        long passives = totalCount - promoters - detractors;
        double npsScore = totalCount > 0
                ? ((double) promoters / totalCount - (double) detractors / totalCount) * 100
                : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("npsScore", Math.round(npsScore * 100.0) / 100.0);
        result.put("totalCount", totalCount);
        result.put("promoters", promoters);
        result.put("passives", passives);
        result.put("detractors", detractors);
        return result;
    }

    /**
     * 分页查询NPS调查列表，仅查询未删除数据，按id倒序
     * @param keyword 关键字（可选，模糊匹配客户名称/反馈意见）
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Page<NpsSurvey> getSurveyList(String keyword, Pageable pageable) {
        Specification<NpsSurvey> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            // 仅查询未删除数据
            predicates.add(cb.equal(root.get("isDeleted"), 0));
            if (keyword != null && !keyword.isBlank()) {
                predicates.add(cb.or(
                        cb.like(root.get("customerName"), "%" + keyword + "%"),
                        cb.like(root.get("feedback"), "%" + keyword + "%")
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return npsSurveyRepository.findAll(spec, pageable);
    }
}
