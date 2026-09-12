package com.hxcoe.oa.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.oa.entity.MeetingEntity;
import com.hxcoe.oa.service.MeetingService;
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
 * 会议管理Controller
 */
@RestController
@RequestMapping("/api/v1/oa/meeting")
@Tag(name = "会议管理", description = "会议相关API")
public class MeetingController {

    private static final Logger logger = LoggerFactory.getLogger(MeetingController.class);

    @Autowired
    private MeetingService meetingService;

    /**
     * 创建会议
     */
    @PostMapping
    @Operation(summary = "创建会议", description = "创建新的会议")
    public Result<MeetingEntity> createMeeting(@RequestBody MeetingEntity meeting) {
        logger.debug("创建会议: {}", meeting);
        MeetingEntity createdMeeting = meetingService.createMeeting(meeting);
        return Result.success(createdMeeting);
    }

    /**
     * 更新会议
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新会议", description = "更新指定ID的会议")
    public Result<MeetingEntity> updateMeeting(@PathVariable Long id, @RequestBody MeetingEntity meeting) {
        logger.debug("更新会议: id={}, {}", id, meeting);
        MeetingEntity updatedMeeting = meetingService.updateMeeting(id, meeting);
        return Result.success(updatedMeeting);
    }

    /**
     * 删除会议
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除会议", description = "删除指定ID的会议")
    public Result<Void> deleteMeeting(@PathVariable Long id) {
        logger.debug("删除会议: id={}", id);
        meetingService.deleteMeeting(id);
        return Result.success();
    }

    /**
     * 根据ID获取会议
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取会议", description = "根据ID获取会议详情")
    public Result<MeetingEntity> getMeetingById(@PathVariable Long id) {
        logger.debug("获取会议: id={}", id);
        MeetingEntity meeting = meetingService.getMeetingById(id);
        return Result.success(meeting);
    }

    /**
     * 获取会议列表
     */
    @GetMapping
    @Operation(summary = "获取会议列表", description = "获取所有会议列表")
    public Result<List<MeetingEntity>> getMeetingList() {
        logger.debug("获取会议列表");
        List<MeetingEntity> meetings = meetingService.getMeetingList();
        return Result.success(meetings);
    }

    /**
     * 分页获取会议列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取会议列表", description = "分页获取会议列表")
    public Result<PageResult<MeetingEntity>> getMeetingPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.debug("分页获取会议列表: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size); // 转换为0-based页码
        Page<MeetingEntity> meetingPage = meetingService.getMeetingPage(pageable);
        
        PageResult<MeetingEntity> pageResult = PageResult.build(
                meetingPage.getTotalElements(),
                size,
                page,
                meetingPage.getContent());
        
        return Result.success(pageResult);
    }

    /**
     * 根据会议室ID获取会议列表
     */
    @GetMapping("/room/{roomId}")
    @Operation(summary = "根据会议室ID获取会议列表", description = "根据会议室ID获取会议列表")
    public Result<List<MeetingEntity>> getMeetingByRoomId(@PathVariable Long roomId) {
        logger.debug("根据会议室ID获取会议列表: roomId={}", roomId);
        List<MeetingEntity> meetings = meetingService.getMeetingByRoomId(roomId);
        return Result.success(meetings);
    }

    /**
     * 根据发起人ID获取会议列表
     */
    @GetMapping("/creator/{creatorId}")
    @Operation(summary = "根据发起人ID获取会议列表", description = "根据发起人ID获取会议列表")
    public Result<List<MeetingEntity>> getMeetingByCreatorId(@PathVariable Long creatorId) {
        logger.debug("根据发起人ID获取会议列表: creatorId={}", creatorId);
        List<MeetingEntity> meetings = meetingService.getMeetingByCreatorId(creatorId);
        return Result.success(meetings);
    }

    /**
     * 根据状态获取会议列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取会议列表", description = "根据状态获取会议列表")
    public Result<List<MeetingEntity>> getMeetingByStatus(@PathVariable String status) {
        logger.debug("根据状态获取会议列表: status={}", status);
        List<MeetingEntity> meetings = meetingService.getMeetingByStatus(status);
        return Result.success(meetings);
    }

    /**
     * 取消会议
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消会议", description = "取消指定ID的会议")
    public Result<MeetingEntity> cancelMeeting(@PathVariable Long id) {
        logger.debug("取消会议: id={}", id);
        MeetingEntity meeting = meetingService.cancelMeeting(id);
        return Result.success(meeting);
    }

    /**
     * 开始会议
     */
    @PutMapping("/{id}/start")
    @Operation(summary = "开始会议", description = "开始指定ID的会议")
    public Result<MeetingEntity> startMeeting(@PathVariable Long id) {
        logger.debug("开始会议: id={}", id);
        MeetingEntity meeting = meetingService.startMeeting(id);
        return Result.success(meeting);
    }

    /**
     * 结束会议
     */
    @PutMapping("/{id}/end")
    @Operation(summary = "结束会议", description = "结束指定ID的会议")
    public Result<MeetingEntity> endMeeting(@PathVariable Long id) {
        logger.debug("结束会议: id={}", id);
        MeetingEntity meeting = meetingService.endMeeting(id);
        return Result.success(meeting);
    }
}