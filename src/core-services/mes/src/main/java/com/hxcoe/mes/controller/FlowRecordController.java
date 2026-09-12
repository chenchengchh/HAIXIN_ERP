package com.hxcoe.mes.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.mes.entity.FlowRecordEntity;
import com.hxcoe.mes.repository.FlowRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/mes/flow-records", "/mes/v1/flow-records", "/api/v1/mes/flow-records", "/api/mes/flow-records"})
public class FlowRecordController {

    @Autowired
    private FlowRecordRepository flowRecordRepository;

    @GetMapping
    public Result<List<FlowRecordEntity>> list() {
        return Result.success("流转记录列表查询成功", flowRecordRepository.findAll());
    }

    @GetMapping("/product/{snCode}")
    public Result<List<FlowRecordEntity>> productHistory(@PathVariable("snCode") String snCode) {
        return Result.success("产品流转历史查询成功", flowRecordRepository.findBySnCodeOrderByTimestampDesc(snCode));
    }
}
