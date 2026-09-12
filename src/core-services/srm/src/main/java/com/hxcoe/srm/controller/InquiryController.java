package com.hxcoe.srm.controller;

import com.hxcoe.srm.entity.InquiryEntity;
import com.hxcoe.srm.service.InquiryService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/srm")
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;

    @PostMapping("/inquiries")
    public Result<InquiryEntity> createInquiry(@RequestBody InquiryEntity inquiry) {
        InquiryEntity result = inquiryService.createInquiry(inquiry);
        return Result.success("询价单创建成功", result);
    }

    @GetMapping("/inquiries")
    public Result<PageResult<InquiryEntity>> getInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<InquiryEntity> result = inquiryService.getInquiries(pageable);
        PageResult<InquiryEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        );
        return Result.success("询价单列表查询成功", pageResult);
    }

    @GetMapping("/inquiries/{id}")
    public Result<InquiryEntity> getInquiryById(@PathVariable Long id) {
        Optional<InquiryEntity> result = inquiryService.getInquiryById(id);
        return result.map(inquiryEntity -> Result.success("询价单查询成功", inquiryEntity)).orElseGet(() -> Result.fail("询价单不存在"));
    }

    @PutMapping("/inquiries/{id}")
    public Result<InquiryEntity> updateInquiry(@PathVariable Long id, @RequestBody InquiryEntity inquiry) {
        InquiryEntity result = inquiryService.updateInquiry(id, inquiry);
        if (result != null) {
            return Result.success("询价单更新成功", result);
        } else {
            return Result.fail("询价单不存在");
        }
    }

    @DeleteMapping("/inquiries/{id}")
    public Result<String> deleteInquiry(@PathVariable Long id) {
        inquiryService.deleteInquiry(id);
        return Result.success("询价单删除成功");
    }

    @PutMapping("/inquiries/{id}/publish")
    public Result<InquiryEntity> publishInquiry(@PathVariable Long id) {
        InquiryEntity result = inquiryService.publishInquiry(id);
        if (result != null) {
            return Result.success("询价单发布成功", result);
        } else {
            return Result.fail("询价单不存在");
        }
    }

    @PutMapping("/inquiries/{id}/close")
    public Result<InquiryEntity> closeInquiry(@PathVariable Long id) {
        InquiryEntity result = inquiryService.closeInquiry(id);
        if (result != null) {
            return Result.success("询价单关闭成功", result);
        } else {
            return Result.fail("询价单不存在");
        }
    }
}
