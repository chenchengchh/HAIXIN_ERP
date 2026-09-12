package com.hxcoe.les.service.impl;

import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.les.dto.LesUploadResultDto;
import com.hxcoe.les.entity.LesSignVoucherEntity;
import com.hxcoe.les.repository.LesSignVoucherRepository;
import com.hxcoe.les.service.LesSignOutboxService;
import com.hxcoe.les.service.LesSignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LesSignServiceImpl implements LesSignService {

    @Autowired
    private LesSignVoucherRepository signVoucherRepository;

    @Autowired
    private LesSignOutboxService lesSignOutboxService;

    @Value("${les.sign.upload-dir:./les-upload/sign}")
    private String uploadDir;

    @Override
    public Result<PageResult<LesSignVoucherEntity>> fetchSignVouchers(int page, int size, Long planId, String status) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createTime"));
        boolean hasPlanId = planId != null;
        boolean hasStatus = status != null && !status.trim().isEmpty();
        Page<LesSignVoucherEntity> result;
        if (hasPlanId && hasStatus) {
            result = signVoucherRepository.findByPlanIdAndStatus(planId, status.trim(), pageable);
        } else if (hasPlanId) {
            result = signVoucherRepository.findByPlanId(planId, pageable);
        } else if (hasStatus) {
            result = signVoucherRepository.findByStatus(status.trim(), pageable);
        } else {
            result = signVoucherRepository.findAll(pageable);
        }
        PageResult<LesSignVoucherEntity> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, result.getContent());
        return Result.success("签收凭证查询成功", pageResult);
    }

    @Override
    public Result<LesSignVoucherEntity> getPlanSignVoucher(Long planId) {
        if (planId == null) {
            return Result.fail("planId不能为空");
        }
        LesSignVoucherEntity voucher = signVoucherRepository.findTopByPlanIdOrderByCreateTimeDesc(planId).orElse(null);
        if (voucher == null) {
            return Result.fail("签收凭证不存在");
        }
        return Result.success(voucher);
    }

    @Override
    public Result<LesSignVoucherEntity> createSignVoucher(LesSignVoucherEntity voucher) {
        if (voucher == null) {
            return Result.fail("签收凭证数据不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        voucher.setCreateTime(now);
        voucher.setUpdateTime(now);
        if (voucher.getStatus() == null || voucher.getStatus().trim().isEmpty()) {
            if (voucher.getOnTimeStatus() != null && voucher.getOnTimeStatus() == 3) {
                voucher.setStatus("exception");
            } else {
                voucher.setStatus("signed");
            }
        }
        // P2-A: 初始化 ERP/CRM 回写状态（关联了订单号才置 PENDING，否则留空）
        if (voucher.getErpOrderNo() != null && !voucher.getErpOrderNo().isBlank()) {
            voucher.setErpSyncStatus("PENDING");
        }
        if (voucher.getCrmOrderNo() != null && !voucher.getCrmOrderNo().isBlank()) {
            voucher.setCrmSyncStatus("PENDING");
        }
        LesSignVoucherEntity saved = signVoucherRepository.save(voucher);
        // P2-A: 签收凭证创建后同事务入队 ERP/CRM 回写事件
        lesSignOutboxService.enqueueSignCompletedIfAbsent(saved);
        return Result.success("签收凭证创建成功", saved);
    }

    @Override
    public Result<LesSignVoucherEntity> updateSignVoucher(Long id, LesSignVoucherEntity voucher) {
        LesSignVoucherEntity existing = signVoucherRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.fail("签收凭证不存在");
        }
        if (voucher == null) {
            return Result.fail("更新数据不能为空");
        }
        if (voucher.getPlanId() != null) {
            existing.setPlanId(voucher.getPlanId());
        }
        if (voucher.getCustomerSign() != null) {
            existing.setCustomerSign(voucher.getCustomerSign());
        }
        if (voucher.getPhotoUrls() != null) {
            existing.setPhotoUrls(voucher.getPhotoUrls());
        }
        if (voucher.getArrivalTime() != null) {
            existing.setArrivalTime(voucher.getArrivalTime());
        }
        if (voucher.getOnTimeStatus() != null) {
            existing.setOnTimeStatus(voucher.getOnTimeStatus());
        }
        if (voucher.getStatus() != null) {
            existing.setStatus(voucher.getStatus());
        }
        existing.setUpdateTime(LocalDateTime.now());
        LesSignVoucherEntity saved = signVoucherRepository.save(existing);
        return Result.success("签收凭证更新成功", saved);
    }

    @Override
    public Result<LesUploadResultDto> uploadSignImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }
        try {
            Files.createDirectories(Paths.get(uploadDir));
            String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().trim();
            String ext = "";
            int dot = originalName.lastIndexOf('.');
            if (dot >= 0 && dot < originalName.length() - 1) {
                ext = originalName.substring(dot);
            }
            String storedName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + "-" + UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = Paths.get(uploadDir).resolve(storedName);
            Files.write(target, file.getBytes());

            LesUploadResultDto dto = new LesUploadResultDto();
            dto.setFileName(storedName);
            dto.setSize(file.getSize());
            dto.setUrl("/les/sign/files/" + storedName);
            return Result.success("上传成功", dto);
        } catch (IOException e) {
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Object> getSignStats(String startDate, String endDate) {
        long total = signVoucherRepository.count();
        long signed = signVoucherRepository.countByStatus("signed");
        long pending = signVoucherRepository.countByStatus("pending");
        long exception = signVoucherRepository.countByStatus("exception");
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("signed", signed);
        stats.put("pending", pending);
        stats.put("exception", exception);
        stats.put("startDate", startDate == null ? "" : startDate);
        stats.put("endDate", endDate == null ? "" : endDate);
        return Result.success("签收统计查询成功", stats);
    }

    @Override
    public Result<PageResult<Object>> fetchSignAnomalies(int page, int size, String status) {
        String queryStatus = status == null || status.trim().isEmpty() ? "exception" : status.trim();
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createTime"));
        Page<LesSignVoucherEntity> result = signVoucherRepository.findByStatus(queryStatus, pageable);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        java.util.List<Object> list = result.getContent().stream().map(voucher -> {
            Map<String, Object> row = new HashMap<>();
            row.put("id", voucher.getId());
            row.put("exceptionNo", "EXP-" + (voucher.getPlanId() == null ? "" : voucher.getPlanId()) + "-" + voucher.getId());
            row.put("signId", voucher.getId());
            row.put("planId", voucher.getPlanId());
            boolean delayed = voucher.getOnTimeStatus() != null && voucher.getOnTimeStatus() == 3;
            row.put("exceptionType", delayed ? "延误" : "异常");
            row.put("reason", delayed ? "延误签收" : "签收异常");
            row.put("customerFeedback", "");
            row.put("createTime", voucher.getArrivalTime() == null ? "" : voucher.getArrivalTime().format(formatter));
            row.put("handleStatus", "exception".equalsIgnoreCase(voucher.getStatus()) ? "pending" : "resolved");
            return row;
        }).collect(Collectors.toList());
        PageResult<Object> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, list);
        return Result.success("签收异常查询成功", pageResult);
    }
}
