package com.hxcoe.srm.controller;

import com.hxcoe.srm.entity.ScoreCardEntity;
import com.hxcoe.srm.service.ScoreCardService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/srm")
public class ScoreCardController {

    @Autowired
    private ScoreCardService scoreCardService;

    @PostMapping({"/score-cards", "/scorecards"})
    public Result<ScoreCardEntity> createScoreCard(@RequestBody ScoreCardEntity scoreCard) {
        ScoreCardEntity result = scoreCardService.createScoreCard(scoreCard);
        return Result.success("评分卡创建成功", result);
    }

    @GetMapping({"/score-cards", "/scorecards"})
    public Result<PageResult<ScoreCardEntity>> getScoreCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ScoreCardEntity> result = scoreCardService.getScoreCards(pageable);
        PageResult<ScoreCardEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        );
        return Result.success("评分卡列表查询成功", pageResult);
    }

    @GetMapping({"/suppliers/{supplierId}/score-cards", "/suppliers/{supplierId}/scorecards"})
    public Result<List<ScoreCardEntity>> getScoreCardsBySupplierId(@PathVariable Long supplierId) {
        List<ScoreCardEntity> result = scoreCardService.getScoreCardsBySupplierId(supplierId);
        return Result.success("供应商评分卡查询成功", result);
    }

    @GetMapping({"/score-cards/{id}", "/scorecards/{id}"})
    public Result<ScoreCardEntity> getScoreCardById(@PathVariable Long id) {
        Optional<ScoreCardEntity> result = scoreCardService.getScoreCardById(id);
        return result.map(entity -> Result.success("评分卡查询成功", entity)).orElseGet(() -> Result.fail("评分卡不存在"));
    }

    @PutMapping({"/score-cards/{id}", "/scorecards/{id}"})
    public Result<ScoreCardEntity> updateScoreCard(@PathVariable Long id, @RequestBody ScoreCardEntity scoreCard) {
        ScoreCardEntity result = scoreCardService.updateScoreCard(id, scoreCard);
        if (result != null) {
            return Result.success("评分卡更新成功", result);
        } else {
            return Result.fail("评分卡不存在");
        }
    }

    @DeleteMapping({"/score-cards/{id}", "/scorecards/{id}"})
    public Result<String> deleteScoreCard(@PathVariable Long id) {
        scoreCardService.deleteScoreCard(id);
        return Result.success("评分卡删除成功");
    }
}
