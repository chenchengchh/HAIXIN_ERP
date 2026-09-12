package com.hxcoe.crm.repository;

import com.hxcoe.crm.entity.ServiceTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 服务工单数据访问接口
 */
@Repository
public interface ServiceTicketRepository extends JpaRepository<ServiceTicket, Long> {

    /**
     * 根据处理人姓名和状态分页查询工单列表
     * @param assigneeName 处理人姓名（模糊匹配）
     * @param status 状态（模糊匹配）
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<ServiceTicket> findByAssigneeNameContainingAndStatusContaining(String assigneeName, String status, Pageable pageable);
}
