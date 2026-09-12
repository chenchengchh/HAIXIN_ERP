package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.NpsSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * NPS调查数据访问接口
 */
@Repository
public interface NpsSurveyRepository extends JpaRepository<NpsSurvey, Long>, JpaSpecificationExecutor<NpsSurvey> {

    /**
     * 统计评分大于等于指定分数的记录数（用于推荐者统计，score >= 9）
     * @param score 分数阈值
     * @return 记录数
     */
    long countByScoreGreaterThanEqual(Integer score);

    /**
     * 统计评分小于等于指定分数的记录数（用于贬损者统计，score <= 6）
     * @param score 分数阈值
     * @return 记录数
     */
    long countByScoreLessThanEqual(Integer score);
}
