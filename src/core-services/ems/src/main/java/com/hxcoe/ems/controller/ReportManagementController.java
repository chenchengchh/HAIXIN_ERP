package com.hxcoe.ems.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.ems.entity.EmsAutoGenerationTaskEntity;
import com.hxcoe.ems.entity.EmsCustomReportEntity;
import com.hxcoe.ems.entity.EmsReportExportHistoryEntity;
import com.hxcoe.ems.entity.EmsStandardReportEntity;
import com.hxcoe.ems.repository.EmsAutoGenerationTaskRepository;
import com.hxcoe.ems.repository.EmsCustomReportRepository;
import com.hxcoe.ems.repository.EmsReportExportHistoryRepository;
import com.hxcoe.ems.repository.EmsStandardReportRepository;
import com.hxcoe.ems.service.CustomReportExecutionService;
import com.hxcoe.ems.service.EmsReportService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ems/reports")
@Validated
public class ReportManagementController {

    private final EmsStandardReportRepository standardReportRepository;
    private final EmsCustomReportRepository customReportRepository;
    private final EmsAutoGenerationTaskRepository autoTaskRepository;
    private final EmsReportExportHistoryRepository exportHistoryRepository;
    private final EmsReportService reportService;
    private final CustomReportExecutionService customReportExecutionService;

    public ReportManagementController(
            EmsStandardReportRepository standardReportRepository,
            EmsCustomReportRepository customReportRepository,
            EmsAutoGenerationTaskRepository autoTaskRepository,
            EmsReportExportHistoryRepository exportHistoryRepository,
            EmsReportService reportService,
            CustomReportExecutionService customReportExecutionService
    ) {
        this.standardReportRepository = standardReportRepository;
        this.customReportRepository = customReportRepository;
        this.autoTaskRepository = autoTaskRepository;
        this.exportHistoryRepository = exportHistoryRepository;
        this.reportService = reportService;
        this.customReportExecutionService = customReportExecutionService;
    }

    @GetMapping("/standard")
    public Result<List<EmsStandardReportEntity>> getStandardReports(@RequestParam(required = false) String status) {
        if (status == null || status.isBlank()) {
            return Result.success(standardReportRepository.findAll(Sort.by(Sort.Direction.ASC, "id")));
        }
        return Result.success(standardReportRepository.findByStatus(status));
    }
    
    @PostMapping("/standard/{id}/generate")
    public Result<EmsStandardReportEntity> generateStandardReport(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> payload) {
        EmsStandardReportEntity updated = reportService.markStandardGenerated(id).orElse(null);
        if (updated == null) {
            return Result.error(404, "报表不存在");
        }
        return Result.success("生成成功", updated);
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id, @RequestParam(required = false, defaultValue = "csv") String format) {
        if (!standardReportRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = reportService.buildStandardReportFile(id, format, null);
        String safeFormat = format == null ? "csv" : format.toLowerCase(Locale.ROOT);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ems_report_" + id + "." + safeFormat + "\"");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/custom")
    public Result<Map<String, Object>> getCustomReports(
            @RequestParam(required = false) String creator,
            @RequestParam(required = false, defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(200) int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<EmsCustomReportEntity> result = (creator == null || creator.isBlank())
                ? customReportRepository.findAll(pageable)
                : customReportRepository.findByCreator(creator, pageable);

        Map<String, Object> data = new HashMap<>();
        data.put("total", result.getTotalElements());
        data.put("list", result.getContent());
        data.put("page", result.getNumber());
        data.put("size", result.getSize());
        return Result.success(data);
    }

    @PostMapping("/custom")
    public Result<EmsCustomReportEntity> createCustomReport(@RequestBody EmsCustomReportEntity payload) {
        payload.setId(null);
        EmsCustomReportEntity saved = customReportRepository.save(payload);
        return Result.success("创建成功", saved);
    }

    @PutMapping("/custom/{id}")
    public Result<EmsCustomReportEntity> updateCustomReport(@PathVariable Long id, @RequestBody EmsCustomReportEntity payload) {
        if (!customReportRepository.existsById(id)) {
            return Result.error(404, "自定义报表不存在");
        }
        payload.setId(id);
        EmsCustomReportEntity saved = customReportRepository.save(payload);
        return Result.success("更新成功", saved);
    }

    @DeleteMapping("/custom/{id}")
    public Result<Void> deleteCustomReport(@PathVariable Long id) {
        if (!customReportRepository.existsById(id)) {
            return Result.error(404, "自定义报表不存在");
        }
        customReportRepository.deleteById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/custom/{id}/execute")
    public ResponseEntity<byte[]> executeCustomReport(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> payload) {
        EmsCustomReportEntity report = customReportRepository.findById(id).orElse(null);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = customReportExecutionService.executeSelectToCsv(report.getSqlQuery(), 5000);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ems_custom_report_" + id + ".csv\"");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/auto-tasks")
    public Result<List<EmsAutoGenerationTaskEntity>> getAutoGenerationTasks() {
        return Result.success(autoTaskRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
    }

    @PostMapping("/auto-tasks")
    public Result<EmsAutoGenerationTaskEntity> createAutoTask(@RequestBody EmsAutoGenerationTaskEntity payload) {
        payload.setId(null);
        if (payload.getNextExecution() == null) {
            payload.setNextExecution(LocalDateTime.now().plusDays(1));
        }
        EmsAutoGenerationTaskEntity saved = autoTaskRepository.save(payload);
        return Result.success("创建成功", saved);
    }

    @PutMapping("/auto-tasks/{id}")
    public Result<EmsAutoGenerationTaskEntity> updateAutoTask(@PathVariable Long id, @RequestBody EmsAutoGenerationTaskEntity payload) {
        if (!autoTaskRepository.existsById(id)) {
            return Result.error(404, "任务不存在");
        }
        payload.setId(id);
        EmsAutoGenerationTaskEntity saved = autoTaskRepository.save(payload);
        return Result.success("更新成功", saved);
    }

    @PutMapping("/auto-tasks/{id}/status")
    public Result<EmsAutoGenerationTaskEntity> toggleTaskStatus(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        EmsAutoGenerationTaskEntity entity = autoTaskRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error(404, "任务不存在");
        }
        String status = payload == null ? null : String.valueOf(payload.getOrDefault("status", ""));
        if (!"enabled".equalsIgnoreCase(status) && !"disabled".equalsIgnoreCase(status)) {
            return Result.error(400, "status必须为enabled或disabled");
        }
        entity.setStatus(status.toLowerCase(Locale.ROOT));
        return Result.success("更新成功", autoTaskRepository.save(entity));
    }

    @GetMapping("/export-history")
    public Result<List<EmsReportExportHistoryEntity>> getExportHistory(
            @RequestParam(required = false) String reportName,
            @RequestParam(required = false) String status
    ) {
        if (reportName != null && !reportName.isBlank()) {
            return Result.success(exportHistoryRepository.findByReportNameContaining(reportName));
        }
        if (status != null && !status.isBlank()) {
            return Result.success(exportHistoryRepository.findByStatus(status));
        }
        return Result.success(exportHistoryRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
    }

    @PostMapping("/export")
    public Result<EmsReportExportHistoryEntity> exportReport(@RequestBody Map<String, Object> payload) {
        Long reportId = null;
        if (payload != null && payload.get("reportId") != null) {
            try {
                reportId = Long.valueOf(String.valueOf(payload.get("reportId")));
            } catch (Exception ignored) {
            }
        }
        if (reportId == null) {
            return Result.error(400, "reportId不能为空");
        }
        String format = payload == null ? "csv" : String.valueOf(payload.getOrDefault("format", "csv"));

        EmsStandardReportEntity report = standardReportRepository.findById(reportId).orElse(null);
        if (report == null) {
            return Result.error(404, "报表不存在");
        }
        String reportName = report.getName();
        byte[] bytes = reportService.buildStandardReportFile(reportId, format, null);
        EmsReportExportHistoryEntity saved = reportService.createExportHistory(reportId, reportName, format, bytes);
        return Result.success("导出成功", saved);
    }

    @GetMapping("/export-history/{id}/download")
    public ResponseEntity<byte[]> downloadFromHistory(@PathVariable Long id) {
        EmsReportExportHistoryEntity history = reportService.findExportHistory(id).orElse(null);
        if (history == null || history.getContentText() == null) {
            return ResponseEntity.notFound().build();
        }
        // Base64存储的二进制内容需解码，csv直接返回原文
        String contentText = history.getContentText();
        byte[] bytes;
        if (contentText.startsWith(EmsReportService.BASE64_PREFIX)) {
            bytes = Base64.getDecoder().decode(contentText.substring(EmsReportService.BASE64_PREFIX.length()));
        } else {
            bytes = contentText.getBytes(StandardCharsets.UTF_8);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(history.getContentType() == null ? "application/octet-stream" : history.getContentType()));
        String fileName = history.getFileName() == null ? ("ems_export_" + id + ".csv") : history.getFileName();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
