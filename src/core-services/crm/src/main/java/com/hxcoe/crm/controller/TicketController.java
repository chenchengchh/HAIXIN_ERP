package com.hxcoe.crm.controller;

import com.hxcoe.crm.entity.ServiceTicket;
import com.hxcoe.crm.service.ServiceTicketService;
import com.hxcoe.common.result.PageResult;
import com.hxcoe.common.result.Result;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务工单控制器
 */
@RestController
@RequestMapping("/api/v1/crm/tickets")
public class TicketController {

    @Autowired
    private ServiceTicketService serviceTicketService;

    @Autowired
    private com.hxcoe.crm.repository.ServiceTicketRepository serviceTicketRepository;

    /**
     * 创建工单
     * @param ticket 工单数据
     * @return 创建结果
     */
    @PostMapping
    public Result<ServiceTicket> createTicket(@RequestBody ServiceTicket ticket) {
        ServiceTicket createdTicket = serviceTicketService.createTicket(ticket);
        return Result.success("成功", createdTicket);
    }

    /**
     * 查询工单详情
     * @param id 工单ID
     * @return 工单详情
     */
    @GetMapping("/{id}")
    public Result<ServiceTicket> getTicketDetail(@PathVariable Long id) {
        ServiceTicket ticket = serviceTicketService.getTicketDetail(id);
        return ticket != null ? Result.success("成功", ticket) : Result.fail("未找到");
    }

    /**
     * 更新工单
     * @param id 工单ID
     * @param ticket 工单数据
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<ServiceTicket> updateTicket(@PathVariable Long id, @RequestBody ServiceTicket ticket) {
        ServiceTicket updatedTicket = serviceTicketService.updateTicket(id, ticket);
        return updatedTicket != null ? Result.success("成功", updatedTicket) : Result.fail("未找到");
    }

    /**
     * 分配工单（状态置为 ASSIGNED）
     * @param id 工单ID
     * @param data 分配数据（assigneeId/assigneeName）
     * @return 分配结果
     */
    @PostMapping("/{id}/assign")
    public Result<ServiceTicket> assignTicket(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Object assigneeIdObj = data.get("assigneeId");
        Long assigneeId = assigneeIdObj instanceof Number ? ((Number) assigneeIdObj).longValue() : null;
        String assigneeName = data.get("assigneeName") != null ? data.get("assigneeName").toString() : null;

        ServiceTicket assignedTicket = serviceTicketService.assignTicket(id, assigneeId, assigneeName);
        return assignedTicket != null ? Result.success("成功", assignedTicket) : Result.fail("未找到");
    }

    /**
     * 回复工单（回复记录追加到 replies，状态置为 REPLIED）
     * @param id 工单ID
     * @param data 回复数据（content/replyBy）
     * @return 回复结果
     */
    @PostMapping("/{id}/reply")
    public Result<ServiceTicket> replyTicket(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        String content = data.get("content") != null ? data.get("content").toString() : null;
        String replyBy = data.get("replyBy") != null ? data.get("replyBy").toString() : null;

        ServiceTicket repliedTicket = serviceTicketService.replyTicket(id, content, replyBy);
        return repliedTicket != null ? Result.success("成功", repliedTicket) : Result.fail("未找到");
    }

    /**
     * 关闭工单（状态置为 CLOSED，记录关闭时间）
     * @param id 工单ID
     * @return 关闭结果
     */
    @PostMapping("/{id}/close")
    public Result<ServiceTicket> closeTicket(@PathVariable Long id) {
        ServiceTicket closedTicket = serviceTicketService.closeTicket(id);
        return closedTicket != null ? Result.success("成功", closedTicket) : Result.fail("未找到");
    }

    /**
     * 汇总查询所有工单的回复记录（解析各工单replies JSON，内存分页）
     * 回复结构：{ticketId, ticketNo, ticketTitle, content, replyBy, replyTime}
     *
     * @param page     页码，默认1
     * @param size     每页大小，默认10
     * @param keyword  回复内容关键词（可选）
     * @param ticketId 工单ID过滤（可选）
     * @return 回复分页数据
     */
    @GetMapping("/replies")
    public Result<PageResult<Map<String, Object>>> getAllReplies(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long ticketId) {

        // 查询有回复的工单并解析replies JSON，汇总为扁平回复列表
        List<ServiceTicket> tickets = serviceTicketRepository.findAll();
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> allReplies = new ArrayList<>();

        for (ServiceTicket ticket : tickets) {
            if (ticketId != null && !ticketId.equals(ticket.getId())) {
                continue;
            }
            String repliesJson = ticket.getReplies();
            if (repliesJson == null || repliesJson.isEmpty()) {
                continue;
            }
            try {
                List<Map<String, Object>> replyList = objectMapper.readValue(repliesJson,
                        new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> reply : replyList) {
                    String content = reply.get("content") != null ? reply.get("content").toString() : "";
                    // 内容关键词过滤
                    if (keyword != null && !keyword.isEmpty() && !content.contains(keyword)) {
                        continue;
                    }
                    Map<String, Object> flat = new HashMap<>();
                    flat.put("ticketId", ticket.getId());
                    flat.put("ticketNo", ticket.getTicketNo());
                    flat.put("ticketTitle", ticket.getTitle());
                    flat.put("content", content);
                    flat.put("replyBy", reply.get("replyBy"));
                    flat.put("replyTime", reply.get("replyTime"));
                    allReplies.add(flat);
                }
            } catch (Exception e) {
                // 单条工单回复格式异常时跳过，不影响整体列表
            }
        }

        // 按回复时间倒序排列（replyTime为ISO字符串，字典序即时间序）
        allReplies.sort(Comparator.comparing(
                r -> r.get("replyTime") != null ? r.get("replyTime").toString() : "",
                Comparator.reverseOrder()));

        // 内存分页
        int total = allReplies.size();
        int safeSize = Math.max(size, 1);
        int fromIndex = Math.min(Math.max(page - 1, 0) * safeSize, total);
        int toIndex = Math.min(fromIndex + safeSize, total);
        List<Map<String, Object>> pageContent = allReplies.subList(fromIndex, toIndex);

        return Result.success(PageResult.build((long) total, safeSize, page, pageContent));
    }

    /**
     * 获取我的工单列表
     * @param page 页码
     * @param size 每页数量
     * @param assigneeName 处理人姓名
     * @param status 状态
     * @return 分页结果
     */
    @GetMapping("/my")
    public Result<PageResult<ServiceTicket>> getMyTickets(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String assigneeName,
            @RequestParam(required = false) String status) {

        // 转换页码，PageRequest从0开始
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<ServiceTicket> ticketPage = serviceTicketService.getMyTickets(assigneeName, status, pageable);
        PageResult<ServiceTicket> pageResult = PageResult.build(
                ticketPage.getTotalElements(),
                ticketPage.getSize(),
                ticketPage.getNumber() + 1,
                ticketPage.getContent()
        );
        return Result.success(pageResult);
    }
}
