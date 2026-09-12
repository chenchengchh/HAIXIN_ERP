package com.hxcoe.les.controller;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.les.dto.LesUploadResultDto;
import com.hxcoe.les.entity.LesSignVoucherEntity;
import com.hxcoe.les.service.LesSignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping({"/api/v1/les/sign", "/les/sign"})
public class LesSignController {

    @Autowired
    private LesSignService signService;

    @Value("${les.sign.upload-dir:./les-upload/sign}")
    private String uploadDir;

    /**
     * 获取签收凭证列表
     */
    @GetMapping("/vouchers")
    public Result<PageResult<LesSignVoucherEntity>> fetchSignVouchers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "planId", required = false) Long planId,
            @RequestParam(value = "status", required = false) String status
    ) {
        return signService.fetchSignVouchers(page, size, planId, status);
    }

    /**
     * 获取运输计划的签收凭证
     */
    @GetMapping("/plans/{planId}/voucher")
    public Result<LesSignVoucherEntity> getPlanSignVoucher(@PathVariable("planId") Long planId) {
        return signService.getPlanSignVoucher(planId);
    }

    /**
     * 创建签收凭证
     */
    @PostMapping("/vouchers")
    public Result<LesSignVoucherEntity> createSignVoucher(@RequestBody LesSignVoucherEntity voucher) {
        return signService.createSignVoucher(voucher);
    }

    /**
     * 更新签收凭证
     */
    @PutMapping("/vouchers/{id}")
    public Result<LesSignVoucherEntity> updateSignVoucher(@PathVariable("id") Long id, @RequestBody LesSignVoucherEntity voucher) {
        return signService.updateSignVoucher(id, voucher);
    }

    /**
     * 上传签收图片
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<LesUploadResultDto> uploadSignImage(@RequestPart("file") MultipartFile file) {
        return signService.uploadSignImage(file);
    }

    /**
     * 获取签收统计数据
     */
    @GetMapping("/stats")
    public Result<Object> getSignStats(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate
    ) {
        return signService.getSignStats(startDate, endDate);
    }

    /**
     * 获取签收异常列表
     */
    @GetMapping("/anomalies")
    public Result<PageResult<Object>> fetchSignAnomalies(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status
    ) {
        return signService.fetchSignAnomalies(page, size, status);
    }

    /**
     * 下载/预览已上传的签收文件
     */
    @GetMapping("/files/{fileName}")
    public ResponseEntity<Resource> getUploadedFile(@PathVariable("fileName") String fileName) throws MalformedURLException {
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path target = base.resolve(fileName).normalize();
        if (!target.startsWith(base)) {
            return ResponseEntity.badRequest().build();
        }
        Resource resource = new UrlResource(target.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}

