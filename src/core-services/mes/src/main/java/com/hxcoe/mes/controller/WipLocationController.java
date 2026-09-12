package com.hxcoe.mes.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.mes.dto.WipLocationUpdateRequest;
import com.hxcoe.mes.entity.WipLocationEntity;
import com.hxcoe.mes.entity.WipLocationHistoryEntity;
import com.hxcoe.mes.repository.WipLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/mes/wip-locations", "/mes/v1/wip-locations", "/api/v1/mes/wip-locations", "/api/mes/wip-locations"})
public class WipLocationController {

    @Autowired
    private WipLocationRepository wipLocationRepository;

    @GetMapping
    public Result<List<WipLocationEntity>> list() {
        return Result.success("在制品位置列表查询成功", wipLocationRepository.findAll());
    }

    @PutMapping
    public Result<WipLocationEntity> update(@RequestBody WipLocationUpdateRequest request) {
        if (request.getSnCode() == null || request.getSnCode().isBlank()) {
            return Result.fail("snCode不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        WipLocationEntity entity = wipLocationRepository.findBySnCode(request.getSnCode()).orElseGet(() -> {
            WipLocationEntity created = new WipLocationEntity();
            created.setSnCode(request.getSnCode());
            created.setCreateTime(now);
            return created;
        });

        entity.setWorkOrderNo(request.getWorkOrderNo() != null ? request.getWorkOrderNo() : entity.getWorkOrderNo());

        String stationId = request.getStationId();
        String stationName = request.getStationName() != null ? request.getStationName() : stationId;
        String stepId = request.getStepId();
        String stepName = request.getStepName() != null ? request.getStepName() : stepId;

        if (stationId != null && !stationId.isBlank()) {
            entity.setCurrentStationId(stationId);
            entity.setCurrentStationName(stationName);
        }
        if (stepId != null && !stepId.isBlank()) {
            entity.setCurrentStepId(stepId);
            entity.setCurrentStepName(stepName);
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            entity.setStatus(request.getStatus());
        }
        entity.setUpdateTime(now);

        WipLocationHistoryEntity history = new WipLocationHistoryEntity();
        history.setWipLocation(entity);
        history.setStationId(stationId);
        history.setStationName(stationName);
        history.setStepId(stepId);
        history.setStepName(stepName);
        history.setStartTime(now);
        history.setEndTime(now);
        entity.getLocationHistory().add(history);

        return Result.success("在制品位置更新成功", wipLocationRepository.save(entity));
    }
}
