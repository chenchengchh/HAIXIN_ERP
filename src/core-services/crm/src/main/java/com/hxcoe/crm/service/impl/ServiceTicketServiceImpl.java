package com.hxcoe.crm.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hxcoe.crm.entity.ServiceTicket;
import com.hxcoe.crm.repository.ServiceTicketRepository;
import com.hxcoe.crm.service.ServiceTicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务工单服务实现类
 */
@Service
public class ServiceTicketServiceImpl implements ServiceTicketService {

    /**
     * 回复时间统一格式（与全局Jackson日期格式一致）
     */
    private static final DateTimeFormatter REPLY_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ServiceTicketRepository serviceTicketRepository;

    /**
     * 创建工单，自动生成工单编号（TK + 时间戳），默认状态为 OPEN
     * @param ticket 工单数据
     * @return 创建后的工单
     */
    @Override
    public ServiceTicket createTicket(ServiceTicket ticket) {
        if (ticket.getTicketNo() == null || ticket.getTicketNo().isEmpty()) {
            ticket.setTicketNo("TK" + System.currentTimeMillis());
        }
        if (ticket.getStatus() == null || ticket.getStatus().isEmpty()) {
            ticket.setStatus("OPEN");
        }
        if (ticket.getPriority() == null || ticket.getPriority().isEmpty()) {
            ticket.setPriority("MEDIUM");
        }
        return serviceTicketRepository.save(ticket);
    }

    /**
     * 查询工单详情
     * @param id 工单ID
     * @return 工单详情
     */
    @Override
    public ServiceTicket getTicketDetail(Long id) {
        return serviceTicketRepository.findById(id).orElse(null);
    }

    /**
     * 更新工单信息
     * @param id 工单ID
     * @param ticket 工单数据
     * @return 更新后的工单，工单不存在时返回 null
     */
    @Override
    public ServiceTicket updateTicket(Long id, ServiceTicket ticket) {
        ServiceTicket existingTicket = serviceTicketRepository.findById(id).orElse(null);
        if (existingTicket != null) {
            // 更新工单信息
            existingTicket.setTitle(ticket.getTitle());
            existingTicket.setCustomerId(ticket.getCustomerId());
            existingTicket.setCustomerName(ticket.getCustomerName());
            existingTicket.setContactName(ticket.getContactName());
            existingTicket.setPhone(ticket.getPhone());
            existingTicket.setCategory(ticket.getCategory());
            existingTicket.setPriority(ticket.getPriority());
            existingTicket.setStatus(ticket.getStatus());
            existingTicket.setDescription(ticket.getDescription());

            return serviceTicketRepository.save(existingTicket);
        }
        return null;
    }

    /**
     * 分配工单，记录处理人信息并将状态置为 ASSIGNED
     * @param id 工单ID
     * @param assigneeId 处理人ID
     * @param assigneeName 处理人姓名
     * @return 分配后的工单，工单不存在时返回 null
     */
    @Override
    public ServiceTicket assignTicket(Long id, Long assigneeId, String assigneeName) {
        ServiceTicket existingTicket = serviceTicketRepository.findById(id).orElse(null);
        if (existingTicket != null) {
            existingTicket.setAssigneeId(assigneeId);
            existingTicket.setAssigneeName(assigneeName);
            existingTicket.setStatus("ASSIGNED");
            return serviceTicketRepository.save(existingTicket);
        }
        return null;
    }

    /**
     * 回复工单，将回复内容以JSON数组形式追加到 replies 字段，状态置为 REPLIED
     * @param id 工单ID
     * @param content 回复内容
     * @param replyBy 回复人
     * @return 回复后的工单，工单不存在时返回 null
     */
    @Override
    public ServiceTicket replyTicket(Long id, String content, String replyBy) {
        ServiceTicket existingTicket = serviceTicketRepository.findById(id).orElse(null);
        if (existingTicket != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> replyList = new ArrayList<>();
            try {
                // 解析已有回复记录
                if (existingTicket.getReplies() != null && !existingTicket.getReplies().isEmpty()) {
                    replyList = objectMapper.readValue(existingTicket.getReplies(),
                            new TypeReference<List<Map<String, Object>>>() {});
                }
            } catch (Exception e) {
                // 已有回复记录格式异常时重新初始化回复列表
                replyList = new ArrayList<>();
            }

            // 追加新回复（replyTime统一为yyyy-MM-dd HH:mm:ss，与全局日期格式一致，字典序即时间序）
            Map<String, Object> reply = new HashMap<>();
            reply.put("content", content);
            reply.put("replyBy", replyBy);
            reply.put("replyTime", LocalDateTime.now().format(REPLY_TIME_FORMATTER));
            replyList.add(reply);

            try {
                existingTicket.setReplies(objectMapper.writeValueAsString(replyList));
            } catch (Exception e) {
                existingTicket.setReplies("[]");
            }
            existingTicket.setStatus("REPLIED");
            return serviceTicketRepository.save(existingTicket);
        }
        return null;
    }

    /**
     * 关闭工单，将状态置为 CLOSED 并记录关闭时间
     * @param id 工单ID
     * @return 关闭后的工单，工单不存在时返回 null
     */
    @Override
    public ServiceTicket closeTicket(Long id) {
        ServiceTicket existingTicket = serviceTicketRepository.findById(id).orElse(null);
        if (existingTicket != null) {
            existingTicket.setStatus("CLOSED");
            existingTicket.setClosedAt(LocalDateTime.now());
            return serviceTicketRepository.save(existingTicket);
        }
        return null;
    }

    /**
     * 分页查询我的工单列表
     * @param assigneeName 处理人姓名
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Override
    public Page<ServiceTicket> getMyTickets(String assigneeName, String status, Pageable pageable) {
        // 处理空字符串，确保查询条件正确
        assigneeName = assigneeName != null ? assigneeName : "";
        status = status != null ? status : "";

        return serviceTicketRepository.findByAssigneeNameContainingAndStatusContaining(assigneeName, status, pageable);
    }
}
