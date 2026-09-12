package com.hxcoe.erp.controller;



import com.hxcoe.erp.entity.*;
import com.hxcoe.erp.service.*;
import com.hxcoe.erp.client.CrmCustomerClient;
import com.hxcoe.erp.client.HrDepartmentClient;
import com.hxcoe.erp.client.HrEmployeeClient;
import com.hxcoe.erp.client.HrPositionClient;
import com.hxcoe.erp.client.BomCategoryClient;
import com.hxcoe.erp.client.BomStructureClient;
import com.hxcoe.erp.client.SrmSupplierClient;
import com.hxcoe.erp.client.WmsLocationClient;
import com.hxcoe.erp.client.WmsWarehouseClient;
import com.hxcoe.erp.support.ResultDataExtractor;
import com.hxcoe.erp.support.MapFieldUtils;

import com.hxcoe.common.result.PageResult;

import com.hxcoe.common.api.ResultAdapter;
import com.hxcoe.common.result.Result;
import com.hxcoe.common.api.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基础数据控制器
 */


@RestController

@RequestMapping("/api/v1/erp/basic-data")

public class BasicDataController {


    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private MaterialService materialService;

    @Autowired
    private WarehouseService warehouseService;

    @Autowired
    private WmsWarehouseClient wmsWarehouseClient;

    @Autowired
    private WmsLocationClient wmsLocationClient;

    @Autowired
    private SrmSupplierClient srmSupplierClient;

    @Autowired
    private CrmCustomerClient crmCustomerClient;

    @Autowired
    private HrDepartmentClient hrDepartmentClient;

    @Autowired
    private HrPositionClient hrPositionClient;

    @Autowired
    private HrEmployeeClient hrEmployeeClient;

    @Autowired
    private BomCategoryClient bomCategoryClient;

    @Autowired
    private BomStructureClient bomStructureClient;

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Value("${erp.master-data.warehouse-write-enabled:false}")
    private boolean warehouseWriteEnabled;

    /**
     * 获取组织列表
     *
     * @param page 页码
     * @param size 每页条数
     * @param name 组织名称
     * @param code 组织编码
     * @return 分页结果
     */

    @GetMapping("/organization")

    public ApiResponse<PageResult<OrganizationEntity>> getOrganizationList(

            @RequestParam(defaultValue = "1") Integer page,

            @RequestParam(defaultValue = "10") Integer size,

            @RequestParam(required = false) String name,

            @RequestParam(required = false) String code) {

        PageResult<OrganizationEntity> result = organizationService.getOrganizationList(page, size, name, code);

            return success("组织操作成功", result);

    }


    /**
     * 创建组织
     *
     * @param organizationEntity 组织实体
     * @return 创建结果
     */

    @PostMapping("/organization")

    public ApiResponse<OrganizationEntity> createOrganization(@RequestBody OrganizationEntity organizationEntity) {

        OrganizationEntity result = organizationService.createOrganization(organizationEntity);

            return success("组织操作成功", result);

    }


    /**
     * 更新组织
     *
     * @param id 组织ID
     * @param organizationEntity 组织实体
     * @return 更新结果
     */

    @PutMapping("/organization/{id}")

    public ApiResponse<OrganizationEntity> updateOrganization(@PathVariable Long id, @RequestBody OrganizationEntity organizationEntity) {

        organizationEntity.setId(id);

        OrganizationEntity result = organizationService.updateOrganization(organizationEntity);
        if (result == null) {
            return notFound("组织不存在");
        }
        return success("组织操作成功", result);

    }


    /**
     * 删除组织
     *
     * @param id 组织ID
     * @return 删除结果
     */

    @DeleteMapping("/organization/{id}")

    public ApiResponse<Boolean> deleteOrganization(@PathVariable Long id) {

        boolean result = organizationService.deleteOrganization(id);

        if (!result) {
            return notFound("组织不存在");
        }
        return success("组织操作成功", result);

    }


    /**
     * 根据ID查询组织
     *
     * @param id 组织ID
     * @return 查询结果
     */

    @GetMapping("/organization/{id}")
    public ApiResponse<OrganizationEntity> getOrganizationById(@PathVariable Long id) {
        OrganizationEntity result = organizationService.getOrganizationById(id);
        if (result == null) {
            return notFound("组织不存在");
        }
        return success("组织操作成功", result);
    }

    // --- 会计科目 (Account) 相关接口 ---

    /**
     * 获取会计科目列表
     */
    @GetMapping("/accounts")
    public ApiResponse<PageResult<AccountEntity>> getAccountList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String type) {
        PageResult<AccountEntity> result = accountService.getAccountList(page, size, name, code, type);
        return success("会计科目查询成功", result);
    }

    /**
     * 创建会计科目
     */
    @PostMapping("/accounts")
    public ApiResponse<AccountEntity> createAccount(@RequestBody AccountEntity accountEntity) {
        AccountEntity result = accountService.createAccount(accountEntity);
        return success("会计科目创建成功", result);
    }

    /**
     * 更新会计科目
     */
    @PutMapping("/accounts/{id}")
    public ApiResponse<AccountEntity> updateAccount(@PathVariable Long id, @RequestBody AccountEntity accountEntity) {
        accountEntity.setId(id);
        AccountEntity result = accountService.updateAccount(accountEntity);
        if (result != null) {
            return success("会计科目更新成功", result);
        }
        return notFound("会计科目不存在");
    }

    /**
     * 删除会计科目
     */
    @DeleteMapping("/accounts/{id}")
    public ApiResponse<Boolean> deleteAccount(@PathVariable Long id) {
        boolean result = accountService.deleteAccount(id);
        if (result) {
            return success("会计科目删除成功", result);
        }
        return notFound("会计科目不存在");
    }

    /**
     * 根据ID查询会计科目
     */
    @GetMapping("/accounts/{id}")
    public ApiResponse<AccountEntity> getAccountById(@PathVariable Long id) {
        AccountEntity result = accountService.getAccountById(id);
        if (result != null) {
            return success("会计科目查询成功", result);
        }
        return notFound("会计科目不存在");
    }

    // --- 供应商 (Supplier) 相关接口 ---

    @GetMapping("/suppliers")
    public ApiResponse<PageResult<SupplierEntity>> getSupplierList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code) {
        int p = Math.max(page == null ? 1 : page, 1);
        int s = Math.max(size == null ? 10 : size, 1);
        if ((name != null && !name.isBlank()) || (code != null && !code.isBlank())) {
            return success("供应商查询成功", supplierService.getSupplierList(p, s, name, code));
        }
        try {
            Result<Object> res = srmSupplierClient.page(Math.max(p - 1, 0), s);
            Object data = res != null ? res.getData() : null;
            ResultDataExtractor.PageParts parts = ResultDataExtractor.extractPage(data);

            java.util.List<SupplierEntity> mapped = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> row : parts.rows()) {
                mapped.add(mapSrmSupplier(row));
            }
            PageResult<SupplierEntity> pageResult = PageResult.build(parts.total(), parts.size(), p, mapped);
            return success("供应商查询成功", pageResult);
        } catch (Exception ignored) {
            return success("供应商查询成功", supplierService.getSupplierList(p, s, null, null));
        }
    }

    @PostMapping("/suppliers")
    public ApiResponse<SupplierEntity> createSupplier(@RequestBody SupplierEntity supplier) {
        return badRequest("供应商主数据权威归 SRM，ERP 禁止写入");
    }

    @PutMapping("/suppliers/{id}")
    public ApiResponse<SupplierEntity> updateSupplier(@PathVariable Long id, @RequestBody SupplierEntity supplier) {
        return badRequest("供应商主数据权威归 SRM，ERP 禁止写入");
    }

    @DeleteMapping("/suppliers/{id}")
    public ApiResponse<Boolean> deleteSupplier(@PathVariable Long id) {
        return badRequest("供应商主数据权威归 SRM，ERP 禁止写入");
    }

    // --- 物料 (Material) 相关接口 ---

    @GetMapping("/materials")
    public ApiResponse<PageResult<MaterialEntity>> getMaterialList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code) {
        return success("物料查询成功", materialService.getMaterialList(page, size, name, code));
    }

    @PostMapping("/materials")
    public ApiResponse<MaterialEntity> createMaterial(@RequestBody MaterialEntity material) {
        return success("物料创建成功", materialService.createMaterial(material));
    }

    @PutMapping("/materials/{id}")
    public ApiResponse<MaterialEntity> updateMaterial(@PathVariable Long id, @RequestBody MaterialEntity material) {
        material.setId(id);
        MaterialEntity result = materialService.updateMaterial(material);
        if (result == null) {
            return notFound("物料不存在");
        }
        return success("物料更新成功", result);
    }

    @DeleteMapping("/materials/{id}")
    public ApiResponse<Boolean> deleteMaterial(@PathVariable Long id) {
        boolean result = materialService.deleteMaterial(id);
        if (!result) {
            return notFound("物料不存在");
        }
        return success("物料删除成功", result);
    }

    @PostMapping("/materials/{id}/approve")
    public ApiResponse<MaterialEntity> approveMaterial(@PathVariable Long id) {
        MaterialEntity result = materialService.approveMaterial(id);
        if (result == null) {
            return notFound("物料不存在");
        }
        return success("物料审核成功", result);
    }

    @PostMapping("/materials/batch-approve")
    public ApiResponse<Map<String, Object>> batchApproveMaterials(@RequestBody Map<String, Object> body) {
        Object idsObj = body != null ? body.get("ids") : null;
        List<Long> ids = new java.util.ArrayList<>();
        if (idsObj instanceof List<?> list) {
            for (Object it : list) {
                if (it instanceof Number n) ids.add(n.longValue());
                else if (it != null) {
                    try { ids.add(Long.parseLong(String.valueOf(it))); } catch (Exception ignored) {}
                }
            }
        }
        int count = materialService.batchApproveMaterials(ids);
        Map<String, Object> data = new HashMap<>();
        data.put("approved_count", count);
        return success("批量审核成功", data);
    }

    // --- 仓库 (Warehouse) 相关接口 ---

    @GetMapping("/warehouses")
    public ApiResponse<PageResult<WarehouseEntity>> getWarehouseList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code) {
        int p = Math.max(page == null ? 1 : page, 1);
        int s = Math.max(size == null ? 10 : size, 1);
        try {
            Result<Object> res = wmsWarehouseClient.list(p - 1, s);
            Object data = res != null ? res.getData() : null;
            ResultDataExtractor.PageParts parts = ResultDataExtractor.extractPage(data);

            java.util.List<WarehouseEntity> mapped = new java.util.ArrayList<>();
            for (java.util.Map<String, Object> row : parts.rows()) {
                mapped.add(mapWarehouse(row));
            }

            PageResult<WarehouseEntity> pageResult = PageResult.build(parts.total(), parts.size(), parts.page(), mapped);
            return success("仓库查询成功", pageResult);
        } catch (Exception ignored) {
            PageResult<WarehouseEntity> pageResult = warehouseService.getWarehouseList(p, s, name, code);
            return success("仓库查询成功", pageResult);
        }
    }

    @PostMapping("/warehouses")
    public ApiResponse<WarehouseEntity> createWarehouse(@RequestBody WarehouseEntity warehouse) {
        if (!warehouseWriteEnabled) {
            return badRequest("仓库/库位权威归 WMS，ERP 禁止写入");
        }
        java.util.Map<String, Object> body = buildWarehouseBody(warehouse);
        Result<Object> res = wmsWarehouseClient.create(body);
        java.util.Map<String, Object> row = ResultDataExtractor.asMap(res != null ? res.getData() : null);
        return success("仓库创建成功", mapWarehouse(row));
    }

    @PutMapping("/warehouses/{id}")
    public ApiResponse<WarehouseEntity> updateWarehouse(@PathVariable Long id, @RequestBody WarehouseEntity warehouse) {
        if (!warehouseWriteEnabled) {
            return badRequest("仓库/库位权威归 WMS，ERP 禁止写入");
        }
        java.util.Map<String, Object> body = buildWarehouseBody(warehouse);
        Result<Object> res = wmsWarehouseClient.update(id, body);
        java.util.Map<String, Object> row = ResultDataExtractor.asMap(res != null ? res.getData() : null);
        WarehouseEntity updated = mapWarehouse(row);
        updated.setId(id);
        return success("仓库更新成功", updated);
    }

    /**
     * 将 WMS 返回的仓库行映射为 ERP 仓库实体
     * @param row WMS 行数据
     * @return 仓库实体
     */
    private WarehouseEntity mapWarehouse(java.util.Map<String, Object> row) {
        WarehouseEntity w = new WarehouseEntity();
        w.setId(MapFieldUtils.getLong(row, "id"));
        w.setWarehouseCode(MapFieldUtils.getStringOrEmpty(row, "warehouseCode", "warehouse_code"));
        w.setWarehouseName(MapFieldUtils.getStringOrEmpty(row, "warehouseName", "warehouse_name"));
        w.setManager(MapFieldUtils.getString(row, "manager", null));
        w.setStatus(MapFieldUtils.getInteger(row, "status"));
        return w;
    }

    /**
     * 构造 WMS 仓库写入请求体
     * @param warehouse ERP 仓库实体
     * @return 请求体 Map
     */
    private java.util.Map<String, Object> buildWarehouseBody(WarehouseEntity warehouse) {
        java.util.Map<String, Object> body = new java.util.HashMap<>();
        body.put("warehouse_code", warehouse != null ? warehouse.getWarehouseCode() : null);
        body.put("warehouse_name", warehouse != null ? warehouse.getWarehouseName() : null);
        body.put("manager", warehouse != null ? warehouse.getManager() : null);
        body.put("status", warehouse != null ? warehouse.getStatus() : null);
        return body;
    }

    @DeleteMapping("/warehouses/{id}")
    public ApiResponse<Boolean> deleteWarehouse(@PathVariable Long id) {
        if (!warehouseWriteEnabled) {
            return badRequest("仓库/库位权威归 WMS，ERP 禁止写入");
        }
        wmsWarehouseClient.delete(id);
        return success("仓库删除成功", true);
    }

    @GetMapping("/locations")
    public ApiResponse<PageResult<Map<String, Object>>> getLocationList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String zoneCode,
            @RequestParam(required = false) String locationTypeCode,
            @RequestParam(required = false) String status
    ) {
        int p = Math.max(page == null ? 1 : page, 1);
        int s = size == null ? 10 : Math.min(Math.max(size, 1), 200);
        try {
            Result<Object> res = wmsLocationClient.page(p, s, keyword, warehouseCode, zoneCode, locationTypeCode, status);
            Object data = res != null ? res.getData() : null;
            ResultDataExtractor.PageParts parts = ResultDataExtractor.extractPage(data);
            PageResult<Map<String, Object>> pageResult = PageResult.build(parts.total(), parts.size(), parts.page(), parts.rows());
            return success("库位列表查询成功", pageResult);
        } catch (Exception ignored) {
        }
        int offset = (p - 1) * s;

        StringBuilder where = new StringBuilder(" WHERE 1=1 ");
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (keyword != null && !keyword.isBlank()) {
            where.append(" AND (l.location_code LIKE :kw OR l.location_name LIKE :kw) ");
            params.addValue("kw", "%" + keyword.trim() + "%");
        }
        if (warehouseCode != null && !warehouseCode.isBlank()) {
            where.append(" AND l.warehouse_code = :warehouseCode ");
            params.addValue("warehouseCode", warehouseCode.trim());
        }
        if (zoneCode != null && !zoneCode.isBlank()) {
            where.append(" AND l.zone_code = :zoneCode ");
            params.addValue("zoneCode", zoneCode.trim());
        }
        if (locationTypeCode != null && !locationTypeCode.isBlank()) {
            where.append(" AND l.location_type_code = :locationTypeCode ");
            params.addValue("locationTypeCode", locationTypeCode.trim());
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND l.status = :status ");
            params.addValue("status", status.trim());
        }

        Long total = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM erp_location l" + where, params, Long.class);
        if (total == null) {
            total = 0L;
        }

        params.addValue("limit", s);
        params.addValue("offset", offset);

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT l.id, l.location_code, l.location_name, l.warehouse_code, w.warehouse_name, l.zone_code, l.location_type_code, l.status, l.remark, l.created_time, l.updated_time "
                        + "FROM erp_location l "
                        + "LEFT JOIN erp_warehouse w ON w.warehouse_code = l.warehouse_code AND w.is_deleted = 0 "
                        + where
                        + "ORDER BY l.updated_time DESC "
                        + "LIMIT :limit OFFSET :offset",
                params
        );

        List<Map<String, Object>> mapped = new java.util.ArrayList<>();
        for (Map<String, Object> r : rows) {
            mapped.add(mapLocationRow(r));
        }
        return success("库位列表查询成功", PageResult.build(total, s, p, mapped));
    }

    @GetMapping("/locations/{id}")
    public ApiResponse<Map<String, Object>> getLocationDetail(@PathVariable Long id) {
        if (id == null) {
            return badRequest("id不能为空");
        }
        try {
            Result<Object> res = wmsLocationClient.detail(id);
            java.util.Map<String, Object> row = ResultDataExtractor.asMap(res != null ? res.getData() : null);
            if (!row.isEmpty()) {
                return success("库位查询成功", row);
            }
        } catch (Exception ignored) {
        }
        MapSqlParameterSource params = new MapSqlParameterSource("id", id);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT l.id, l.location_code, l.location_name, l.warehouse_code, w.warehouse_name, l.zone_code, l.location_type_code, l.status, l.remark, l.created_time, l.updated_time "
                        + "FROM erp_location l "
                        + "LEFT JOIN erp_warehouse w ON w.warehouse_code = l.warehouse_code AND w.is_deleted = 0 "
                        + "WHERE l.id = :id LIMIT 1",
                params
        );
        if (rows.isEmpty()) {
            return notFound("库位不存在");
        }
        return success("库位查询成功", mapLocationRow(rows.get(0)));
    }

    /**
     * 将库位 SQL 行（下划线命名）映射为前端使用的驼峰 Map
     * @param r 数据库行
     * @return 驼峰命名字段 Map
     */
    private Map<String, Object> mapLocationRow(Map<String, Object> r) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", r.get("id"));
        row.put("locationCode", r.get("location_code"));
        row.put("locationName", r.get("location_name"));
        row.put("warehouseId", r.get("warehouse_code"));
        row.put("warehouseCode", r.get("warehouse_code"));
        row.put("warehouseName", r.get("warehouse_name") == null
                ? String.valueOf(r.getOrDefault("warehouse_code", ""))
                : r.get("warehouse_name"));
        row.put("zoneCode", r.get("zone_code"));
        row.put("zoneName", r.get("zone_code") == null ? "" : String.valueOf(r.get("zone_code")));
        row.put("locationTypeCode", r.get("location_type_code"));
        row.put("status", r.get("status"));
        row.put("remark", r.get("remark"));
        row.put("createdAt", r.get("created_time") == null ? "" : String.valueOf(r.get("created_time")));
        row.put("updatedAt", r.get("updated_time") == null ? "" : String.valueOf(r.get("updated_time")));
        return row;
    }

    /**
     * 将 SRM 返回的供应商行映射为 ERP 供应商实体
     * @param row SRM 行数据（驼峰/下划线混合命名）
     * @return 供应商实体
     */
    private SupplierEntity mapSrmSupplier(java.util.Map<String, Object> row) {
        SupplierEntity s = new SupplierEntity();
        s.setId(MapFieldUtils.getLong(row, "id"));
        s.setSupplierCode(MapFieldUtils.getStringOrEmpty(row, "supplierCode", "supplier_code"));
        s.setSupplierName(MapFieldUtils.getStringOrEmpty(row, "supplierName", "supplier_name"));
        s.setContactPerson(MapFieldUtils.getString(row, "contactPerson", "contact_person"));
        s.setContactPhone(MapFieldUtils.getString(row, "contactPhone", "contact_phone"));
        s.setEmail(MapFieldUtils.getString(row, "email", null));
        s.setAddress(MapFieldUtils.getString(row, "address", null));
        // 供应商类型兼容多种字段命名
        String supplierType = MapFieldUtils.getString(row, "type", null);
        if (supplierType == null) supplierType = MapFieldUtils.getString(row, "category", null);
        if (supplierType == null) supplierType = MapFieldUtils.getString(row, "supplierType", "supplier_type");
        s.setSupplierType(supplierType);
        s.setRating(MapFieldUtils.getString(row, "rating", null));
        s.setStatus(MapFieldUtils.toActiveStatus(row.get("status")));
        s.setIsDeleted(0);
        return s;
    }

    @GetMapping("/customers")
    public ApiResponse<PageResult<Object>> getCustomers(@RequestParam Map<String, Object> params) {
        Map<String, Object> crmParams = new HashMap<>();
        if (params != null) {
            if (params.containsKey("page")) crmParams.put("page", params.get("page"));
            if (params.containsKey("size")) crmParams.put("size", params.get("size"));
            if (params.containsKey("customer_code")) crmParams.put("customerNo", params.get("customer_code"));
            if (params.containsKey("customer_name")) crmParams.put("customerName", params.get("customer_name"));
            if (params.containsKey("customer_level")) crmParams.put("level", params.get("customer_level"));
            if (params.containsKey("status")) crmParams.put("status", params.get("status"));
        }
        return ResultAdapter.fromResult(crmCustomerClient.list(crmParams));
    }

    @GetMapping("/customers/{id}")
    public ApiResponse<Object> getCustomerDetail(@PathVariable Long id) {
        return ResultAdapter.fromResult(crmCustomerClient.getById(id));
    }

    @PostMapping("/customers")
    public ApiResponse<Object> createCustomer(@RequestBody Map<String, Object> body) {
        return ResultAdapter.fromResult(crmCustomerClient.create(body));
    }

    @PutMapping("/customers/{id}")
    public ApiResponse<Object> updateCustomer(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        if (body != null) {
            body.put("id", id);
        }
        return ResultAdapter.fromResult(crmCustomerClient.update(body));
    }

    @DeleteMapping("/customers/{id}")
    public ApiResponse<Boolean> deleteCustomer(@PathVariable Long id) {
        return ResultAdapter.fromResult(crmCustomerClient.delete(id));
    }

    @GetMapping("/departments")
    public ApiResponse<PageResult<Object>> getDepartments(@RequestParam Map<String, Object> params) {
        Map<String, Object> forwarded = new HashMap<>();
        if (params != null) {
            forwarded.putAll(params);
        }
        Object pageObj = forwarded.get("page");
        if (pageObj instanceof Number) {
            int page = ((Number) pageObj).intValue();
            forwarded.put("page", Math.max(page - 1, 0));
        }
        return ResultAdapter.fromResult(hrDepartmentClient.page(forwarded));
    }

    @PostMapping("/departments")
    public ApiResponse<Object> createDepartment(@RequestBody Map<String, Object> body) {
        return ResultAdapter.fromResult(hrDepartmentClient.create(body));
    }

    @PutMapping("/departments/{id}")
    public ApiResponse<Object> updateDepartment(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResultAdapter.fromResult(hrDepartmentClient.update(id, body));
    }

    @DeleteMapping("/departments/{id}")
    public ApiResponse<Void> deleteDepartment(@PathVariable Long id) {
        return ResultAdapter.fromResult(hrDepartmentClient.delete(id));
    }

    @GetMapping("/positions")
    public ApiResponse<PageResult<Object>> getPositions(@RequestParam Map<String, Object> params) {
        Map<String, Object> forwarded = new HashMap<>();
        if (params != null) {
            forwarded.putAll(params);
        }
        Object pageObj = forwarded.get("page");
        if (pageObj instanceof Number) {
            int page = ((Number) pageObj).intValue();
            forwarded.put("page", Math.max(page - 1, 0));
        }
        return ResultAdapter.fromResult(hrPositionClient.page(forwarded));
    }

    @PostMapping("/positions")
    public ApiResponse<Object> createPosition(@RequestBody Map<String, Object> body) {
        return ResultAdapter.fromResult(hrPositionClient.create(body));
    }

    @PutMapping("/positions/{id}")
    public ApiResponse<Object> updatePosition(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResultAdapter.fromResult(hrPositionClient.update(id, body));
    }

    @DeleteMapping("/positions/{id}")
    public ApiResponse<Void> deletePosition(@PathVariable Long id) {
        return ResultAdapter.fromResult(hrPositionClient.delete(id));
    }

    @GetMapping("/employees")
    public ApiResponse<PageResult<Object>> getEmployees(@RequestParam Map<String, Object> params) {
        Map<String, Object> forwarded = new HashMap<>();
        if (params != null) {
            forwarded.putAll(params);
        }
        Object pageObj = forwarded.get("page");
        if (pageObj instanceof Number) {
            int page = ((Number) pageObj).intValue();
            forwarded.put("page", Math.max(page - 1, 0));
        }
        return ResultAdapter.fromResult(hrEmployeeClient.page(forwarded));
    }

    @PostMapping("/employees")
    public ApiResponse<Object> createEmployee(@RequestBody Map<String, Object> body) {
        return ResultAdapter.fromResult(hrEmployeeClient.create(body));
    }

    @GetMapping("/employees/{id}")
    public ApiResponse<Object> getEmployeeById(@PathVariable Long id) {
        return ResultAdapter.fromResult(hrEmployeeClient.getById(id));
    }

    @PutMapping("/employees/{id}")
    public ApiResponse<Object> updateEmployee(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ResultAdapter.fromResult(hrEmployeeClient.update(id, body));
    }

    @DeleteMapping("/employees/{id}")
    public ApiResponse<Void> deleteEmployee(@PathVariable Long id) {
        return ResultAdapter.fromResult(hrEmployeeClient.delete(id));
    }

    @GetMapping("/material-categories")
    public ApiResponse<Object> getMaterialCategories() {
        return bomCategoryClient.tree();
    }

    @GetMapping("/boms")
    public ApiResponse<Object> getBoms(@RequestParam Map<String, Object> params) {
        if (params == null) {
            return ApiResponse.success(Collections.emptyList());
        }
        Object materialId = params.getOrDefault("materialId", params.get("material_id"));
        if (materialId instanceof Number) {
            return bomStructureClient.getByMaterialId(((Number) materialId).longValue());
        }
        Object productId = params.getOrDefault("productId", params.get("product_id"));
        if (productId instanceof Number) {
            String version = params.get("version") != null ? String.valueOf(params.get("version")) : null;
            return bomStructureClient.getTree(((Number) productId).longValue(), version);
        }
        return ApiResponse.success(Collections.emptyList());
    }

    @PostMapping("/boms")
    public ApiResponse<Object> createBom(@RequestBody Map<String, Object> body) {
        return bomStructureClient.create(body);
    }

    @GetMapping("/boms/{id}")
    public ApiResponse<Object> getBomById(@PathVariable Long id) {
        return bomStructureClient.getHeaderById(id);
    }

    @PutMapping("/boms/{id}")
    public ApiResponse<Object> updateBom(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return bomStructureClient.update(id, body);
    }

    @DeleteMapping("/boms/{id}")
    public ApiResponse<Void> deleteBom(@PathVariable Long id) {
        return bomStructureClient.delete(id);
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
