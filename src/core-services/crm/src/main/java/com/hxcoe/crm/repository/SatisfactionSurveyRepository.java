package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.SatisfactionSurvey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 满意度调查数据访问接口
 */
@Repository
public interface SatisfactionSurveyRepository extends JpaRepository<SatisfactionSurvey, Long>, JpaSpecificationExecutor<SatisfactionSurvey> {

    /**
     * 根据客户ID查询调查记录列表
     * @param customerId 客户ID
     * @return 调查记录列表
     */
    List<SatisfactionSurvey> findByCustomerId(Long customerId);

    /**
     * 统计评分大于等于指定分数的记录数（用于满意数量统计）
     * @param score 分数阈值
     * @return 记录数
     */
    long countByScoreGreaterThanEqual(Integer score);

    /**
     * 计算所有调查记录的平均评分
     * @return 平均评分
     */
    @Query("SELECT AVG(s.score) FROM SatisfactionSurvey s")
    Double findAverageScore();
}
