package com.hxcoe.scada.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.scada.entity.ScadaReportDefEntity;
import com.hxcoe.scada.entity.ScadaTagValueEntity;
import com.hxcoe.scada.repository.ScadaReportDefRepository;
import com.hxcoe.scada.repository.ScadaTagValueRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/scada/reports")
public class ScadaReportController {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ScadaReportDefRepository repository;

    private final ScadaTagValueRepository tagValueRepository;

    public ScadaReportController(ScadaReportDefRepository repository,
                                 ScadaTagValueRepository tagValueRepository) {
        this.repository = repository;
        this.tagValueRepository = tagValueRepository;
    }

    @GetMapping
    public ApiResponse<List<ScadaReportDefEntity>> list() {
        return ApiResponse.success(repository.findAll());
    }

    @PostMapping
    public ApiResponse<ScadaReportDefEntity> create(@RequestBody ScadaReportDefEntity payload) {
        payload.setId(null);
        return ApiResponse.success("创建成功", repository.save(payload));
    }

    @PutMapping("/{id}")
    public ApiResponse<ScadaReportDefEntity> update(@PathVariable Long id, @RequestBody ScadaReportDefEntity payload) {
        if (!repository.existsById(id)) {
            return ApiResponse.error(404, "报表配置不存在");
        }
        payload.setId(id);
        return ApiResponse.success("更新成功", repository.save(payload));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ApiResponse.error(404, "报表配置不存在");
        }
        repository.deleteById(id);
        return ApiResponse.success(null);
    }

    /**
     * 生成SCADA运行报表（按点位聚合统计：样本数/平均值/最大值/最小值）
     * POST /api/v1/scada/reports/generate
     * 入参：type（班报/日报/月报）、date（报表日期）、format（excel/csv/pdf）
     *
     * @param payload 报表请求参数
     * @return 报表文件流（xlsx/csv/pdf）
     */
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generate(@RequestBody Map<String, Object> payload) {
        String type = String.valueOf(payload.getOrDefault("type", "日报"));
        String format = String.valueOf(payload.getOrDefault("format", "excel")).toLowerCase(Locale.ROOT).trim();
        LocalDate date = parseReportDate(payload.get("date"));

        // 计算报表时间范围：班报/日报按当天，月报按当月
        LocalDateTime start;
        LocalDateTime end;
        if ("月报".equals(type)) {
            start = date.withDayOfMonth(1).atStartOfDay();
            end = date.withDayOfMonth(date.lengthOfMonth()).atTime(23, 59, 59);
        } else {
            start = date.atStartOfDay();
            end = date.atTime(23, 59, 59);
        }

        List<ReportRow> rows = aggregateReportRows(start, end);
        String periodText = start.format(DATETIME_FORMATTER) + " ~ " + end.format(DATETIME_FORMATTER);
        String fileBaseName = String.format("SCADA_%s_%s", type, date);

        try {
            switch (format) {
                case "excel":
                case "xlsx":
                    return buildFileResponse(buildExcel(type, periodText, rows), fileBaseName + ".xlsx",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                case "pdf":
                    return buildFileResponse(buildPdf(type, periodText, rows), fileBaseName + ".pdf",
                            MediaType.APPLICATION_PDF_VALUE);
                case "csv":
                default:
                    return buildFileResponse(buildCsv(type, periodText, rows), fileBaseName + ".csv",
                            "text/csv;charset=UTF-8");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(("报表生成失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 解析报表日期（兼容ISO字符串/日期字符串/毫秒时间戳，缺省取当天）
     *
     * @param raw 原始日期值
     * @return 报表日期（本地时区）
     */
    private LocalDate parseReportDate(Object raw) {
        if (raw == null) {
            return LocalDate.now();
        }
        String text = String.valueOf(raw).trim();
        try {
            if (text.matches("^\\d{11,}$")) {
                return Instant.ofEpochMilli(Long.parseLong(text)).atZone(ZoneId.systemDefault()).toLocalDate();
            }
            if (text.length() >= 10) {
                // ISO带时间的字符串（如2026-08-02T00:00:00.000Z）按UTC解析后转本地日期
                if (text.contains("T")) {
                    return Instant.parse(text).atZone(ZoneId.systemDefault()).toLocalDate();
                }
                return LocalDate.parse(text.substring(0, 10));
            }
        } catch (Exception ignored) {
        }
        return LocalDate.now();
    }

    /**
     * 查询时间范围内的点位值并按点位聚合统计
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 按点位编码排序的统计行
     */
    private List<ReportRow> aggregateReportRows(LocalDateTime start, LocalDateTime end) {
        List<ScadaTagValueEntity> entities = tagValueRepository.findByTsBetweenOrderByTagCodeAscTsAsc(start, end);
        Map<String, ReportRow> rowMap = new LinkedHashMap<>();
        for (ScadaTagValueEntity entity : entities) {
            if (entity.getValue() == null) {
                continue;
            }
            ReportRow row = rowMap.computeIfAbsent(entity.getTagCode(), k -> {
                ReportRow r = new ReportRow();
                r.tagCode = k;
                r.unit = entity.getUnit() == null ? "" : entity.getUnit();
                return r;
            });
            row.count++;
            row.sum += entity.getValue();
            row.max = Math.max(row.max, entity.getValue());
            row.min = Math.min(row.min, entity.getValue());
        }
        return new ArrayList<>(rowMap.values());
    }

    /**
     * 生成Excel报表（xlsx）
     */
    private byte[] buildExcel(String type, String periodText, List<ReportRow> rows) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("SCADA" + type);
            int rowIdx = 0;

            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.createCell(0).setCellValue("SCADA系统运行" + type);
            Row periodRow = sheet.createRow(rowIdx++);
            periodRow.createCell(0).setCellValue("统计周期: " + periodText);
            Row genRow = sheet.createRow(rowIdx++);
            genRow.createCell(0).setCellValue("生成时间: " + LocalDateTime.now().format(DATETIME_FORMATTER));
            rowIdx++;

            String[] headers = {"点位编码", "单位", "样本数", "平均值", "最大值", "最小值"};
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            for (ReportRow r : rows) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.tagCode);
                row.createCell(1).setCellValue(r.unit);
                row.createCell(2).setCellValue(r.count);
                row.createCell(3).setCellValue(r.count > 0 ? round2(r.sum / r.count) : 0);
                row.createCell(4).setCellValue(r.count > 0 ? round2(r.max) : 0);
                row.createCell(5).setCellValue(r.count > 0 ? round2(r.min) : 0);
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * 生成CSV报表（带BOM，Excel可直接打开中文）
     */
    private byte[] buildCsv(String type, String periodText, List<ReportRow> rows) {
        StringBuilder sb = new StringBuilder("﻿");
        sb.append("SCADA系统运行").append(type).append("\n");
        sb.append("统计周期,").append(periodText).append("\n");
        sb.append("生成时间,").append(LocalDateTime.now().format(DATETIME_FORMATTER)).append("\n\n");
        sb.append("点位编码,单位,样本数,平均值,最大值,最小值\n");
        for (ReportRow r : rows) {
            sb.append(r.tagCode).append(',')
                    .append(r.unit).append(',')
                    .append(r.count).append(',')
                    .append(r.count > 0 ? round2(r.sum / r.count) : 0).append(',')
                    .append(r.count > 0 ? round2(r.max) : 0).append(',')
                    .append(r.count > 0 ? round2(r.min) : 0).append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * 生成PDF报表（中文采用STSong-Light内置CID字体，无需嵌入字体文件）
     */
    private byte[] buildPdf(String type, String periodText, List<ReportRow> rows) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(baseFont, 16, Font.BOLD);
            Font textFont = new Font(baseFont, 10);
            Font headerFont = new Font(baseFont, 11, Font.BOLD);

            Paragraph title = new Paragraph("SCADA系统运行" + type, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("统计周期: " + periodText, textFont));
            document.add(new Paragraph("生成时间: " + LocalDateTime.now().format(DATETIME_FORMATTER), textFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            String[] headers = {"点位编码", "单位", "样本数", "平均值", "最大值", "最小值"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
            for (ReportRow r : rows) {
                table.addCell(new Phrase(r.tagCode, textFont));
                table.addCell(new Phrase(r.unit, textFont));
                table.addCell(new Phrase(String.valueOf(r.count), textFont));
                table.addCell(new Phrase(String.valueOf(r.count > 0 ? round2(r.sum / r.count) : 0), textFont));
                table.addCell(new Phrase(String.valueOf(r.count > 0 ? round2(r.max) : 0), textFont));
                table.addCell(new Phrase(String.valueOf(r.count > 0 ? round2(r.min) : 0), textFont));
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }

    /**
     * 保留两位小数
     */
    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * 构建文件下载响应（文件名URL编码，兼容中文）
     */
    private ResponseEntity<byte[]> buildFileResponse(byte[] bytes, String filename, String contentType) {
        String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encoded + "\"");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    /**
     * 报表统计行（按点位聚合）
     */
    private static class ReportRow {
        private String tagCode;
        private String unit;
        private long count;
        private double sum;
        private double max = Double.NEGATIVE_INFINITY;
        private double min = Double.POSITIVE_INFINITY;
    }
}
