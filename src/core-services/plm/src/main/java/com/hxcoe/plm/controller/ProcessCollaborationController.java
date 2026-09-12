package com.hxcoe.plm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.plm.entity.ChangeRequestEntity;
import com.hxcoe.plm.entity.ProcessFileEntity;
import com.hxcoe.plm.entity.ProcessRouteEntity;
import com.hxcoe.plm.repository.ChangeRequestRepository;
import com.hxcoe.plm.repository.ProcessFileRepository;
import com.hxcoe.plm.repository.ProcessRouteRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;

@RestController
@RequestMapping({"/plm/process", "/api/v1/plm/process"})
public class ProcessCollaborationController {

    @Autowired
    private ProcessRouteRepository processRouteRepository;

    @Autowired
    private ProcessFileRepository processFileRepository;

    @Autowired
    private ChangeRequestRepository changeRequestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${plm.file.storage-path:/data/plm/files}")
    private String fileStoragePath;

    /**
     * 分页获取工艺路线列表。
     */
    @GetMapping("/routes")
    public ApiResponse<PageResult<Map<String, Object>>> getProcessRoutes(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "type", required = false) String type) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));

        Specification<ProcessRouteEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeValue = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("routeName"), likeValue),
                        cb.like(root.get("routeCode"), likeValue)
                ));
            }
            if (type != null && !type.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("routeType"), type.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProcessRouteEntity> result = processRouteRepository.findAll(spec, pageable);
        List<Map<String, Object>> records = result.getContent().stream().map(this::toProcessRouteMap).toList();
        PageResult<Map<String, Object>> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, records);
        return ApiResponse.success(pageResult);
    }

    /**
     * 创建工艺路线。
     */
    @PostMapping("/routes")
    public ApiResponse<Map<String, Object>> createProcessRoute(@RequestBody Map<String, Object> body) {
        String name = asString(body.get("name"));
        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error(400, "工艺路线名称不能为空");
        }

        ProcessRouteEntity entity = new ProcessRouteEntity();
        entity.setRouteCode(asString(body.get("code")));
        entity.setRouteName(name.trim());
        entity.setProductName(asString(body.get("productName")));
        entity.setProductCode(asString(body.get("productCode")));
        entity.setVersion(asString(body.get("version")));
        entity.setRouteType(asString(body.get("type")));
        entity.setStatus(defaultIfBlank(asString(body.get("status")), "draft"));
        entity.setCreateUser(defaultIfBlank(asString(body.get("createUser")), "当前用户"));
        entity.setStepsJson(toJson(body.get("steps")));
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        ProcessRouteEntity saved = processRouteRepository.save(entity);
        return ApiResponse.success(toProcessRouteMap(saved));
    }

    /**
     * 更新工艺路线。
     */
    @PutMapping("/routes/{id}")
    public ApiResponse<Map<String, Object>> updateProcessRoute(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        ProcessRouteEntity entity = processRouteRepository.findById(id).orElse(null);
        if (entity == null) {
            return ApiResponse.error(404, "工艺路线不存在");
        }

        if (body.containsKey("code")) entity.setRouteCode(asString(body.get("code")));
        if (body.containsKey("name")) entity.setRouteName(asString(body.get("name")));
        if (body.containsKey("productName")) entity.setProductName(asString(body.get("productName")));
        if (body.containsKey("productCode")) entity.setProductCode(asString(body.get("productCode")));
        if (body.containsKey("version")) entity.setVersion(asString(body.get("version")));
        if (body.containsKey("type")) entity.setRouteType(asString(body.get("type")));
        if (body.containsKey("status")) entity.setStatus(asString(body.get("status")));
        if (body.containsKey("createUser")) entity.setCreateUser(asString(body.get("createUser")));
        if (body.containsKey("steps")) entity.setStepsJson(toJson(body.get("steps")));
        entity.setUpdatedTime(LocalDateTime.now());

        ProcessRouteEntity saved = processRouteRepository.save(entity);
        return ApiResponse.success(toProcessRouteMap(saved));
    }

    /**
     * 删除工艺路线。
     */
    @DeleteMapping("/routes/{id}")
    public ApiResponse<Void> deleteProcessRoute(@PathVariable("id") Long id) {
        if (!processRouteRepository.existsById(id)) {
            return ApiResponse.error(404, "工艺路线不存在");
        }
        processRouteRepository.deleteById(id);
        return ApiResponse.success(null);
    }

    /**
     * 分页获取工艺文件列表。
     */
    @GetMapping("/files")
    public ApiResponse<PageResult<Map<String, Object>>> getProcessFiles(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "type", required = false) String type) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));

        Specification<ProcessFileEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeValue = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), likeValue),
                        cb.like(root.get("fileCode"), likeValue)
                ));
            }
            if (type != null && !type.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("fileType"), type.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ProcessFileEntity> result = processFileRepository.findAll(spec, pageable);
        List<Map<String, Object>> records = result.getContent().stream().map(this::toProcessFileMap).toList();
        PageResult<Map<String, Object>> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, records);
        return ApiResponse.success(pageResult);
    }

    /**
     * 创建工艺文件（元数据）。
     */
    @PostMapping("/files")
    public ApiResponse<Map<String, Object>> createProcessFile(@RequestBody Map<String, Object> body) {
        String title = asString(body.get("title"));
        if (title == null || title.trim().isEmpty()) {
            return ApiResponse.error(400, "文件标题不能为空");
        }

        ProcessFileEntity entity = new ProcessFileEntity();
        entity.setFileCode(asString(body.get("code")));
        entity.setTitle(title.trim());
        entity.setFileType(asString(body.get("type")));
        entity.setCategory(asString(body.get("category")));
        entity.setVersion(asString(body.get("version")));
        entity.setStatus(defaultIfBlank(asString(body.get("status")), "active"));
        entity.setAuthor(defaultIfBlank(asString(body.get("author")), "当前用户"));
        entity.setFileSize(asString(body.get("fileSize")));
        entity.setFileFormat(asString(body.get("fileType")));
        entity.setFilePath(asString(body.get("filePath")));
        entity.setRemark(asString(body.get("remark")));
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        ProcessFileEntity saved = processFileRepository.save(entity);
        return ApiResponse.success(toProcessFileMap(saved));
    }

    /**
     * 上传工艺文件（二进制）并落库元数据。
     */
    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, Object>> uploadProcessFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "version", required = false) String version,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "remark", required = false) String remark) {
        if (file == null || file.isEmpty()) {
            return ApiResponse.error(400, "文件不能为空");
        }
        if (title == null || title.trim().isEmpty()) {
            return ApiResponse.error(400, "文件标题不能为空");
        }

        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0 && dot < originalName.length() - 1) {
            ext = "." + originalName.substring(dot + 1);
        }

        String savedFilename = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dir = Paths.get(fileStoragePath, "process_files");
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(savedFilename);
            file.transferTo(target.toFile());

            ProcessFileEntity entity = new ProcessFileEntity();
            entity.setFileCode(code);
            entity.setTitle(title.trim());
            entity.setFileType(type);
            entity.setCategory(category);
            entity.setVersion(version);
            entity.setStatus(defaultIfBlank(status, "draft"));
            entity.setAuthor(defaultIfBlank(author, "当前用户"));
            entity.setFileSize(file.getSize() <= 0 ? null : String.valueOf(file.getSize()));
            entity.setFileFormat(ext.isEmpty() ? null : ext.substring(1));
            entity.setFilePath(target.toString());
            entity.setRemark(remark);
            entity.setCreatedTime(LocalDateTime.now());
            entity.setUpdatedTime(LocalDateTime.now());
            ProcessFileEntity saved = processFileRepository.save(entity);
            return ApiResponse.success(toProcessFileMap(saved));
        } catch (Exception e) {
            return ApiResponse.error(500, "文件上传失败");
        }
    }

    /**
     * 更新工艺文件（元数据）。
     */
    @PutMapping("/files/{id}")
    public ApiResponse<Map<String, Object>> updateProcessFile(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        ProcessFileEntity entity = processFileRepository.findById(id).orElse(null);
        if (entity == null) {
            return ApiResponse.error(404, "工艺文件不存在");
        }

        if (body.containsKey("code")) entity.setFileCode(asString(body.get("code")));
        if (body.containsKey("title")) entity.setTitle(asString(body.get("title")));
        if (body.containsKey("type")) entity.setFileType(asString(body.get("type")));
        if (body.containsKey("category")) entity.setCategory(asString(body.get("category")));
        if (body.containsKey("version")) entity.setVersion(asString(body.get("version")));
        if (body.containsKey("status")) entity.setStatus(asString(body.get("status")));
        if (body.containsKey("author")) entity.setAuthor(asString(body.get("author")));
        if (body.containsKey("fileSize")) entity.setFileSize(asString(body.get("fileSize")));
        if (body.containsKey("fileType")) entity.setFileFormat(asString(body.get("fileType")));
        if (body.containsKey("filePath")) entity.setFilePath(asString(body.get("filePath")));
        if (body.containsKey("remark")) entity.setRemark(asString(body.get("remark")));
        entity.setUpdatedTime(LocalDateTime.now());

        ProcessFileEntity saved = processFileRepository.save(entity);
        return ApiResponse.success(toProcessFileMap(saved));
    }

    @GetMapping("/files/{id}/download")
    public ResponseEntity<Resource> downloadProcessFile(@PathVariable("id") Long id) {
        ProcessFileEntity entity = processFileRepository.findById(id).orElse(null);
        if (entity == null || entity.getFilePath() == null || entity.getFilePath().trim().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        FileSystemResource resource = new FileSystemResource(entity.getFilePath());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String filename = entity.getTitle() == null || entity.getTitle().isEmpty() ? "process_file" : entity.getTitle();
        String ext = entity.getFileFormat() == null || entity.getFileFormat().isEmpty() ? "" : "." + entity.getFileFormat();
        String encoded = java.net.URLEncoder.encode(filename + ext, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * 删除工艺文件。
     */
    @DeleteMapping("/files/{id}")
    public ApiResponse<Void> deleteProcessFile(@PathVariable("id") Long id) {
        if (!processFileRepository.existsById(id)) {
            return ApiResponse.error(404, "工艺文件不存在");
        }
        processFileRepository.deleteById(id);
        return ApiResponse.success(null);
    }

    /**
     * 分页获取工艺变更列表。
     */
    @GetMapping("/changes")
    public ApiResponse<PageResult<Map<String, Object>>> getProcessChanges(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "status", required = false) String status) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));

        Specification<ChangeRequestEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeValue = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), likeValue),
                        cb.like(root.get("changeCode"), likeValue),
                        cb.like(root.get("processCode"), likeValue)
                ));
            }
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ChangeRequestEntity> result = changeRequestRepository.findAll(spec, pageable);
        List<Map<String, Object>> records = result.getContent().stream().map(this::toChangeRequestMap).toList();
        PageResult<Map<String, Object>> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, records);
        return ApiResponse.success(pageResult);
    }

    /**
     * 创建工艺变更。
     */
    @PostMapping("/changes")
    public ApiResponse<Map<String, Object>> createProcessChange(@RequestBody Map<String, Object> body) {
        String title = asString(body.get("title"));
        if (title == null || title.trim().isEmpty()) {
            return ApiResponse.error(400, "变更标题不能为空");
        }

        ChangeRequestEntity entity = new ChangeRequestEntity();
        entity.setProductId(0L);
        entity.setChangeCode(asString(body.get("code")));
        entity.setTitle(title.trim());
        entity.setChangeType(asString(body.get("type")));
        entity.setProcessCode(asString(body.get("processCode")));
        entity.setProcessName(asString(body.get("processName")));
        entity.setReason(asString(body.get("reason")));
        entity.setContent(asString(body.get("content")));
        entity.setStatus(defaultIfBlank(asString(body.get("status")), "in-progress"));
        entity.setApplyUser(defaultIfBlank(asString(body.get("applyUser")), "当前用户"));
        entity.setApplyTime(parseLocalDate(asString(body.get("applyTime"))));
        entity.setApproveUser(asString(body.get("approveUser")));
        entity.setApproveTime(parseLocalDate(asString(body.get("approveTime"))));
        entity.setImplementTime(parseLocalDate(asString(body.get("implementTime"))));
        entity.setCreatedBy(entity.getApplyUser());
        entity.setCreatedTime(LocalDateTime.now());
        entity.setUpdatedTime(LocalDateTime.now());

        ChangeRequestEntity saved = changeRequestRepository.save(entity);
        return ApiResponse.success(toChangeRequestMap(saved));
    }

    /**
     * 更新工艺变更。
     */
    @PutMapping("/changes/{id}")
    public ApiResponse<Map<String, Object>> updateProcessChange(@PathVariable("id") Long id, @RequestBody Map<String, Object> body) {
        ChangeRequestEntity entity = changeRequestRepository.findById(id).orElse(null);
        if (entity == null) {
            return ApiResponse.error(404, "工艺变更不存在");
        }

        if (body.containsKey("code")) entity.setChangeCode(asString(body.get("code")));
        if (body.containsKey("title")) entity.setTitle(asString(body.get("title")));
        if (body.containsKey("type")) entity.setChangeType(asString(body.get("type")));
        if (body.containsKey("processCode")) entity.setProcessCode(asString(body.get("processCode")));
        if (body.containsKey("processName")) entity.setProcessName(asString(body.get("processName")));
        if (body.containsKey("reason")) entity.setReason(asString(body.get("reason")));
        if (body.containsKey("content")) entity.setContent(asString(body.get("content")));
        if (body.containsKey("status")) entity.setStatus(asString(body.get("status")));
        if (body.containsKey("applyUser")) entity.setApplyUser(asString(body.get("applyUser")));
        if (body.containsKey("applyTime")) entity.setApplyTime(parseLocalDate(asString(body.get("applyTime"))));
        if (body.containsKey("approveUser")) entity.setApproveUser(asString(body.get("approveUser")));
        if (body.containsKey("approveTime")) entity.setApproveTime(parseLocalDate(asString(body.get("approveTime"))));
        if (body.containsKey("implementTime")) entity.setImplementTime(parseLocalDate(asString(body.get("implementTime"))));
        entity.setUpdatedTime(LocalDateTime.now());

        ChangeRequestEntity saved = changeRequestRepository.save(entity);
        return ApiResponse.success(toChangeRequestMap(saved));
    }

    /**
     * 删除工艺变更。
     */
    @DeleteMapping("/changes/{id}")
    public ApiResponse<Void> deleteProcessChange(@PathVariable("id") Long id) {
        if (!changeRequestRepository.existsById(id)) {
            return ApiResponse.error(404, "工艺变更不存在");
        }
        changeRequestRepository.deleteById(id);
        return ApiResponse.success(null);
    }

    private Map<String, Object> toProcessRouteMap(ProcessRouteEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId() == null ? null : String.valueOf(entity.getId()));
        map.put("code", entity.getRouteCode());
        map.put("name", entity.getRouteName());
        map.put("status", defaultIfBlank(entity.getStatus(), "draft"));
        map.put("productName", entity.getProductName());
        map.put("productCode", entity.getProductCode());
        map.put("version", entity.getVersion());
        map.put("type", entity.getRouteType());
        map.put("createUser", entity.getCreateUser());
        map.put("steps", parseJsonArray(entity.getStepsJson()));
        map.put("createTime", entity.getCreatedTime() == null ? null : entity.getCreatedTime().toLocalDate().toString());
        return map;
    }

    private Map<String, Object> toProcessFileMap(ProcessFileEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId() == null ? null : String.valueOf(entity.getId()));
        map.put("code", entity.getFileCode());
        map.put("title", entity.getTitle());
        map.put("type", entity.getFileType());
        map.put("category", entity.getCategory());
        map.put("version", entity.getVersion());
        map.put("status", entity.getStatus());
        map.put("author", entity.getAuthor());
        map.put("createTime", entity.getCreatedTime() == null ? null : entity.getCreatedTime().toLocalDate().toString());
        map.put("updateTime", entity.getUpdatedTime() == null ? null : entity.getUpdatedTime().toLocalDate().toString());
        map.put("fileSize", entity.getFileSize());
        map.put("fileType", entity.getFileFormat());
        map.put("filePath", entity.getFilePath());
        map.put("remark", entity.getRemark());
        return map;
    }

    private Map<String, Object> toChangeRequestMap(ChangeRequestEntity entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", entity.getId() == null ? null : String.valueOf(entity.getId()));
        map.put("code", entity.getChangeCode());
        map.put("title", entity.getTitle());
        map.put("type", entity.getChangeType());
        map.put("processCode", entity.getProcessCode());
        map.put("processName", entity.getProcessName());
        map.put("reason", entity.getReason());
        map.put("content", entity.getContent());
        map.put("status", entity.getStatus());
        map.put("applyUser", entity.getApplyUser());
        map.put("applyTime", entity.getApplyTime() == null ? null : entity.getApplyTime().toString());
        map.put("approveUser", entity.getApproveUser());
        map.put("approveTime", entity.getApproveTime() == null ? null : entity.getApproveTime().toString());
        map.put("implementTime", entity.getImplementTime() == null ? null : entity.getImplementTime().toString());
        return map;
    }

    private String asString(Object value) {
        if (value == null) return null;
        String text = String.valueOf(value);
        return text.isEmpty() ? null : text;
    }

    private String defaultIfBlank(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) return defaultValue;
        return value;
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<Object> parseJsonArray(String json) {
        if (json == null || json.trim().isEmpty()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Object>>() {});
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }
}
