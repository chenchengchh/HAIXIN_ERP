package com.hxcoe.plm.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.plm.entity.PlmDocumentEntity;
import com.hxcoe.plm.repository.PlmDocumentRepository;
import com.hxcoe.plm.service.PlmDocumentService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
public class PlmDocumentServiceImpl implements PlmDocumentService {

    @Autowired
    private PlmDocumentRepository repository;

    @Value("${plm.file.storage-path:/data/plm/files}")
    private String storagePath;

    @Override
    public Result<PageResult<PlmDocumentEntity>> page(Pageable pageable, String keyword, String docType) {
        Specification<PlmDocumentEntity> spec = (root, query, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.trim() + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("docCode"), like)
                ));
            }
            if (docType != null && !docType.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("docType"), docType.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<PlmDocumentEntity> page = repository.findAll(spec, pageable);
        PageResult<PlmDocumentEntity> pageResult = PageResult.build(page.getTotalElements(), page.getSize(), page.getNumber() + 1, page.getContent());
        return Result.success(pageResult);
    }

    @Override
    public Result<PlmDocumentEntity> create(PlmDocumentEntity entity) {
        if (entity.getTitle() == null || entity.getTitle().trim().isEmpty()) {
            return Result.error("文档标题不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        entity.setTitle(entity.getTitle().trim());
        entity.setCreatedTime(now);
        entity.setUpdatedTime(now);
        PlmDocumentEntity saved = repository.save(entity);
        return Result.success(saved);
    }

    @Override
    public Result<PlmDocumentEntity> update(Long id, PlmDocumentEntity entity) {
        PlmDocumentEntity existing = repository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("文档不存在");
        }
        entity.setId(id);
        entity.setCreatedTime(existing.getCreatedTime());
        entity.setUpdatedTime(LocalDateTime.now());
        PlmDocumentEntity saved = repository.save(entity);
        return Result.success(saved);
    }

    @Override
    public Result<Void> delete(Long id) {
        if (!repository.existsById(id)) {
            return Result.error("文档不存在");
        }
        repository.deleteById(id);
        return Result.success();
    }

    @Override
    public Result<PlmDocumentEntity> upload(MultipartFile file, PlmDocumentEntity entity) {
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }
        if (entity.getTitle() == null || entity.getTitle().trim().isEmpty()) {
            return Result.error("文档标题不能为空");
        }

        try {
            File dir = new File(storagePath);
            if (!dir.exists() && !dir.mkdirs()) {
                return Result.error("文件存储目录不可用");
            }

            String original = file.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf("."));
            }
            String storedName = UUID.randomUUID().toString().replace("-", "") + ext;
            File target = new File(dir, storedName);
            file.transferTo(target);

            LocalDateTime now = LocalDateTime.now();
            entity.setTitle(entity.getTitle().trim());
            entity.setFileName(original);
            entity.setFilePath(new File(dir, storedName).getAbsolutePath().replace("\\", "/"));
            entity.setFileFormat(ext.startsWith(".") ? ext.substring(1) : ext);
            entity.setFileSize(String.valueOf(file.getSize()));
            entity.setCreatedTime(now);
            entity.setUpdatedTime(now);

            PlmDocumentEntity saved = repository.save(entity);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error("上传失败");
        }
    }

    @Override
    public Optional<PlmDocumentEntity> getById(Long id) {
        return repository.findById(id);
    }
}

