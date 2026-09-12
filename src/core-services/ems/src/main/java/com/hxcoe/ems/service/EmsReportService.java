package com.hxcoe.ems.service;

import com.hxcoe.ems.entity.EmsReportExportHistoryEntity;
import com.hxcoe.ems.entity.EmsStandardReportEntity;
import com.hxcoe.ems.entity.RealTimeDataEntity;
import com.hxcoe.ems.repository.EmsReportExportHistoryRepository;
import com.hxcoe.ems.repository.EmsStandardReportRepository;
import com.hxcoe.ems.repository.RealTimeDataRepository;
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
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class EmsReportService {

    private final EmsStandardReportRepository standardReportRepository;
    private final EmsReportExportHistoryRepository exportHistoryRepository;
    private final RealTimeDataRepository realTimeDataRepository;

    public EmsReportService(
            EmsStandardReportRepository standardReportRepository,
            EmsReportExportHistoryRepository exportHistoryRepository,
            RealTimeDataRepository realTimeDataRepository
    ) {
        this.standardReportRepository = standardReportRepository;
        this.exportHistoryRepository = exportHistoryRepository;
        this.realTimeDataRepository = realTimeDataRepository;
    }

    @PostConstruct
    public void initDefaults() {
        if (standardReportRepository.count() > 0) {
            return;
        }
        createStandardReport("能耗日报", "日报", "每日", "active");
        createStandardReport("能耗月报", "月报", "每月", "active");
        createStandardReport("能耗年报", "年报", "每年", "active");
        createStandardReport("设备能耗分析报告", "分析报告", "每月", "active");
    }

    private void createStandardReport(String name, String type, String frequency, String status) {
        EmsStandardReportEntity entity = new EmsStandardReportEntity();
        entity.setName(name);
        entity.setType(type);
        entity.setFrequency(frequency);
        entity.setStatus(status);
        entity.setLastGenerated(LocalDateTime.now());
        standardReportRepository.save(entity);
    }

    public Optional<EmsStandardReportEntity> markStandardGenerated(Long reportId) {
        EmsStandardReportEntity report = standardReportRepository.findById(reportId).orElse(null);
        if (report == null) return Optional.empty();
        report.setLastGenerated(LocalDateTime.now());
        return Optional.of(standardReportRepository.save(report));
    }

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 生成标准报表文件，支持csv/excel/pdf三种格式
     * @param reportId 报表ID
     * @param format 导出格式（csv/excel/pdf）
     * @param dateRange 日期范围
     * @returns 报表文件字节数组
     */
    public byte[] buildStandardReportFile(Long reportId, String format, List<String> dateRange) {
        String safeFormat = format == null ? "csv" : format.toLowerCase(Locale.ROOT);
        EmsStandardReportEntity report = standardReportRepository.findById(reportId).orElse(null);
        String reportName = report == null ? "standard_report" : report.getName();
        List<RealTimeDataEntity> rows = findRealTimeDataByDateRange(dateRange);
        try {
            return switch (safeFormat) {
                case "csv" -> buildCsv(reportName, rows).getBytes(StandardCharsets.UTF_8);
                case "excel", "xlsx" -> buildExcel(reportName, rows);
                case "pdf" -> buildPdf(reportName, rows);
                default -> throw new IllegalArgumentException("format仅支持csv/excel/pdf");
            };
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("报表文件生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成Excel报表（xlsx）
     */
    private byte[] buildExcel(String reportName, List<RealTimeDataEntity> rows) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("EMS报表");
            int rowIdx = 0;

            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.createCell(0).setCellValue(reportName);
            Row genRow = sheet.createRow(rowIdx++);
            genRow.createCell(0).setCellValue("生成时间: " + LocalDateTime.now().format(DATETIME_FORMATTER));
            rowIdx++;

            String[] headers = {"ID", "能源类型", "区域", "实时值", "单位", "采集时间", "状态"};
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            for (RealTimeDataEntity r : rows) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getId() == null ? 0 : r.getId());
                row.createCell(1).setCellValue(r.getEnergyType() == null ? "" : r.getEnergyType());
                row.createCell(2).setCellValue(r.getArea() == null ? "" : r.getArea());
                row.createCell(3).setCellValue(r.getActualValue() == null ? 0 : r.getActualValue());
                row.createCell(4).setCellValue(r.getUnit() == null ? "" : r.getUnit());
                row.createCell(5).setCellValue(r.getCollectionTime() == null ? "" : r.getCollectionTime().format(DATETIME_FORMATTER));
                row.createCell(6).setCellValue(r.getStatus() == null ? "" : r.getStatus());
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
     * 生成PDF报表（中文采用STSong-Light内置CID字体，无需嵌入字体文件）
     */
    private byte[] buildPdf(String reportName, List<RealTimeDataEntity> rows) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            BaseFont baseFont = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(baseFont, 16, Font.BOLD);
            Font textFont = new Font(baseFont, 9);
            Font headerFont = new Font(baseFont, 10, Font.BOLD);

            Paragraph title = new Paragraph(reportName, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("生成时间: " + LocalDateTime.now().format(DATETIME_FORMATTER), textFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            String[] headers = {"ID", "能源类型", "区域", "实时值", "单位", "采集时间", "状态"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }
            for (RealTimeDataEntity r : rows) {
                table.addCell(new Phrase(r.getId() == null ? "" : String.valueOf(r.getId()), textFont));
                table.addCell(new Phrase(r.getEnergyType() == null ? "" : r.getEnergyType(), textFont));
                table.addCell(new Phrase(r.getArea() == null ? "" : r.getArea(), textFont));
                table.addCell(new Phrase(r.getActualValue() == null ? "" : String.valueOf(r.getActualValue()), textFont));
                table.addCell(new Phrase(r.getUnit() == null ? "" : r.getUnit(), textFont));
                table.addCell(new Phrase(r.getCollectionTime() == null ? "" : r.getCollectionTime().format(DATETIME_FORMATTER), textFont));
                table.addCell(new Phrase(r.getStatus() == null ? "" : r.getStatus(), textFont));
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        }
    }

    /**
     * 创建导出历史记录：csv存原文，excel/pdf等二进制内容以Base64存储（加base64:前缀）
     */
    public EmsReportExportHistoryEntity createExportHistory(Long reportId, String reportName, String format, byte[] bytes) {
        String safeFormat = format == null ? "csv" : format.toLowerCase(Locale.ROOT);
        EmsReportExportHistoryEntity entity = new EmsReportExportHistoryEntity();
        entity.setReportId(reportId);
        entity.setReportName(reportName == null ? "" : reportName);
        entity.setFormat(safeFormat);
        entity.setExportTime(LocalDateTime.now());
        entity.setStatus("success");
        entity.setFileName(buildFileName(reportName, safeFormat));
        entity.setContentType(guessContentType(safeFormat));
        if ("csv".equals(safeFormat)) {
            entity.setContentText(new String(bytes, StandardCharsets.UTF_8));
        } else {
            entity.setContentText(BASE64_PREFIX + Base64.getEncoder().encodeToString(bytes));
        }
        return exportHistoryRepository.save(entity);
    }

    /** Base64内容前缀标记 */
    public static final String BASE64_PREFIX = "base64:";

    public Optional<EmsReportExportHistoryEntity> findExportHistory(Long id) {
        return exportHistoryRepository.findById(id);
    }

    private List<RealTimeDataEntity> findRealTimeDataByDateRange(List<String> dateRange) {
        if (dateRange == null || dateRange.isEmpty()) {
            return realTimeDataRepository.findAll();
        }
        LocalDateTime start = parseDateStart(dateRange.size() > 0 ? dateRange.get(0) : null);
        LocalDateTime end = parseDateEnd(dateRange.size() > 1 ? dateRange.get(1) : null);
        if (start == null || end == null) {
            return realTimeDataRepository.findAll();
        }
        return realTimeDataRepository.findByCollectionTimeBetween(start, end);
    }

    private LocalDateTime parseDateStart(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value).atStartOfDay();
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException("dateRange格式错误: " + value);
    }

    private LocalDateTime parseDateEnd(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDate.parse(value).atTime(23, 59, 59);
        } catch (Exception ignored) {
        }
        try {
            return LocalDateTime.parse(value);
        } catch (Exception ignored) {
        }
        throw new IllegalArgumentException("dateRange格式错误: " + value);
    }

    private String buildCsv(String reportName, List<RealTimeDataEntity> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("report,").append(escapeCsv(reportName)).append("\n");
        sb.append("id,energyType,area,actualValue,unit,collectionTime,status\n");
        for (RealTimeDataEntity row : rows) {
            sb.append(row.getId() == null ? "" : row.getId()).append(",");
            sb.append(escapeCsv(row.getEnergyType())).append(",");
            sb.append(escapeCsv(row.getArea())).append(",");
            sb.append(row.getActualValue() == null ? "" : row.getActualValue()).append(",");
            sb.append(escapeCsv(row.getUnit())).append(",");
            sb.append(row.getCollectionTime() == null ? "" : row.getCollectionTime()).append(",");
            sb.append(escapeCsv(row.getStatus())).append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\n") || v.contains("\"")) {
            return "\"" + v + "\"";
        }
        return v;
    }

    private String buildFileName(String reportName, String format) {
        String base = reportName == null || reportName.isBlank() ? "ems_report" : reportName;
        String normalized = base.replaceAll("[\\\\/:*?\"<>|]", "_");
        // excel格式统一使用xlsx扩展名
        String ext = "excel".equals(format) ? "xlsx" : (format == null ? "csv" : format);
        return normalized + "_" + LocalDate.now() + "." + ext;
    }

    private String guessContentType(String format) {
        if (format == null) return "text/csv";
        String f = format.toLowerCase(Locale.ROOT);
        return switch (f) {
            case "csv" -> "text/csv";
            case "pdf" -> "application/pdf";
            case "excel", "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default -> "application/octet-stream";
        };
    }
}
