package com.hxcoe.plm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResultAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.plm.entity.PlmDocumentEntity;
import com.hxcoe.plm.service.PlmDocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping({"/plm/documents", "/api/v1/plm/documents"})
public class PlmDocumentController {

    @Autowired
    private PlmDocumentService documentService;

    @GetMapping("/page")
    public ApiResponse<PageResult<PlmDocumentEntity>> page(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "type", required = false) String type) {
        int safePage = page <= 0 ? 0 : page - 1;
        Pageable pageable = PageRequest.of(safePage, Math.max(size, 1));
        return ResultAdapter.fromResult(documentService.page(pageable, keyword, type));
    }

    @GetMapping("/{id}")
    public ApiResponse<PlmDocumentEntity> get(@PathVariable("id") Long id) {
        Result<PlmDocumentEntity> result = documentService.getById(id)
                .map(Result::success)
                .orElseGet(() -> Result.error("文档不存在"));
        return ResultAdapter.fromResult(result);
    }

    @PostMapping
    public ApiResponse<PlmDocumentEntity> create(@RequestBody PlmDocumentEntity entity) {
        return ResultAdapter.fromResult(documentService.create(entity));
    }

    @PutMapping("/{id}")
    public ApiResponse<PlmDocumentEntity> update(@PathVariable("id") Long id, @RequestBody PlmDocumentEntity entity) {
        return ResultAdapter.fromResult(documentService.update(id, entity));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") Long id) {
        return ResultAdapter.fromResult(documentService.delete(id));
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PlmDocumentEntity> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String docCode,
            @RequestParam String title,
            @RequestParam(required = false) String docType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String remark) {
        PlmDocumentEntity entity = new PlmDocumentEntity();
        entity.setDocCode(docCode);
        entity.setTitle(title);
        entity.setDocType(docType);
        entity.setCategory(category);
        entity.setVersion(version);
        entity.setStatus(status);
        entity.setAuthor(author);
        entity.setRemark(remark);
        return ResultAdapter.fromResult(documentService.upload(file, entity));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable("id") Long id) {
        PlmDocumentEntity entity = documentService.getById(id).orElse(null);
        if (entity == null || entity.getFilePath() == null || entity.getFilePath().trim().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(entity.getFilePath());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String filename = entity.getFileName() == null || entity.getFileName().isEmpty() ? "document" : entity.getFileName();
        String encoded = java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
