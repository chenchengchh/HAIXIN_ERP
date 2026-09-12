package com.hxcoe.hr.controller;

import com.hxcoe.hr.entity.TrainingParticipantEntity;
import com.hxcoe.hr.service.TrainingParticipantService;
import com.hxcoe.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 培训参与Controller
 */
@RestController
@RequestMapping("/api/v1/hr/training-participants")
@Tag(name = "培训参与管理", description = "培训参与记录相关接口")
public class TrainingParticipantController {

    private static final Logger logger = LoggerFactory.getLogger(TrainingParticipantController.class);

    @Autowired
    private TrainingParticipantService trainingParticipantService;

    /**
     * 查询全部培训参与记录
     * @return 培训参与记录列表
     */
    @GetMapping
    @Operation(summary = "查询全部培训参与记录", description = "查询全部培训参与记录列表")
    public Result<List<TrainingParticipantEntity>> getAllTrainingParticipants() {
        logger.info("查询全部培训参与记录");
        List<TrainingParticipantEntity> participants = trainingParticipantService.getAllTrainingParticipants();
        return Result.success(participants);
    }

    /**
     * 分页查询培训参与记录
     * @param page 页码（从1开始）
     * @param size 每页条数
     * @return 培训参与记录分页列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询培训参与记录", description = "分页查询培训参与记录")
    public Result<Page<TrainingParticipantEntity>> getTrainingParticipantsByPage(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size) {
        logger.info("分页查询培训参与记录: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<TrainingParticipantEntity> participants = trainingParticipantService.getTrainingParticipantsByPage(pageable);
        return Result.success(participants);
    }

    /**
     * 根据ID查询培训参与记录
     * @param id 培训参与记录ID
     * @return 培训参与实体
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询培训参与记录", description = "根据ID查询培训参与记录")
    public Result<TrainingParticipantEntity> getTrainingParticipantById(@PathVariable Long id) {
        logger.info("查询培训参与记录: id={}", id);
        TrainingParticipantEntity participant = trainingParticipantService.getTrainingParticipantById(id);
        return Result.success(participant);
    }

    /**
     * 创建培训参与记录
     * @param trainingParticipant 培训参与实体
     * @return 培训参与实体
     */
    @PostMapping
    @Operation(summary = "创建培训参与记录", description = "创建新的培训参与记录")
    public Result<TrainingParticipantEntity> createTrainingParticipant(@RequestBody TrainingParticipantEntity trainingParticipant) {
        logger.info("创建培训参与记录");
        TrainingParticipantEntity createdParticipant = trainingParticipantService.createTrainingParticipant(trainingParticipant);
        return Result.success(createdParticipant);
    }

    /**
     * 更新培训参与记录
     * @param id 培训参与记录ID
     * @param trainingParticipant 培训参与实体
     * @return 培训参与实体
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新培训参与记录", description = "根据ID更新培训参与记录")
    public Result<TrainingParticipantEntity> updateTrainingParticipant(@PathVariable Long id, @RequestBody TrainingParticipantEntity trainingParticipant) {
        logger.info("更新培训参与记录: id={}", id);
        TrainingParticipantEntity updatedParticipant = trainingParticipantService.updateTrainingParticipant(id, trainingParticipant);
        return Result.success(updatedParticipant);
    }

    /**
     * 删除培训参与记录
     * @param id 培训参与记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除培训参与记录", description = "根据ID删除培训参与记录")
    public Result<Void> deleteTrainingParticipant(@PathVariable Long id) {
        logger.info("删除培训参与记录: id={}", id);
        trainingParticipantService.deleteTrainingParticipant(id);
        return Result.success();
    }

    /**
     * 根据培训计划ID查询参与记录
     * @param trainingId 培训计划ID
     * @return 培训参与记录列表
     */
    @GetMapping("/training/{trainingId}")
    @Operation(summary = "根据培训计划查询参与记录", description = "根据培训计划ID查询参与记录")
    public Result<List<TrainingParticipantEntity>> getByTrainingId(@PathVariable Long trainingId) {
        logger.info("根据培训计划查询参与记录: trainingId={}", trainingId);
        List<TrainingParticipantEntity> participants = trainingParticipantService.getByTrainingId(trainingId);
        return Result.success(participants);
    }

    /**
     * 根据员工ID查询参与记录
     * @param employeeId 员工ID
     * @return 培训参与记录列表
     */
    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "根据员工查询参与记录", description = "根据员工ID查询参与记录")
    public Result<List<TrainingParticipantEntity>> getByEmployeeId(@PathVariable Long employeeId) {
        logger.info("根据员工查询参与记录: employeeId={}", employeeId);
        List<TrainingParticipantEntity> participants = trainingParticipantService.getByEmployeeId(employeeId);
        return Result.success(participants);
    }
}
