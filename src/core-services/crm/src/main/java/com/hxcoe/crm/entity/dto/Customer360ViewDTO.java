package com.hxcoe.crm.entity.dto;

import com.hxcoe.crm.entity.CustomerContactEntity;
import com.hxcoe.crm.entity.CustomerEntity;
import com.hxcoe.crm.entity.CustomerFollowUpEntity;
import com.hxcoe.crm.entity.CustomerTransactionEntity;
import com.hxcoe.crm.entity.CustomerTagEntity;
import lombok.Data;

import java.util.List;

/**
 * 客户360°视图DTO
 */
@Data
public class Customer360ViewDTO {

    /**
     * 客户基本信息
     */
    private CustomerEntity customerInfo;

    /**
     * 客户联系人列表
     */
    private List<CustomerContactEntity> contacts;

    /**
     * 客户跟进记录列表
     */
    private List<CustomerFollowUpEntity> followUps;

    /**
     * 客户交易记录列表
     */
    private List<CustomerTransactionEntity> transactions;

    /**
     * 客户标签列表
     */
    private List<CustomerTagEntity> tags;

    /**
     * 客户等级
     */
    private String level;

    /**
     * 客户状态
     */
    private String status;

    /**
     * 客户生命周期阶段
     */
    private String lifecycleStage;

    /**
     * 客户商机列表
     */
    private List<Object> opportunities;
}
