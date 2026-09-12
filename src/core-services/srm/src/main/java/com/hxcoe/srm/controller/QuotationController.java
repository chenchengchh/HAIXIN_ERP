package com.hxcoe.srm.controller;

import com.hxcoe.srm.entity.QuotationEntity;
import com.hxcoe.srm.service.QuotationService;
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
public class QuotationController {

    @Autowired
    private QuotationService quotationService;

    @PostMapping("/quotations")
    public Result<QuotationEntity> createQuotation(@RequestBody QuotationEntity quotation) {
        QuotationEntity result = quotationService.createQuotation(quotation);
        return Result.success("报价单创建成功", result);
    }

    @GetMapping("/quotations")
    public Result<PageResult<QuotationEntity>> getQuotations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<QuotationEntity> result = quotationService.getQuotations(pageable);
        PageResult<QuotationEntity> pageResult = PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        );
        return Result.success("报价单列表查询成功", pageResult);
    }

    @GetMapping("/quotations/{id}")
    public Result<QuotationEntity> getQuotationById(@PathVariable Long id) {
        Optional<QuotationEntity> result = quotationService.getQuotationById(id);
        return result.map(quotationEntity -> Result.success("报价单查询成功", quotationEntity)).orElseGet(() -> Result.fail("报价单不存在"));
    }

    @PutMapping("/quotations/{id}")
    public Result<QuotationEntity> updateQuotation(@PathVariable Long id, @RequestBody QuotationEntity quotation) {
        QuotationEntity result = quotationService.updateQuotation(id, quotation);
        if (result != null) {
            return Result.success("报价单更新成功", result);
        } else {
            return Result.fail("报价单不存在");
        }
    }

    @DeleteMapping("/quotations/{id}")
    public Result<String> deleteQuotation(@PathVariable Long id) {
        quotationService.deleteQuotation(id);
        return Result.success("报价单删除成功");
    }

    @PutMapping("/quotations/{id}/accept")
    public Result<QuotationEntity> acceptQuotation(@PathVariable Long id) {
        QuotationEntity result = quotationService.acceptQuotation(id);
        if (result != null) {
            return Result.success("报价单接受成功", result);
        } else {
            return Result.fail("报价单不存在");
        }
    }

    @PutMapping("/quotations/{id}/reject")
    public Result<QuotationEntity> rejectQuotation(@PathVariable Long id) {
        QuotationEntity result = quotationService.rejectQuotation(id);
        if (result != null) {
            return Result.success("报价单拒绝成功", result);
        } else {
            return Result.fail("报价单不存在");
        }
    }
}
