package com.hxcoe.scm.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.api.ResponseStatusAdapter;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.hxcoe.scm.client.SrmClient;
import com.hxcoe.scm.entity.SupplierEntity;
import com.hxcoe.scm.service.SupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/scm/suppliers", "/scm/suppliers"})
public class SupplierController {

    @Autowired
    private SrmClient srmClient;

    @Autowired
    private SupplierService supplierService;

    @GetMapping
    public ApiResponse<Map<String, Object>> getSuppliers(@RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer size) {
        int p = Math.max(page == null ? 1 : page, 1);
        int s = Math.max(size == null ? 10 : size, 1);
        try {
            Result<Map<String, Object>> remote = srmClient.getSuppliers(Math.max(p - 1, 0), s);
            if (remote != null && ResponseStatusAdapter.isSuccess(remote.getCode()) && remote.getData() != null) {
                return success("供应商查询成功", normalizeRemotePage(remote.getData(), p, s));
            }
        } catch (Exception ignored) {
        }
        Result<PageResult<SupplierEntity>> local = supplierService.getSuppliersByPage(PageRequest.of(p - 1, s));
        PageResult<SupplierEntity> pr = local != null && ResponseStatusAdapter.isSuccess(local.getCode()) ? local.getData() : null;
        Map<String, Object> data = new HashMap<>();
        if (pr != null) {
            data.put("total", pr.getTotal());
            data.put("page", pr.getCurrentPage());
            data.put("size", pr.getPageSize());
            data.put("list", pr.getRecords());
        } else {
            data.put("total", 0L);
            data.put("page", p);
            data.put("size", s);
            data.put("list", java.util.List.of());
        }
        return success("供应商查询成功", data);
    }

    private static Map<String, Object> normalizeRemotePage(Map<String, Object> raw, int page, int size) {
        Map<String, Object> data = new HashMap<>();
        Object total = raw.get("total");
        if (total == null) total = raw.get("totalElements");
        Object pageValue = raw.get("page");
        if (pageValue == null) pageValue = raw.get("currentPage");
        Object sizeValue = raw.get("size");
        if (sizeValue == null) sizeValue = raw.get("pageSize");
        Object list = raw.get("list");
        if (!(list instanceof java.util.List<?>)) {
            list = raw.get("content");
        }
        if (!(list instanceof java.util.List<?>)) {
            list = raw.get("records");
        }
        data.put("total", total instanceof Number n ? n.longValue() : 0L);
        data.put("page", pageValue instanceof Number n ? n.intValue() : page);
        data.put("size", sizeValue instanceof Number n ? n.intValue() : size);
        data.put("list", list instanceof java.util.List<?> rows ? rows : java.util.List.of());
        return data;
    }

    private static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.success(message, data);
    }

    // 其他方法暂时废弃或同样代理
}
