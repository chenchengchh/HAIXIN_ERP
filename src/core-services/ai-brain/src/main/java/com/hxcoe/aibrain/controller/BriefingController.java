package com.hxcoe.aibrain.controller;

import com.hxcoe.aibrain.entity.AiSuggestionEntity;
import com.hxcoe.aibrain.service.briefing.MorningBriefingService;
import com.hxcoe.common.result.Result;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 晨会简报控制器（S05）
 */
@RestController
@RequestMapping("/api/v1/ai-brain/briefing")
public class BriefingController {

    @Autowired
    private MorningBriefingService morningBriefingService;

    /**
     * 手动触发六域巡检并生成简报（定时任务每日8:30自动执行）
     *
     * @return 本次巡检发现的简报条目
     */
    @PostMapping("/generate")
    public Result<List<AiSuggestionEntity>> generate() {
        return Result.success("巡检完成", morningBriefingService.generate());
    }
}
