package com.hxcoe.bom.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResultAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.bom.entity.MaterialEntity;
import com.hxcoe.bom.repository.MaterialRepository;
import com.hxcoe.bom.service.MaterialService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/bom/material", "/api/v1/bom/material"})
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private MaterialRepository materialRepository;

    @Value("${bom.master-data.material-write-enabled:false}")
    private boolean materialWriteEnabled;

    @PostMapping
    public ApiResponse<MaterialEntity> createMaterial(@RequestBody MaterialEntity material) {
        return ResultAdapter.fromResult(materialService.createMaterial(material));
    }

    @PutMapping("/{id}")
    public ApiResponse<MaterialEntity> updateMaterial(@PathVariable("id") Long id, @RequestBody MaterialEntity material) {
        return ResultAdapter.fromResult(materialService.updateMaterial(id, material));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteMaterial(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(materialService.deleteMaterial(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<MaterialEntity> getMaterialById(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(materialService.getMaterialById(id));
    }

    @GetMapping("/by-code/{code}")
    public ApiResponse<MaterialEntity> getMaterialByCode(@PathVariable("code") String code) {
        return ResultAdapter.fromResult(materialService.getMaterialByCode(code));
    }

    @GetMapping
    public ApiResponse<PageResult<MaterialEntity>> getMaterials(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "spec", required = false) String spec,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size);
        return ResultAdapter.fromResult(materialService.getMaterials(code, name, spec, status, pageable));
    }

    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getMaterialConfig() {
        // 只读配置查询：前端据此禁用创建/编辑/删除/导入按钮（物料主数据由 ERP 权威维护）
        Map<String, Object> config = new HashMap<>();
        config.put("materialWriteEnabled", materialWriteEnabled);
        config.put("authorityHint", materialWriteEnabled ? "" : "物料主数据由 ERP 统一维护，BOM 侧为只读镜像");
        return ApiResponse.success(config);
    }

    @PostMapping("/import")
    public ApiResponse<Integer> importMaterials(@RequestParam(name = "file") MultipartFile file) throws IOException {
        if (!materialWriteEnabled) {
            return ApiResponse.error(403, "物料主数据权威归 ERP，BOM 禁止写入");
        }
        Workbook workbook = WorkbookFactory.create(file.getInputStream());
        Sheet sheet = workbook.getNumberOfSheets() > 0 ? workbook.getSheetAt(0) : null;
        if (sheet == null) {
            return ApiResponse.error(400, "导入文件无有效sheet");
        }

        int startRowIndex = 0;
        Row firstRow = sheet.getRow(0);
        if (firstRow != null) {
            String cell0 = getCellString(firstRow.getCell(0));
            if (cell0 != null && cell0.toLowerCase().contains("material")) {
                startRowIndex = 1;
            }
        }

        List<MaterialEntity> toSave = new ArrayList<>();
        for (int i = startRowIndex; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String materialCode = getCellString(row.getCell(0));
            String materialName = getCellString(row.getCell(1));
            if (materialCode == null || materialCode.isBlank() || materialName == null || materialName.isBlank()) {
                continue;
            }

            MaterialEntity entity = materialRepository.findFirstByMaterialCode(materialCode).orElse(new MaterialEntity());
            if (entity.getId() == null) {
                entity.setCreatedTime(LocalDateTime.now());
            } else {
                entity.setUpdatedTime(LocalDateTime.now());
            }

            entity.setMaterialCode(materialCode);
            entity.setMaterialName(materialName);
            entity.setMaterialType(getCellString(row.getCell(2)));
            entity.setUnit(getCellString(row.getCell(3)));
            entity.setMaterialSpec(getCellString(row.getCell(4)));

            String status = getCellString(row.getCell(5));
            if (status != null && !status.isBlank()) {
                entity.setStatus(status);
            }

            entity.setRemark(getCellString(row.getCell(6)));
            entity.setUnitPrice(getCellDecimal(row.getCell(7)));
            entity.setCategoryId(getCellLong(row.getCell(8)));
            entity.setCategoryName(getCellString(row.getCell(9)));
            entity.setDescription(getCellString(row.getCell(10)));
            entity.setAttrJson(getCellString(row.getCell(11)));

            toSave.add(entity);
        }

        materialRepository.saveAll(toSave);
        workbook.close();
        return ApiResponse.success(toSave.size());
    }

    @GetMapping("/export")
    public void exportMaterials(
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "name", required = false) String name,
            HttpServletResponse response) throws IOException {
        String c = code == null ? "" : code;
        String n = name == null ? "" : name;
        List<MaterialEntity> materials = materialRepository
                .findByMaterialCodeContainingAndMaterialNameContaining(c, n, Pageable.unpaged())
                .getContent();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("materials");

        Row header = sheet.createRow(0);
        String[] columns = new String[] {
                "materialCode", "materialName", "materialType", "unit", "materialSpec", "status", "remark",
                "unitPrice", "categoryId", "categoryName", "description", "attrJson"
        };
        for (int i = 0; i < columns.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(columns[i]);
        }

        int rowIndex = 1;
        for (MaterialEntity m : materials) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(nullToEmpty(m.getMaterialCode()));
            row.createCell(1).setCellValue(nullToEmpty(m.getMaterialName()));
            row.createCell(2).setCellValue(nullToEmpty(m.getMaterialType()));
            row.createCell(3).setCellValue(nullToEmpty(m.getUnit()));
            row.createCell(4).setCellValue(nullToEmpty(m.getMaterialSpec()));
            row.createCell(5).setCellValue(nullToEmpty(m.getStatus()));
            row.createCell(6).setCellValue(nullToEmpty(m.getRemark()));
            row.createCell(7).setCellValue(m.getUnitPrice() == null ? "" : m.getUnitPrice().toPlainString());
            row.createCell(8).setCellValue(m.getCategoryId() == null ? "" : String.valueOf(m.getCategoryId()));
            row.createCell(9).setCellValue(nullToEmpty(m.getCategoryName()));
            row.createCell(10).setCellValue(nullToEmpty(m.getDescription()));
            row.createCell(11).setCellValue(m.getAttrJson() == null ? "" : String.valueOf(m.getAttrJson()));
        }

        String filename = URLEncoder.encode("bom_material.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
        workbook.write(response.getOutputStream());
        workbook.close();
    }

    @GetMapping("/page")
    public ApiResponse<PageResult<MaterialEntity>> getMaterialsByPage(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        int safePage = page <= 0 ? 0 : page - 1;
        Pageable pageable = PageRequest.of(safePage, size);
        return ResultAdapter.fromResult(materialService.getMaterialsByPage(pageable));
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String getCellString(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            double v = cell.getNumericCellValue();
            long l = (long) v;
            if (Math.abs(v - l) < 0.000001d) {
                return String.valueOf(l);
            }
            return String.valueOf(v);
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        if (cell.getCellType() == CellType.FORMULA) {
            return cell.getCellFormula();
        }
        return cell.toString();
    }

    private static Long getCellLong(Cell cell) {
        String s = getCellString(cell);
        if (s == null || s.isBlank()) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal getCellDecimal(Cell cell) {
        String s = getCellString(cell);
        if (s == null || s.isBlank()) return null;
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
