package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.SatisfactionSurvey;
import com.hxcoe.crm.service.SatisfactionSurveyService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 满意度调查控制器
 */
@RestController
@RequestMapping("/api/v1/crm/surveys")
public class SurveyController {

    @Autowired
    private SatisfactionSurveyService satisfactionSurveyService;

    /**
     * 创建满意度调查
     * @param survey 调查数据
     * @return 创建结果
     */
    @PostMapping
    public Result<SatisfactionSurvey> createSurvey(@RequestBody SatisfactionSurvey survey) {
        SatisfactionSurvey createdSurvey = satisfactionSurveyService.createSurvey(survey);
        return Result.success("成功", createdSurvey);
    }

    /**
     * 查询客户的调查记录列表
     * @param customerId 客户ID
     * @return 调查记录列表
     */
    @GetMapping("/customer/{customerId}")
    public Result<List<SatisfactionSurvey>> getSurveysByCustomer(@PathVariable Long customerId) {
        List<SatisfactionSurvey> surveys = satisfactionSurveyService.getSurveysByCustomer(customerId);
        return Result.success(surveys);
    }

    /**
     * 获取满意度统计数据（总数、平均分、满意数量及满意占比）
     * @return 统计数据
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = satisfactionSurveyService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 分页查询满意度调查列表（仅查未删除数据，按id倒序）
     * @param page 页码
     * @param size 每页数量
     * @param keyword 关键字（可选，匹配客户名称/反馈意见）
     * @param surveyType 调查类型（可选）
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<SatisfactionSurvey>> getSurveyList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String surveyType) {

        // 转换页码，PageRequest从0开始，按id倒序
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<SatisfactionSurvey> surveyPage = satisfactionSurveyService.getSurveyList(keyword, surveyType, pageable);
        PageResult<SatisfactionSurvey> pageResult = PageResult.build(
                surveyPage.getTotalElements(),
                surveyPage.getSize(),
                surveyPage.getNumber() + 1,
                surveyPage.getContent()
        );
        return Result.success(pageResult);
    }
}
