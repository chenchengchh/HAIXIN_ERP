package com.hxcoe.qms.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.QualityReportEntity;
import com.hxcoe.qms.service.QualityReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 质量报告管理控制器
 */
@RestController
@RequestMapping("/api/v1/qms/reports")
public class QualityReportController {

    @Autowired
    private QualityReportService qualityReportService;

    /**
     * 分页查询质量报告列表
     *
     * @param page 页码（从1开始）
     * @param size 每页数量
     * @param reportNo 报告编号（模糊）
     * @param status 状态
     * @return 分页结果
     */
    @GetMapping
    public Result<PageResult<QualityReportEntity>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String reportNo,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createdTime"));
        return qualityReportService.page(reportNo, status, pageable);
    }

    /**
     * 获取质量报告详情
     *
     * @param id 报告ID
     * @return 报告详情
     */
    @GetMapping("/{id}")
    public Result<QualityReportEntity> getById(@PathVariable Long id) {
        return qualityReportService.getById(id);
    }

    /**
     * 创建质量报告
     *
     * @param entity 报告数据
     * @return 创建结果
     */
    @PostMapping
    public Result<QualityReportEntity> create(@RequestBody QualityReportEntity entity) {
        return qualityReportService.create(entity);
    }

    /**
     * 更新质量报告
     *
     * @param id 报告ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<QualityReportEntity> update(@PathVariable Long id, @RequestBody QualityReportEntity entity) {
        return qualityReportService.update(id, entity);
    }

    /**
     * 删除质量报告
     *
     * @param id 报告ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return qualityReportService.delete(id);
    }

    /**
     * 生成质量报告
     *
     * @param body 生成参数
     * @return 生成结果
     */
    @PostMapping("/generate")
    public Result<QualityReportEntity> generate(@RequestBody Map<String, Object> body) {
        return qualityReportService.generate(body);
    }

    /**
     * 导出质量报告
     *
     * @param id 报告ID
     * @param format 导出格式
     * @return 导出文件
     */
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long id, @RequestParam(defaultValue = "pdf") String format) {
        Result<QualityReportEntity> reportResult = qualityReportService.getById(id);
        if (reportResult.getCode() != 200 || reportResult.getData() == null) {
            String msg = reportResult.getMessage() == null ? "质量报告不存在" : reportResult.getMessage();
            return ResponseEntity.status(404).contentType(MediaType.TEXT_PLAIN).body(msg.getBytes(StandardCharsets.UTF_8));
        }

        QualityReportEntity report = reportResult.getData();
        String fileName = "quality-report-" + report.getReportNo() + "." + format;
        byte[] bytes = String.valueOf(report.getContent()).getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
