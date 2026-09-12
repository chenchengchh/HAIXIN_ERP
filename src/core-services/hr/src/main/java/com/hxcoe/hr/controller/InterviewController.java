package com.hxcoe.hr.controller;

import com.hxcoe.hr.entity.InterviewEntity;
import com.hxcoe.hr.repository.InterviewRepository;
import com.hxcoe.common.result.Result;
import com.hxcoe.common.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 面试控制器
 */
@RestController
@RequestMapping({"/api/v1/hr/interviews", "/api/v1/hr/recruitment/interviews"})
@Tag(name = "面试管理", description = "面试管理相关接口")
public class InterviewController {

    private static final Logger logger = LoggerFactory.getLogger(InterviewController.class);

    @Autowired
    private InterviewRepository interviewRepository;

    /**
     * 获取所有面试记录
     */
    @GetMapping
    @Operation(summary = "获取所有面试记录", description = "获取所有面试记录列表")
    public Result<List<InterviewEntity>> getAllInterviews() {
        logger.info("获取所有面试记录");
        List<InterviewEntity> interviews = interviewRepository.findAll();
        return Result.success(interviews);
    }

    /**
     * 分页获取面试记录
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取面试记录", description = "分页查询面试记录")
    public Result<PageResult<InterviewEntity>> getInterviewsByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.info("分页获取面试记录: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Direction.DESC, "interviewTime"));
        Page<InterviewEntity> interviewPage = interviewRepository.findAll(pageable);
        
        PageResult<InterviewEntity> pageResult = new PageResult<>();
        pageResult.setTotal(interviewPage.getTotalElements());
        pageResult.setPageSize(interviewPage.getSize());
        pageResult.setCurrentPage(interviewPage.getNumber() + 1);
        pageResult.setRecords(interviewPage.getContent());
        
        return Result.success(pageResult);
    }

    /**
     * 创建面试记录
     */
    @PostMapping
    @Operation(summary = "创建面试记录", description = "创建新的面试记录")
    public Result<InterviewEntity> createInterview(@RequestBody InterviewEntity interview) {
        logger.info("创建面试记录");
        InterviewEntity createdInterview = interviewRepository.save(interview);
        return Result.success(createdInterview);
    }

    /**
     * 更新面试记录
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新面试记录", description = "根据ID更新面试记录")
    public Result<InterviewEntity> updateInterview(@PathVariable Long id, @RequestBody InterviewEntity interview) {
        logger.info("更新面试记录: id={}", id);
        interview.setId(id);
        InterviewEntity updatedInterview = interviewRepository.save(interview);
        return Result.success(updatedInterview);
    }

    /**
     * 删除面试记录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除面试记录", description = "根据ID删除面试记录")
    public Result<Void> deleteInterview(@PathVariable Long id) {
        logger.info("删除面试记录: id={}", id);
        interviewRepository.deleteById(id);
        return Result.success();
    }
}
