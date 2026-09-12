package com.hxcoe.mes.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.mes.entity.BatchEntity;
import com.hxcoe.mes.repository.BatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping({"/mes/batches", "/mes/v1/batches", "/api/v1/mes/batches", "/api/mes/batches"})
public class BatchController {

    @Autowired
    private BatchRepository batchRepository;

    @GetMapping
    public Result<List<BatchEntity>> list() {
        return Result.success("批次列表查询成功", batchRepository.findAll());
    }

    @PostMapping
    public Result<BatchEntity> create(@RequestBody BatchEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        if (entity.getStatus() == null || entity.getStatus().isBlank()) {
            entity.setStatus("in_process");
        }
        return Result.success("批次创建成功", batchRepository.save(entity));
    }

    @GetMapping("/{batchNo}")
    public Result<BatchEntity> detail(@PathVariable("batchNo") String batchNo) {
        BatchEntity entity = batchRepository.findByBatchNo(batchNo).orElse(null);
        if (entity == null) {
            return Result.fail("批次不存在");
        }
        return Result.success("批次详情查询成功", entity);
    }
}
