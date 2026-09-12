package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.ServiceTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 服务工单服务接口
 */
public interface ServiceTicketService {

    /**
     * 创建工单
     * @param ticket 工单数据
     * @return 创建后的工单
     */
    ServiceTicket createTicket(ServiceTicket ticket);

    /**
     * 查询工单详情
     * @param id 工单ID
     * @return 工单详情
     */
    ServiceTicket getTicketDetail(Long id);

    /**
     * 更新工单
     * @param id 工单ID
     * @param ticket 工单数据
     * @return 更新后的工单
     */
    ServiceTicket updateTicket(Long id, ServiceTicket ticket);

    /**
     * 分配工单（状态置为 ASSIGNED）
     * @param id 工单ID
     * @param assigneeId 处理人ID
     * @param assigneeName 处理人姓名
     * @return 分配后的工单
     */
    ServiceTicket assignTicket(Long id, Long assigneeId, String assigneeName);

    /**
     * 回复工单（回复记录追加到 replies，状态置为 REPLIED）
     * @param id 工单ID
     * @param content 回复内容
     * @param replyBy 回复人
     * @return 回复后的工单
     */
    ServiceTicket replyTicket(Long id, String content, String replyBy);

    /**
     * 关闭工单（状态置为 CLOSED 并记录关闭时间）
     * @param id 工单ID
     * @return 关闭后的工单
     */
    ServiceTicket closeTicket(Long id);

    /**
     * 分页查询我的工单列表
     * @param assigneeName 处理人姓名
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<ServiceTicket> getMyTickets(String assigneeName, String status, Pageable pageable);
}
