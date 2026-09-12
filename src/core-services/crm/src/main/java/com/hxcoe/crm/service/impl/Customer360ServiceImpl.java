package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.CustomerContactEntity;
import com.hxcoe.crm.entity.CustomerEntity;
import com.hxcoe.crm.entity.CustomerFollowUpEntity;
import com.hxcoe.crm.entity.CustomerTransactionEntity;
import com.hxcoe.crm.entity.CustomerTagEntity;
import com.hxcoe.crm.entity.dto.Customer360ViewDTO;
import com.hxcoe.crm.repository.*;
import com.hxcoe.crm.service.Customer360Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 客户360°视图服务实现类
 */
@Slf4j
@Service
public class Customer360ServiceImpl implements Customer360Service {

    private final Executor executor = Executors.newFixedThreadPool(4);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerContactRepository contactRepository;

    @Autowired
    private CustomerFollowUpRepository followUpRepository;

    @Autowired
    private CustomerTransactionRepository transactionRepository;

    @Autowired
    private CustomerTagRepository tagRepository;

    @Autowired
    private CustomerTagRelationRepository tagRelationRepository;

    @Override
    public Customer360ViewDTO getCustomer360View(Long customerId) {
        log.info("获取客户360°视图，客户ID：{}", customerId);

        try {
            // 获取客户基本信息
            CustomerEntity customer = customerRepository.findById(customerId).orElse(null);
            if (customer == null) {
                log.warn("客户不存在，客户ID：{}", customerId);
                return null;
            }
            log.info("获取到客户基本信息，客户名称：{}", customer.getCustomerName());

            // 并行获取关联数据，提高性能
            CompletableFuture<List<CustomerContactEntity>> contactsFuture = CompletableFuture.supplyAsync(
                    () -> contactRepository.findByCustomerId(customerId), executor);

            CompletableFuture<List<CustomerFollowUpEntity>> followUpsFuture = CompletableFuture.supplyAsync(
                    () -> followUpRepository.findByCustomerIdOrderByFollowUpTimeDesc(customerId), executor);

            CompletableFuture<List<CustomerTransactionEntity>> transactionsFuture = CompletableFuture.supplyAsync(
                    () -> transactionRepository.findByCustomerIdOrderByDealDateDesc(customerId), executor);

            CompletableFuture<List<CustomerTagEntity>> tagsFuture = CompletableFuture.supplyAsync(() -> {
                List<Long> tagIds = tagRelationRepository.findByCustomerId(customerId)
                        .stream()
                        .map(tagRelation -> tagRelation.getTagId())
                        .collect(Collectors.toList());
                if (tagIds.isEmpty()) {
                    return new ArrayList<>();
                }
                return tagRepository.findAllById(tagIds);
            }, executor);

            // 等待所有查询完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    contactsFuture, followUpsFuture, transactionsFuture, tagsFuture);
            allFutures.join();

            List<CustomerContactEntity> contacts = contactsFuture.join();
            List<CustomerFollowUpEntity> followUps = followUpsFuture.join();
            List<CustomerTransactionEntity> transactions = transactionsFuture.join();
            List<CustomerTagEntity> tags = tagsFuture.join();

            log.info("并行查询完成 - 联系人:{}, 跟进记录:{}, 交易记录:{}, 标签:{}",
                    contacts.size(), followUps.size(), transactions.size(), tags.size());

            // 组装360°视图数据
            Customer360ViewDTO viewDTO = new Customer360ViewDTO();
            viewDTO.setCustomerInfo(customer);
            viewDTO.setContacts(contacts);
            viewDTO.setFollowUps(followUps);
            viewDTO.setTransactions(transactions);
            viewDTO.setTags(tags);
            viewDTO.setLevel(customer.getLevel());
            viewDTO.setStatus(customer.getStatus());
            
            // 目前商机功能尚未实现，返回空列表
            viewDTO.setOpportunities(new ArrayList<>());

            // 设置客户生命周期阶段
            String lifecycleStage = getLifecycleStage(customer, transactions.size());
            viewDTO.setLifecycleStage(lifecycleStage);
            log.info("客户生命周期阶段：{}", lifecycleStage);

            log.info("客户360°视图数据组装完成，客户ID：{}", customerId);
            return viewDTO;
        } catch (Exception e) {
            log.error("获取客户360°视图失败，客户ID：{}", customerId, e);
            throw new RuntimeException("获取客户360°视图失败", e);
        }
    }

    /**
     * 根据客户状态和交易次数判断客户生命周期阶段
     *
     * @param customer     客户实体
     * @param transactionCount 交易次数
     * @return 生命周期阶段
     */
    private String getLifecycleStage(CustomerEntity customer, int transactionCount) {
        String status = customer.getStatus();
        switch (status) {
            case "potential":
                return "潜在客户";
            case "active":
                if (transactionCount == 0) {
                    return "新客户";
                } else if (transactionCount < 5) {
                    return "成长客户";
                } else {
                    return "成熟客户";
                }
            case "inactive":
                return "沉睡客户";
            case "lost":
                return "流失客户";
            default:
                return "未知";
        }
    }
}
