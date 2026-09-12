package com.hxcoe.oa.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.oa.entity.MeetingRoomEntity;
import com.hxcoe.oa.service.MeetingRoomService;
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
 * 会议室管理Controller
 */
@RestController
@RequestMapping("/api/v1/oa/meeting/room")
@Tag(name = "会议室管理", description = "会议室相关API")
public class MeetingRoomController {

    private static final Logger logger = LoggerFactory.getLogger(MeetingRoomController.class);

    @Autowired
    private MeetingRoomService meetingRoomService;

    /**
     * 创建会议室
     */
    @PostMapping
    @Operation(summary = "创建会议室", description = "创建新的会议室")
    public Result<MeetingRoomEntity> createMeetingRoom(@RequestBody MeetingRoomEntity room) {
        logger.debug("创建会议室: {}", room);
        MeetingRoomEntity createdRoom = meetingRoomService.createMeetingRoom(room);
        return Result.success(createdRoom);
    }

    /**
     * 更新会议室
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新会议室", description = "更新指定ID的会议室")
    public Result<MeetingRoomEntity> updateMeetingRoom(@PathVariable Long id, @RequestBody MeetingRoomEntity room) {
        logger.debug("更新会议室: id={}, {}", id, room);
        MeetingRoomEntity updatedRoom = meetingRoomService.updateMeetingRoom(id, room);
        return Result.success(updatedRoom);
    }

    /**
     * 删除会议室
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除会议室", description = "删除指定ID的会议室")
    public Result<Void> deleteMeetingRoom(@PathVariable Long id) {
        logger.debug("删除会议室: id={}", id);
        meetingRoomService.deleteMeetingRoom(id);
        return Result.success();
    }

    /**
     * 根据ID获取会议室
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取会议室", description = "根据ID获取会议室详情")
    public Result<MeetingRoomEntity> getMeetingRoomById(@PathVariable Long id) {
        logger.debug("获取会议室: id={}", id);
        MeetingRoomEntity room = meetingRoomService.getMeetingRoomById(id);
        return Result.success(room);
    }

    /**
     * 根据会议室编码获取会议室
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根据会议室编码获取会议室", description = "根据会议室编码获取会议室详情")
    public Result<MeetingRoomEntity> getMeetingRoomByCode(@PathVariable String code) {
        logger.debug("根据会议室编码获取会议室: code={}", code);
        MeetingRoomEntity room = meetingRoomService.getMeetingRoomByCode(code);
        return Result.success(room);
    }

    /**
     * 获取会议室列表
     */
    @GetMapping
    @Operation(summary = "获取会议室列表", description = "获取所有会议室列表")
    public Result<List<MeetingRoomEntity>> getMeetingRoomList() {
        logger.debug("获取会议室列表");
        List<MeetingRoomEntity> rooms = meetingRoomService.getMeetingRoomList();
        return Result.success(rooms);
    }

    /**
     * 分页获取会议室列表
     */
    @GetMapping("/page")
    @Operation(summary = "分页获取会议室列表", description = "分页获取会议室列表")
    public Result<PageResult<MeetingRoomEntity>> getMeetingRoomPage(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.debug("分页获取会议室列表: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page - 1, size); // 转换为0-based页码
        Page<MeetingRoomEntity> roomPage = meetingRoomService.getMeetingRoomPage(pageable);
        
        PageResult<MeetingRoomEntity> pageResult = PageResult.build(
                roomPage.getTotalElements(),
                size,
                page,
                roomPage.getContent());
        
        return Result.success(pageResult);
    }

    /**
     * 根据状态获取会议室列表
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "根据状态获取会议室列表", description = "根据状态获取会议室列表")
    public Result<List<MeetingRoomEntity>> getMeetingRoomByStatus(@PathVariable Integer status) {
        logger.debug("根据状态获取会议室列表: status={}", status);
        List<MeetingRoomEntity> rooms = meetingRoomService.getMeetingRoomByStatus(status);
        return Result.success(rooms);
    }

    /**
     * 根据容量获取会议室列表
     */
    @GetMapping("/capacity/{capacity}")
    @Operation(summary = "根据容量获取会议室列表", description = "根据容量获取会议室列表")
    public Result<List<MeetingRoomEntity>> getMeetingRoomByCapacityGreaterThanEqual(@PathVariable Integer capacity) {
        logger.debug("根据容量获取会议室列表: capacity={}", capacity);
        List<MeetingRoomEntity> rooms = meetingRoomService.getMeetingRoomByCapacityGreaterThanEqual(capacity);
        return Result.success(rooms);
    }

    /**
     * 根据设备获取会议室列表
     */
    @GetMapping("/equipment/{equipment}")
    @Operation(summary = "根据设备获取会议室列表", description = "根据设备获取会议室列表")
    public Result<List<MeetingRoomEntity>> getMeetingRoomByEquipmentContaining(@PathVariable String equipment) {
        logger.debug("根据设备获取会议室列表: equipment={}", equipment);
        List<MeetingRoomEntity> rooms = meetingRoomService.getMeetingRoomByEquipmentContaining(equipment);
        return Result.success(rooms);
    }

    /**
     * 启用会议室
     */
    @PutMapping("/{id}/enable")
    @Operation(summary = "启用会议室", description = "启用指定ID的会议室")
    public Result<MeetingRoomEntity> enableMeetingRoom(@PathVariable Long id) {
        logger.debug("启用会议室: id={}", id);
        MeetingRoomEntity room = meetingRoomService.enableMeetingRoom(id);
        return Result.success(room);
    }

    /**
     * 禁用会议室
     */
    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用会议室", description = "禁用指定ID的会议室")
    public Result<MeetingRoomEntity> disableMeetingRoom(@PathVariable Long id) {
        logger.debug("禁用会议室: id={}", id);
        MeetingRoomEntity room = meetingRoomService.disableMeetingRoom(id);
        return Result.success(room);
    }
}