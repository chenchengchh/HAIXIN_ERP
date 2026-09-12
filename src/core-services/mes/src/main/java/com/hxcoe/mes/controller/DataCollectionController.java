package com.hxcoe.mes.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.mes.client.QmsClient;
import com.hxcoe.mes.client.dto.qms.QualityInspectionDTO;
import com.hxcoe.mes.dto.SimpleStatusUpdateRequest;
import com.hxcoe.mes.entity.EquipmentDataEntity;
import com.hxcoe.mes.entity.ManualReportingEntity;
import com.hxcoe.mes.entity.QualityInspectionEntity;
import com.hxcoe.mes.repository.EquipmentDataRepository;
import com.hxcoe.mes.repository.ManualReportingRepository;
import com.hxcoe.mes.repository.QualityInspectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/mes/data-collection", "/mes/v1/data-collection", "/api/v1/mes/data-collection", "/api/mes/data-collection"})
public class DataCollectionController {

    private static final Logger log = LoggerFactory.getLogger(DataCollectionController.class);

    @Autowired
    private ManualReportingRepository manualReportingRepository;

    @Autowired
    private EquipmentDataRepository equipmentDataRepository;

    @Autowired
    private QualityInspectionRepository qualityInspectionRepository;

    @Autowired(required = false)
    private QmsClient qmsClient;

    @GetMapping("/manual")
    public Result<List<ManualReportingEntity>> listManual() {
        return Result.success("人工报工采集列表查询成功", manualReportingRepository.findAll());
    }

    @PostMapping("/manual")
    public Result<ManualReportingEntity> createManual(@RequestBody ManualReportingEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("submitted");
        }
        return Result.success("人工报工采集创建成功", manualReportingRepository.save(entity));
    }

    @PutMapping("/manual/{id}/status")
    public Result<ManualReportingEntity> updateManualStatus(
            @PathVariable("id") Long id,
            @RequestBody(required = false) SimpleStatusUpdateRequest body,
            @RequestParam(value = "status", required = false) String status) {
        String nextStatus = body != null && body.getStatus() != null ? body.getStatus() : status;
        if (nextStatus == null || nextStatus.isBlank()) {
            return Result.fail("status不能为空");
        }
        ManualReportingEntity entity = manualReportingRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.fail("人工报工不存在");
        }
        entity.setStatus(nextStatus);
        entity.setUpdateTime(LocalDateTime.now());
        return Result.success("人工报工状态更新成功", manualReportingRepository.save(entity));
    }

    @GetMapping("/equipment")
    public Result<List<EquipmentDataEntity>> listEquipment() {
        return Result.success("设备数据采集列表查询成功", equipmentDataRepository.findAll());
    }

    @GetMapping("/equipment/{equipmentId}/realtime")
    public Result<List<EquipmentDataEntity>> realtimeEquipment(@PathVariable("equipmentId") String equipmentId) {
        return Result.success("实时设备数据查询成功", equipmentDataRepository.findTop50ByEquipmentIdOrderByTimestampDesc(equipmentId));
    }

    @GetMapping("/quality")
    public Result<List<QualityInspectionEntity>> listQuality() {
        return Result.success("质量检验采集列表查询成功", qualityInspectionRepository.findAll());
    }

    @PostMapping("/quality")
    public Result<QualityInspectionEntity> createQuality(@RequestBody QualityInspectionEntity entity) {
        if (entity.getCreateTime() == null) {
            entity.setCreateTime(LocalDateTime.now());
        }
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("pending");
        }
        QualityInspectionEntity saved = qualityInspectionRepository.save(entity);
        try {
            if (qmsClient != null) {
                QualityInspectionDTO dto = new QualityInspectionDTO();
                dto.setInspectionCode(saved.getInspectionId() != null ? saved.getInspectionId() : "MES-QI-" + saved.getId());
                dto.setProductCode((saved.getSnCode() != null && !saved.getSnCode().isBlank()) ? saved.getSnCode() : "MES-PROD-" + saved.getId());
                dto.setProductName((saved.getInspectionName() != null && !saved.getInspectionName().isBlank()) ? saved.getInspectionName() : "MES质量检验");
                dto.setSourceType("MES");
                dto.setSourceNo(saved.getWorkOrderNo());
                dto.setQuantity(BigDecimal.ONE);
                dto.setStatus(saved.getStatus());
                qmsClient.createInspection(dto);
            }
        } catch (Exception ex) {
            log.warn("sync quality inspection to qms failed, id={}", saved.getId(), ex);
        }
        return Result.success("质量检验采集创建成功", saved);
    }

    @GetMapping("/quality/{id}")
    public Result<QualityInspectionEntity> qualityDetail(@PathVariable("id") Long id) {
        QualityInspectionEntity entity = qualityInspectionRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.fail("质量检验不存在");
        }
        return Result.success("质量检验详情查询成功", entity);
    }

    @PutMapping("/quality/{id}/status")
    public Result<QualityInspectionEntity> updateQualityStatus(
            @PathVariable("id") Long id,
            @RequestBody(required = false) SimpleStatusUpdateRequest body,
            @RequestParam(value = "status", required = false) String status) {
        String nextStatus = body != null && body.getStatus() != null ? body.getStatus() : status;
        if (nextStatus == null || nextStatus.isBlank()) {
            return Result.fail("status不能为空");
        }
        QualityInspectionEntity entity = qualityInspectionRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.fail("质量检验不存在");
        }
        entity.setStatus(nextStatus);
        return Result.success("质量检验状态更新成功", qualityInspectionRepository.save(entity));
    }
}
