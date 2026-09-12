package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.SalesTarget;
import com.hxcoe.crm.repository.SalesTargetRepository;
import com.hxcoe.crm.service.SalesTargetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 销售目标服务实现类
 */
@Service
public class SalesTargetServiceImpl implements SalesTargetService {

    @Autowired
    private SalesTargetRepository salesTargetRepository;

    /**
     * 分页查询我的销售目标
     * @param ownerName 负责人姓名
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Page<SalesTarget> getMyTargets(String ownerName, Pageable pageable) {
        // 处理空字符串，确保查询条件正确
        ownerName = ownerName != null ? ownerName : "";

        return salesTargetRepository.findByOwnerNameContaining(ownerName, pageable);
    }
}
