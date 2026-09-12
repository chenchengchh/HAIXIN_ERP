package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.wms.entity.AsnEntity;
import com.hxcoe.wms.service.AsnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping({"/api/v1/wms/inbound/asn", "/wms/inbound/asn", "/api/wms/inbound/asn"})
public class InboundController {

    @Autowired
    private AsnService asnService;

    @PostMapping
    public ApiResponse<AsnEntity> createAsn(@RequestBody AsnEntity asn) {
        AsnEntity result = asnService.createAsn(asn);
        return success("ASN创建成功", result);
    }

    @GetMapping
    public ApiResponse<PageResult<AsnEntity>> getAsns(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String asnNo,
            @RequestParam(required = false) String deliveryNoteNo,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1));
        Page<AsnEntity> result = asnService.getAsns(pageable, asnNo, deliveryNoteNo, supplierName, warehouseCode, status, startTime, endTime);
        return success("ASN列表查询成功", PageResult.build(
                result.getTotalElements(),
                result.getSize(),
                result.getNumber() + 1,
                result.getContent()
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<AsnEntity> getAsnById(@PathVariable Long id) {
        Optional<AsnEntity> result = asnService.getAsnById(id);
        return result.map(entity -> success("ASN查询成功", entity)).orElseGet(() -> notFound("ASN不存在"));
    }

    @PutMapping("/{id}")
    public ApiResponse<AsnEntity> updateAsn(@PathVariable Long id, @RequestBody AsnEntity asn) {
        AsnEntity result = asnService.updateAsn(id, asn);
        if (result == null) {
            return notFound("ASN不存在");
        }
        return success("ASN更新成功", result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAsn(@PathVariable Long id) {
        boolean ok = asnService.deleteAsn(id);
        if (!ok) {
            return notFound("ASN不存在");
        }
        return success("ASN删除成功", null);
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<AsnEntity> receiveAsn(@PathVariable Long id) {
        AsnEntity result = asnService.receiveAsn(id);
        if (result != null) {
            return success("ASN收货成功", result);
        } else {
            return notFound("ASN不存在");
        }
    }

    @PostMapping("/{id}/start")
    public ApiResponse<AsnEntity> startAsn(@PathVariable Long id) {
        AsnEntity result = asnService.startAsn(id);
        if (result == null) {
            return notFound("ASN不存在");
        }
        return success("ASN开始收货成功", result);
    }

    @PostMapping("/{id}/scan")
    public ApiResponse<AsnEntity> scanAsn(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        String barcode = body != null ? String.valueOf(body.getOrDefault("barcode", "")) : "";
        java.math.BigDecimal quantity = null;
        if (body != null && body.get("quantity") != null) {
            try {
                quantity = new java.math.BigDecimal(String.valueOf(body.get("quantity")));
            } catch (Exception ignored) {
            }
        }
        if (barcode == null || barcode.isBlank()) {
            return badRequest("barcode不能为空");
        }
        AsnEntity result = asnService.scanAsn(id, barcode, quantity);
        if (result == null) {
            return notFound("ASN不存在");
        }
        return success("ASN扫码成功", result);
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<AsnEntity> completeAsn(@PathVariable Long id) {
        AsnEntity result = asnService.receiveAsn(id);
        if (result == null) {
            return notFound("ASN不存在");
        }
        return success("ASN完成收货成功", result);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<AsnEntity> cancelAsn(@PathVariable Long id) {
        AsnEntity result = asnService.cancelAsn(id);
        if (result == null) {
            return notFound("ASN不存在");
        }
        return success("ASN取消成功", result);
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    private static <T> ApiResponse<T> badRequest(String message) {
        return ApiResponse.error(400, message);
    }

    private static <T> ApiResponse<T> notFound(String message) {
        return ApiResponse.error(404, message);
    }
}
