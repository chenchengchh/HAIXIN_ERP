package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.dto.Customer360ViewDTO;

/**
 * 客户360°视图服务接口
 */
public interface Customer360Service {

    /**
     * 获取客户360°视图数据
     *
     * @param customerId 客户ID
     * @return 客户360°视图数据
     */
    Customer360ViewDTO getCustomer360View(Long customerId);
}
