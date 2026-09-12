package com.hxcoe.wms.controller;

import com.hxcoe.common.api.ApiResponse;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.wms.entity.InventoryEntity;
import com.hxcoe.wms.entity.InventoryTransactionEntity;
import com.hxcoe.wms.entity.StockCountItemEntity;
import com.hxcoe.wms.entity.StockCountJobEntity;
import com.hxcoe.wms.entity.WarehouseEntity;
import com.hxcoe.wms.repository.InventoryRepository;
import com.hxcoe.wms.repository.InventoryTransactionRepository;
import com.hxcoe.wms.repository.StockCountItemRepository;
import com.hxcoe.wms.repository.StockCountJobRepository;
import com.hxcoe.wms.repository.WarehouseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

@RestController
@RequestMapping({"/api/v1/wms/stock/count-jobs", "/wms/stock/count-jobs", "/api/wms/stock/count-jobs"})
public class StockCountController {

    @Autowired
    private StockCountJobRepository stockCountJobRepository;

    @Autowired
    private StockCountItemRepository stockCountItemRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private InventoryTransactionRepository inventoryTransactionRepository;

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String countNo,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String status
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 1) - 1, Math.max(size, 1), Sort.by(Sort.Direction.DESC, "createTime"));
        Specification<StockCountJobEntity> specification = (root, query, cb) -> {
            // Hibernate 6下cb.conjunction()+getExpressions().add()会静默失效，使用List<Predicate>+cb.and()
            List<Predicate> predicates = new ArrayList<>();
            if (countNo != null && !countNo.isBlank()) {
                predicates.add(cb.like(root.get("countNo"), "%" + countNo.trim() + "%"));
            }
            if (warehouseCode != null && !warehouseCode.isBlank()) {
                predicates.add(cb.equal(root.get("warehouseCode"), warehouseCode.trim()));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"), status.trim()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<StockCountJobEntity> result = stockCountJobRepository.findAll(specification, pageable);
        List<Map<String, Object>> rows = result.getContent().stream().map(this::toRow).toList();
        PageResult<Map<String, Object>> pageResult = PageResult.build(result.getTotalElements(), result.getSize(), result.getNumber() + 1, rows);
        return success("盘点任务列表查询成功", pageResult);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        String warehouseCode = body == null ? null : String.valueOf(body.getOrDefault("warehouseCode", ""));
        if (warehouseCode == null || warehouseCode.isBlank()) {
            return badRequest("warehouseCode 不能为空");
        }
        String countType = body == null ? null : String.valueOf(body.getOrDefault("countType", "1"));
        String createUser = body == null ? null : String.valueOf(body.getOrDefault("createUser", "admin"));

        StockCountJobEntity job = new StockCountJobEntity();
        job.setCountNo(generateCountNo());
        job.setWarehouseCode(warehouseCode.trim());
        job.setCountType(blankToDefault(countType, "1"));
        job.setStatus("0");
        job.setCreateUser(blankToDefault(createUser, "admin"));
        job.setTotalItemCount(0);
        job.setFinishedItemCount(0);
        job.setDiffCount(0);
        StockCountJobEntity saved = stockCountJobRepository.save(job);
        return success("盘点任务创建成功", toRow(saved));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        StockCountJobEntity job = stockCountJobRepository.findById(id).orElse(null);
        if (job == null) {
            return notFound("盘点任务不存在");
        }
        Map<String, Object> row = toRow(job);
        List<StockCountItemEntity> items = stockCountItemRepository.findByJobId(job.getId());
        row.put("items", toItems(items, job.getCountType()));
        return success("盘点任务查询成功", row);
    }

    @PutMapping("/{id}/start")
    public ApiResponse<Map<String, Object>> start(@PathVariable Long id) {
        try {
            StockCountJobEntity job = stockCountJobRepository.findById(id).orElse(null);
            if (job == null) {
                return notFound("盘点任务不存在");
            }
            if (Objects.equals(job.getStatus(), "3")) {
                return badRequest("盘点任务已取消");
            }
            if (!Objects.equals(job.getStatus(), "1")) {
                job.setStatus("1");
            }
            if (job.getStartTime() == null) {
                job.setStartTime(LocalDateTime.now());
            }

            List<StockCountItemEntity> existing = stockCountItemRepository.findByJobId(job.getId());
            if (existing.isEmpty()) {
                String warehouseCode = job.getWarehouseCode();
                List<InventoryEntity> inventories = warehouseCode == null ? List.of() : inventoryRepository.findByWarehouseCode(warehouseCode);
                List<StockCountItemEntity> items = new ArrayList<>();
                for (InventoryEntity inv : inventories) {
                    StockCountItemEntity item = new StockCountItemEntity();
                    item.setJob(job);
                    item.setLocationCode(inv.getLocationCode());
                    item.setMaterialCode(inv.getMaterialCode());
                    item.setMaterialName(inv.getMaterialName());
                    item.setBatchNo(inv.getBatchNo());
                    item.setUnit(inv.getUnit());
                    item.setSysQty(inv.getQuantity() == null ? BigDecimal.ZERO : inv.getQuantity());
                    item.setCountQty(BigDecimal.ZERO);
                    item.setDiffQty(BigDecimal.ZERO);
                    items.add(item);
                }
                stockCountItemRepository.saveAll(items);
                job.setTotalItemCount(items.size());
                job.setFinishedItemCount(0);
                job.setDiffCount(0);
                stockCountJobRepository.save(job);
            } else {
                stockCountJobRepository.save(job);
            }

            Map<String, Object> row = toRow(job);
            List<StockCountItemEntity> items = stockCountItemRepository.findByJobId(job.getId());
            row.put("items", toItems(items, job.getCountType()));
            return success("盘点任务开始成功", row);
        } catch (Exception e) {
            return badRequest("盘点任务开始失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/scan")
    public ApiResponse<Map<String, Object>> scan(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        StockCountJobEntity job = stockCountJobRepository.findById(id).orElse(null);
        if (job == null) {
            return notFound("盘点任务不存在");
        }
        if (!Objects.equals(job.getStatus(), "1")) {
            return badRequest("盘点任务未处于执行中");
        }
        String barcode = body == null ? null : String.valueOf(body.getOrDefault("barcode", ""));
        if (barcode == null || barcode.isBlank()) {
            return badRequest("barcode 不能为空");
        }
        BigDecimal qty = parseDecimalOrDefault(body == null ? null : body.get("quantity"), BigDecimal.ONE);

        List<StockCountItemEntity> items = stockCountItemRepository.findByJobId(job.getId());
        StockCountItemEntity matched = null;
        for (StockCountItemEntity item : items) {
            if (item == null) {
                continue;
            }
            if (Objects.equals(item.getMaterialCode(), barcode) || Objects.equals(item.getLocationCode(), barcode)) {
                matched = item;
                break;
            }
        }
        if (matched == null) {
            matched = new StockCountItemEntity();
            matched.setJob(job);
            matched.setLocationCode("");
            matched.setMaterialCode(barcode);
            matched.setMaterialName("");
            matched.setBatchNo("");
            matched.setUnit("");
            matched.setSysQty(BigDecimal.ZERO);
            matched.setCountQty(BigDecimal.ZERO);
            matched.setDiffQty(BigDecimal.ZERO);
            items.add(matched);
            job.setTotalItemCount(items.size());
        }

        BigDecimal newCountQty = (matched.getCountQty() == null ? BigDecimal.ZERO : matched.getCountQty()).add(qty);
        matched.setCountQty(newCountQty);
        matched.setDiffQty(newCountQty.subtract(matched.getSysQty() == null ? BigDecimal.ZERO : matched.getSysQty()));
        matched.setScanTime(LocalDateTime.now());

        stockCountItemRepository.save(matched);
        recalcJobStats(job.getId());

        Map<String, Object> data = new HashMap<>();
        data.put("job", toRow(stockCountJobRepository.findById(job.getId()).orElse(job)));
        data.put("item", toItem(matched, job.getCountType()));
        return success("扫码盘点成功", data);
    }

    @PutMapping("/{id}/items/{itemId}")
    public ApiResponse<Map<String, Object>> updateItem(@PathVariable Long id, @PathVariable Long itemId, @RequestBody Map<String, Object> body) {
        StockCountJobEntity job = stockCountJobRepository.findById(id).orElse(null);
        if (job == null) {
            return notFound("盘点任务不存在");
        }
        StockCountItemEntity item = stockCountItemRepository.findById(itemId).orElse(null);
        if (item == null || item.getJob() == null || !Objects.equals(item.getJob().getId(), id)) {
            return notFound("盘点明细不存在");
        }
        BigDecimal countQty = parseDecimalOrDefault(body == null ? null : body.get("countQty"), BigDecimal.ZERO);
        item.setCountQty(countQty);
        item.setDiffQty(countQty.subtract(item.getSysQty() == null ? BigDecimal.ZERO : item.getSysQty()));
        item.setScanTime(LocalDateTime.now());
        stockCountItemRepository.save(item);
        recalcJobStats(job.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("job", toRow(stockCountJobRepository.findById(job.getId()).orElse(job)));
        data.put("item", toItem(item, job.getCountType()));
        return success("盘点明细更新成功", data);
    }

    @PostMapping("/{id}/save")
    public ApiResponse<Map<String, Object>> save(@PathVariable Long id) {
        StockCountJobEntity job = stockCountJobRepository.findById(id).orElse(null);
        if (job == null) {
            return notFound("盘点任务不存在");
        }
        recalcJobStats(job.getId());
        StockCountJobEntity saved = stockCountJobRepository.findById(job.getId()).orElse(job);
        return success("盘点结果保存成功", toRow(saved));
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<Map<String, Object>> complete(@PathVariable Long id) {
        StockCountJobEntity job = stockCountJobRepository.findById(id).orElse(null);
        if (job == null) {
            return notFound("盘点任务不存在");
        }
        if (!Objects.equals(job.getStatus(), "1")) {
            return badRequest("盘点任务未处于执行中");
        }
        List<StockCountItemEntity> items = stockCountItemRepository.findByJobId(job.getId());
        for (StockCountItemEntity item : items) {
            if (item == null) {
                continue;
            }
            BigDecimal sysQty = item.getSysQty() == null ? BigDecimal.ZERO : item.getSysQty();
            BigDecimal countQty = item.getCountQty() == null ? BigDecimal.ZERO : item.getCountQty();
            BigDecimal diff = countQty.subtract(sysQty);
            if (diff.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            String wh = job.getWarehouseCode();
            String materialCode = item.getMaterialCode();
            String loc = item.getLocationCode();
            String batch = item.getBatchNo();
            InventoryEntity inv = inventoryRepository
                    .findByMaterialCodeAndWarehouseCodeAndLocationCodeAndBatchNo(materialCode, wh, loc, batch)
                    .orElse(null);
            if (inv == null) {
                inv = new InventoryEntity();
                inv.setWarehouseCode(wh);
                inv.setLocationCode(loc == null ? "" : loc);
                inv.setMaterialCode(materialCode == null ? "" : materialCode);
                inv.setMaterialName(item.getMaterialName());
                inv.setBatchNo(batch == null ? "" : batch);
                inv.setQuantity(BigDecimal.ZERO);
                inv.setUnit(item.getUnit());
            }
            inv.setQuantity(countQty);
            inventoryRepository.save(inv);

            InventoryTransactionEntity txn = new InventoryTransactionEntity();
            txn.setWarehouseCode(wh);
            txn.setMaterialCode(materialCode);
            txn.setMaterialName(item.getMaterialName());
            txn.setBatchNo(batch);
            txn.setLocationCode(loc);
            txn.setQuantity(diff);
            txn.setUnit(item.getUnit());
            txn.setType("ADJUST");
            txn.setSourceNo(job.getCountNo());
            txn.setOperator(job.getCreateUser());
            txn.setTransactionTime(LocalDateTime.now());
            inventoryTransactionRepository.save(txn);
        }

        job.setStatus("2");
        job.setEndTime(LocalDateTime.now());
        stockCountJobRepository.save(job);
        recalcJobStats(job.getId());
        StockCountJobEntity saved = stockCountJobRepository.findById(job.getId()).orElse(job);
        return success("盘点完成成功", toRow(saved));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancel(@PathVariable Long id) {
        StockCountJobEntity job = stockCountJobRepository.findById(id).orElse(null);
        if (job == null) {
            return notFound("盘点任务不存在");
        }
        job.setStatus("3");
        stockCountJobRepository.save(job);
        return success("盘点取消成功", toRow(job));
    }

    private void recalcJobStats(Long jobId) {
        StockCountJobEntity job = stockCountJobRepository.findById(jobId).orElse(null);
        if (job == null) {
            return;
        }
        List<StockCountItemEntity> items = stockCountItemRepository.findByJobId(jobId);
        int finished = 0;
        int diff = 0;
        for (StockCountItemEntity item : items) {
            if (item == null) {
                continue;
            }
            if (item.getCountQty() != null && item.getCountQty().compareTo(BigDecimal.ZERO) > 0) {
                finished++;
            }
            if (item.getDiffQty() != null && item.getDiffQty().compareTo(BigDecimal.ZERO) != 0) {
                diff++;
            }
        }
        job.setTotalItemCount(items.size());
        job.setFinishedItemCount(finished);
        job.setDiffCount(diff);
        stockCountJobRepository.save(job);
    }

    private Map<String, Object> toRow(StockCountJobEntity job) {
        WarehouseEntity wh = job.getWarehouseCode() == null ? null : warehouseRepository.findByWarehouseCode(job.getWarehouseCode());
        String warehouseName = wh == null ? (job.getWarehouseCode() == null ? "" : job.getWarehouseCode()) : wh.getWarehouseName();

        Map<String, Object> row = new HashMap<>();
        row.put("id", job.getId());
        row.put("countNo", job.getCountNo());
        row.put("warehouseId", job.getWarehouseCode());
        row.put("warehouseCode", job.getWarehouseCode());
        row.put("warehouseName", warehouseName);
        row.put("countType", job.getCountType());
        row.put("status", job.getStatus());
        row.put("totalItemCount", job.getTotalItemCount() == null ? 0 : job.getTotalItemCount());
        row.put("finishedItemCount", job.getFinishedItemCount() == null ? 0 : job.getFinishedItemCount());
        row.put("diffCount", job.getDiffCount() == null ? 0 : job.getDiffCount());
        row.put("createTime", format(job.getCreateTime()));
        row.put("createUser", job.getCreateUser());
        row.put("startTime", format(job.getStartTime()));
        row.put("endTime", format(job.getEndTime()));
        return row;
    }

    private static List<Map<String, Object>> toItems(List<StockCountItemEntity> items, String countType) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (StockCountItemEntity item : items) {
            rows.add(toItem(item, countType));
        }
        return rows;
    }

    private static Map<String, Object> toItem(StockCountItemEntity item, String countType) {
        Map<String, Object> row = new HashMap<>();
        row.put("id", item.getId());
        row.put("countId", item.getJob() == null ? null : item.getJob().getId());
        row.put("locationCode", item.getLocationCode());
        row.put("materialCode", item.getMaterialCode());
        row.put("materialName", item.getMaterialName());
        row.put("batchNo", item.getBatchNo());
        row.put("sysQty", Objects.equals(countType, "1") ? toNumber(item.getSysQty()) : 0);
        row.put("countQty", toNumber(item.getCountQty()));
        row.put("diffQty", toNumber(item.getDiffQty()));
        row.put("unit", item.getUnit());
        row.put("scanTime", format(item.getScanTime()));
        return row;
    }

    private static int toNumber(BigDecimal v) {
        if (v == null) return 0;
        return v.intValue();
    }

    private static String format(LocalDateTime t) {
        // 统一输出"yyyy-MM-dd HH:mm:ss"，避免toString()微秒非0时格式不一致
        return com.hxcoe.wms.util.WmsDateTimes.format(t);
    }

    private static String generateCountNo() {
        String time = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        int rand = new Random().nextInt(900) + 100;
        return "COUNT" + time + rand;
    }

    private static String blankToDefault(String v, String d) {
        if (v == null) return d;
        String t = v.trim();
        return t.isEmpty() ? d : t;
    }

    private static BigDecimal parseDecimalOrDefault(Object v, BigDecimal d) {
        if (v == null) {
            return d;
        }
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (Exception e) {
            return d;
        }
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
