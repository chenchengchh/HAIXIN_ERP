package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 销售线索服务接口
 */
public interface LeadService {
    
    /**
     * 获取线索列表
     * @param leadName 线索名称
     * @param status 状态
     * @param rating 评级
     * @param ownerName 负责人姓名
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Lead> getLeadsList(String leadName, String status, String rating, String ownerName, Pageable pageable);
    
    /**
     * 创建销售线索
     * @param lead 线索数据
     * @return 创建结果
     */
    Lead createLead(Lead lead);
    
    /**
     * 查询线索详情
     * @param id 线索ID
     * @return 线索详情
     */
    Lead getLeadDetail(Long id);
    
    /**
     * 更新线索
     * @param id 线索ID
     * @param lead 线索数据
     * @return 更新结果
     */
    Lead updateLead(Long id, Lead lead);
    
    /**
     * 将线索转化为客户
     * @param id 线索ID
     * @param data 转化数据
     * @return 转化结果
     */
    String convertLead(Long id, Object data);
}
