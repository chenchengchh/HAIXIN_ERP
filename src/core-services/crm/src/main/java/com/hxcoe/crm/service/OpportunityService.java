package com.hxcoe.crm.service;

import com.hxcoe.crm.entity.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 商机服务接口
 */
public interface OpportunityService {
    
    /**
     * 获取商机列表
     * @param opportunityName 商机名称
     * @param customerName 客户名称
     * @param stage 阶段
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Opportunity> getOpportunitiesList(String opportunityName, String customerName, String stage, String status, Pageable pageable);
    
    /**
     * 创建商机
     * @param opportunity 商机数据
     * @return 创建结果
     */
    Opportunity createOpportunity(Opportunity opportunity);
    
    /**
     * 查询商机详情
     * @param id 商机ID
     * @return 商机详情
     */
    Opportunity getOpportunityDetail(Long id);
    
    /**
     * 更新商机
     * @param id 商机ID
     * @param opportunity 商机数据
     * @return 更新结果
     */
    Opportunity updateOpportunity(Long id, Opportunity opportunity);
    
    /**
     * 推进商机阶段
     * @param id 商机ID
     * @param data 阶段数据
     * @return 推进结果
     */
    String updateOpportunityStage(Long id, Object data);
    
    /**
     * 获取我的商机列表
     * @param ownerId 负责人ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<Opportunity> getMyOpportunities(Long ownerId, Pageable pageable);

    /**
     * 获取销售漏斗分析（按阶段分组统计商机数量、预计金额及阶段转化率，仅统计未删除数据）
     * @return 销售漏斗数据列表，每项包含 stage/stageName/count/amount/conversionRate
     */
    List<Map<String, Object>> getSalesFunnel();
}
