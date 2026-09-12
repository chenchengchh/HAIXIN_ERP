package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.NpsSurvey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * NPS调查服务接口
 */
public interface NpsSurveyService {

    /**
     * 提交NPS调查
     * @param survey NPS调查数据
     * @return 创建后的NPS记录
     */
    NpsSurvey createSurvey(NpsSurvey survey);

    /**
     * 获取NPS得分（推荐者占比 - 贬损者占比）及分类统计
     * @return NPS统计数据Map
     */
    Map<String, Object> getNpsScore();

    /**
     * 分页查询NPS调查列表（仅查未删除数据，关键字匹配客户名称/反馈意见，按id倒序）
     * @param keyword 关键字（可选，匹配customerName/feedback）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<NpsSurvey> getSurveyList(String keyword, Pageable pageable);
}
