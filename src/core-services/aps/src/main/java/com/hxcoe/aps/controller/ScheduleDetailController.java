package com.hxcoe.aps.controller;

import com.hxcoe.aps.entity.ScheduleDetailEntity;
import com.hxcoe.aps.repository.ScheduleDetailRepository;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/aps/schedule-details")
public class ScheduleDetailController {

    @Autowired
    private ScheduleDetailRepository scheduleDetailRepository;

    @GetMapping("/plan/{planId}")
    public Result<List<ScheduleDetailEntity>> listByPlan(@PathVariable Long planId) {
        return Result.success(scheduleDetailRepository.findByPlanId(planId));
    }
}

