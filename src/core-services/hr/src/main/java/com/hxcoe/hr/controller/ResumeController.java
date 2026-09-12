package com.hxcoe.hr.controller;

import com.hxcoe.hr.entity.ResumeEntity;
import com.hxcoe.hr.repository.ResumeRepository;
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
 * 简历控制器
 */
@RestController
@RequestMapping({"/api/v1/hr/resumes", "/api/v1/hr/recruitment/resumes"})
@Tag(name = "简历管理", description = "简历管理相关接口")
public class ResumeController {

    private static final Logger logger = LoggerFactory.getLogger(ResumeController.class);

    @Autowired
    private ResumeRepository resumeRepository;

    /**
     * 获取所有简历
     */
    @GetMapping
    @Operation(summary = "获取所有简历", description = "获取所有简历列表")
    public Result<List<ResumeEntity>> getAllResumes() {
        logger.info("获取所有简历");
        List<ResumeEntity> resumes = resumeRepository.findAll();
        return Result.success(resumes);
    }

    /**
     * 分页获取简历
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取简历", description = "分页查询简历")
    public Result<PageResult<ResumeEntity>> getResumesByPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        logger.info("分页获取简历: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Direction.DESC, "createdTime"));
        Page<ResumeEntity> resumePage = resumeRepository.findAll(pageable);
        
        PageResult<ResumeEntity> pageResult = new PageResult<>();
        pageResult.setTotal(resumePage.getTotalElements());
        pageResult.setPageSize(resumePage.getSize());
        pageResult.setCurrentPage(resumePage.getNumber() + 1);
        pageResult.setRecords(resumePage.getContent());
        
        return Result.success(pageResult);
    }

    /**
     * 创建简历
     */
    @PostMapping
    @Operation(summary = "创建简历", description = "创建新的简历")
    public Result<ResumeEntity> createResume(@RequestBody ResumeEntity resume) {
        logger.info("创建简历");
        ResumeEntity createdResume = resumeRepository.save(resume);
        return Result.success(createdResume);
    }

    /**
     * 更新简历
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新简历", description = "根据ID更新简历")
    public Result<ResumeEntity> updateResume(@PathVariable Long id, @RequestBody ResumeEntity resume) {
        logger.info("更新简历: id={}", id);
        resume.setId(id);
        ResumeEntity updatedResume = resumeRepository.save(resume);
        return Result.success(updatedResume);
    }

    /**
     * 删除简历
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除简历", description = "根据ID删除简历")
    public Result<Void> deleteResume(@PathVariable Long id) {
        logger.info("删除简历: id={}", id);
        resumeRepository.deleteById(id);
        return Result.success();
    }
}
