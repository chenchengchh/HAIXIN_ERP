package com.hxcoe.mes.controller;

import com.hxcoe.common.result.Result;
import com.hxcoe.mes.entity.WipLocationEntity;
import com.hxcoe.mes.repository.WipLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/mes/wip", "/mes/v1/wip", "/api/v1/mes/wip", "/api/mes/wip"})
public class WipController {

    @Autowired
    private WipLocationRepository wipLocationRepository;

    @GetMapping("/{snCode}")
    public Result<WipLocationEntity> detail(@PathVariable("snCode") String snCode) {
        WipLocationEntity entity = wipLocationRepository.findBySnCode(snCode).orElse(null);
        if (entity == null) {
            return Result.fail("在制品不存在");
        }
        return Result.success("在制品详情查询成功", entity);
    }
}
