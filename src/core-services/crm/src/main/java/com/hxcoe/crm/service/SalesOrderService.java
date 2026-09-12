package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.SalesOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

/**
 * 销售订单服务接口
 */
public interface SalesOrderService {

    /**
     * 创建销售订单
     */
    SalesOrderEntity createOrder(SalesOrderEntity order);

    /**
     * 更新销售订单
     */
    SalesOrderEntity updateOrder(Long id, SalesOrderEntity order);

    /**
     * 删除销售订单
     */
    void deleteOrder(Long id);

    /**
     * 根据ID获取销售订单
     */
    Optional<SalesOrderEntity> getOrderById(Long id);

    /**
     * 分页查询销售订单
     */
    Page<SalesOrderEntity> getOrderList(Specification<SalesOrderEntity> spec, Pageable pageable);

    /**
     * 提交审批
     */
    SalesOrderEntity submitOrder(Long id);

    /**
     * 审批通过
     */
    SalesOrderEntity approveOrder(Long id);

    /**
     * 审批拒绝。
     * <p>由OA审批回调驱动，将审批中（submitted）的订单置为rejected并回写审批意见。</p>
     *
     * @param id      订单ID
     * @param comment 审批意见
     * @return 更新后的订单，订单不存在或状态不正确时返回 null
     */
    SalesOrderEntity rejectOrder(Long id, String comment);
}
