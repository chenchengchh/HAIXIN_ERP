package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.NpsSurvey;
import com.hxcoe.crm.service.NpsSurveyService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * NPS调查控制器
 */
@RestController
@RequestMapping("/api/v1/crm/nps")
public class NpsController {

    @Autowired
    private NpsSurveyService npsSurveyService;

    /**
     * 提交NPS调查
     * @param survey NPS调查数据
     * @return 创建结果
     */
    @PostMapping
    public Result<NpsSurvey> createSurvey(@RequestBody NpsSurvey survey) {
        NpsSurvey createdSurvey = npsSurveyService.createSurvey(survey);
        return Result.success("成功", createdSurvey);
    }

    /**
     * 获取NPS得分（推荐者占比 - 贬损者占比）及分类统计
     * @return NPS统计数据
     */
    @GetMapping("/score")
    public Result<Map<String, Object>> getNpsScore() {
        Map<String, Object> npsScore = npsSurveyService.getNpsScore();
        return Result.success(npsScore);
    }

    /**
     * 分页查询NPS调查列表（仅查未删除数据，按id倒序）
     * @param page 页码
     * @param size 每页数量
     * @param keyword 关键字（可选，匹配客户名称/反馈意见）
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<PageResult<NpsSurvey>> getSurveyList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        // 转换页码，PageRequest从0开始，按id倒序
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<NpsSurvey> surveyPage = npsSurveyService.getSurveyList(keyword, pageable);
        PageResult<NpsSurvey> pageResult = PageResult.build(
                surveyPage.getTotalElements(),
                surveyPage.getSize(),
                surveyPage.getNumber() + 1,
                surveyPage.getContent()
        );
        return Result.success(pageResult);
    }
}
