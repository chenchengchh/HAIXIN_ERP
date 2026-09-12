package com.hxcoe.qms.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.qms.entity.DataCollectionEntity;
import com.hxcoe.qms.repository.DataCollectionRepository;
import com.hxcoe.qms.service.DataCollectionService;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 质量数据采集服务实现
 */
@Service
public class DataCollectionServiceImpl implements DataCollectionService {

    @Autowired
    private DataCollectionRepository dataCollectionRepository;

    /**
     * 分页查询质量数据采集
     *
     * @param collectionNo 采集编号（模糊）
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Result<PageResult<DataCollectionEntity>> page(String collectionNo, String dataType, LocalDate collectionDateStart, LocalDate collectionDateEnd, String status, Pageable pageable) {
        Specification<DataCollectionEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (collectionNo != null && !collectionNo.isBlank()) {
                predicates.add(cb.like(root.get("collectionNo"), "%" + collectionNo.trim() + "%"));
            }
            if (dataType != null && !dataType.isBlank()) {
                predicates.add(cb.equal(root.get("dataType"), dataType.trim()));
            }
            if (collectionDateStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("collectionDate"), collectionDateStart));
            }
            if (collectionDateEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("collectionDate"), collectionDateEnd));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<DataCollectionEntity> page = dataCollectionRepository.findAll(specification, pageable);
        PageResult<DataCollectionEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    /**
     * 获取采集详情
     *
     * @param id 采集ID
     * @return 采集详情
     */
    @Override
    public Result<DataCollectionEntity> getById(Long id) {
        DataCollectionEntity entity = dataCollectionRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("质量数据采集不存在");
        }
        return Result.success(entity);
    }

    /**
     * 创建质量数据采集
     *
     * @param entity 采集数据
     * @return 创建结果
     */
    @Override
    public Result<DataCollectionEntity> create(DataCollectionEntity entity) {
        if (entity.getCollectionNo() == null || entity.getCollectionNo().isBlank()) {
            entity.setCollectionNo("DC-" + System.currentTimeMillis());
        } else if (dataCollectionRepository.findByCollectionNo(entity.getCollectionNo().trim()).isPresent()) {
            return Result.error("采集编号已存在");
        }
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("draft");
        }
        if (entity.getCollectionDate() == null) {
            entity.setCollectionDate(LocalDate.now());
        }
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());
        DataCollectionEntity saved = dataCollectionRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 更新质量数据采集
     *
     * @param id 采集ID
     * @param entity 更新数据
     * @return 更新结果
     */
    @Override
    public Result<DataCollectionEntity> update(Long id, DataCollectionEntity entity) {
        DataCollectionEntity existing = dataCollectionRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("质量数据采集不存在");
        }
        entity.setId(id);
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedTime(LocalDateTime.now());
        DataCollectionEntity saved = dataCollectionRepository.save(entity);
        return Result.success(saved);
    }

    /**
     * 提交质量数据采集
     *
     * @param id 采集ID
     * @return 提交结果
     */
    @Override
    public Result<Void> submit(Long id) {
        DataCollectionEntity entity = dataCollectionRepository.findById(id).orElse(null);
        if (entity == null) {
            return Result.error("质量数据采集不存在");
        }
        entity.setStatus("submitted");
        entity.setUpdatedTime(LocalDateTime.now());
        dataCollectionRepository.save(entity);
        return Result.success();
    }

    /**
     * 删除质量数据采集
     *
     * @param id 采集ID
     * @return 删除结果
     */
    @Override
    public Result<Void> delete(Long id) {
        if (id == null || !dataCollectionRepository.existsById(id)) {
            return Result.error("质量数据采集不存在");
        }
        dataCollectionRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 导入质量数据
     *
     * @param file 导入文件
     * @return 导入结果
     */
    @Override
    public Result<Map<String, Object>> importData(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("导入文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        String ext = getFileExtension(fileName);

        try {
            List<Map<String, Object>> dataItems;
            List<String> headers;
            if (Objects.equals(ext, "csv")) {
                CsvParseResult parsed = parseCsv(file);
                dataItems = parsed.items;
                headers = parsed.headers;
            } else if (Objects.equals(ext, "xlsx") || Objects.equals(ext, "xls")) {
                ExcelParseResult parsed = parseExcel(file);
                dataItems = parsed.items;
                headers = parsed.headers;
            } else {
                return Result.error("不支持的文件类型：" + ext);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileName);
            result.put("size", file.getSize());
            result.put("imported", dataItems.size());
            result.put("headers", headers);
            result.put("dataItems", dataItems);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("导入解析失败：" + e.getMessage());
        }
    }

    private static String getFileExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        int idx = fileName.lastIndexOf('.');
        if (idx < 0 || idx == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(idx + 1).trim().toLowerCase();
    }

    private record CsvParseResult(List<String> headers, List<Map<String, Object>> items) {}

    private CsvParseResult parseCsv(MultipartFile file) throws Exception {
        List<String> headers = new ArrayList<>();
        List<Map<String, Object>> items = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return new CsvParseResult(headers, items);
            }
            List<String> headerCells = parseCsvLine(headerLine);
            for (int i = 0; i < headerCells.size(); i++) {
                String h = headerCells.get(i);
                if (i == 0 && h != null && !h.isEmpty() && h.charAt(0) == '\uFEFF') {
                    h = h.substring(1);
                }
                h = h == null ? "" : h.trim();
                headers.add(h.isEmpty() ? "col" + (i + 1) : h);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                List<String> cells = parseCsvLine(line);
                boolean allBlank = true;
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    String v = i < cells.size() ? cells.get(i) : "";
                    if (v != null && !v.isBlank()) {
                        allBlank = false;
                    }
                    row.put(headers.get(i), v == null ? "" : v);
                }
                if (!allBlank) {
                    items.add(row);
                }
            }
        }

        return new CsvParseResult(headers, items);
    }

    private static List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        if (line == null) {
            return out;
        }
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
                continue;
            }
            if (c == ',' && !inQuotes) {
                out.add(sb.toString().trim());
                sb.setLength(0);
                continue;
            }
            sb.append(c);
        }
        out.add(sb.toString().trim());
        return out;
    }

    private record ExcelParseResult(List<String> headers, List<Map<String, Object>> items) {}

    private ExcelParseResult parseExcel(MultipartFile file) throws Exception {
        List<String> headers = new ArrayList<>();
        List<Map<String, Object>> items = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            if (workbook.getNumberOfSheets() <= 0) {
                return new ExcelParseResult(headers, items);
            }
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                return new ExcelParseResult(headers, items);
            }
            short lastCellNum = headerRow.getLastCellNum();
            int colCount = Math.max(lastCellNum, (short) 0);
            for (int c = 0; c < colCount; c++) {
                String h = formatter.formatCellValue(headerRow.getCell(c));
                h = h == null ? "" : h.trim();
                headers.add(h.isEmpty() ? "col" + (c + 1) : h);
            }

            int firstDataRow = headerRow.getRowNum() + 1;
            for (int r = firstDataRow; r <= sheet.getLastRowNum(); r++) {
                Row rowObj = sheet.getRow(r);
                if (rowObj == null) {
                    continue;
                }
                boolean allBlank = true;
                Map<String, Object> row = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    String v = formatter.formatCellValue(rowObj.getCell(c));
                    if (v != null && !v.isBlank()) {
                        allBlank = false;
                    }
                    row.put(headers.get(c), v == null ? "" : v.trim());
                }
                if (!allBlank) {
                    items.add(row);
                }
            }
        }

        return new ExcelParseResult(headers, items);
    }
}
