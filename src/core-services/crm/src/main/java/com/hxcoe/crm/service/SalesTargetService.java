package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.SalesTarget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 销售目标服务接口
 */
public interface SalesTargetService {

    /**
     * 分页查询我的销售目标
     * @param ownerName 负责人姓名
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<SalesTarget> getMyTargets(String ownerName, Pageable pageable);
}
