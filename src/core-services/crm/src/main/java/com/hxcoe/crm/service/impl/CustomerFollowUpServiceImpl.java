package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.CustomerEntity;
import com.hxcoe.crm.entity.CustomerFollowUpEntity;
import com.hxcoe.crm.repository.CustomerFollowUpRepository;
import com.hxcoe.crm.repository.CustomerRepository;
import com.hxcoe.crm.service.CustomerFollowUpService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 客户跟进记录服务实现类
 */
@Service
public class CustomerFollowUpServiceImpl implements CustomerFollowUpService {

    @Autowired
    private CustomerFollowUpRepository followUpRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public CustomerFollowUpEntity createFollowUp(CustomerFollowUpEntity followUpEntity) {
        // 设置默认值
        if (followUpEntity.getFollowUpTime() == null) {
            followUpEntity.setFollowUpTime(LocalDateTime.now());
        }
        return followUpRepository.save(followUpEntity);
    }

    @Override
    public CustomerFollowUpEntity getFollowUpById(Long id) {
        return followUpRepository.findById(id).orElse(null);
    }

    @Override
    public CustomerFollowUpEntity updateFollowUp(CustomerFollowUpEntity followUpEntity) {
        return followUpRepository.save(followUpEntity);
    }

    @Override
    public boolean deleteFollowUp(Long id) {
        CustomerFollowUpEntity followUp = followUpRepository.findById(id).orElse(null);
        if (followUp != null) {
            followUpRepository.delete(followUp);
            return true;
        }
        return false;
    }

    @Override
    public List<CustomerFollowUpEntity> getFollowUpsByCustomerId(Long customerId) {
        return followUpRepository.findByCustomerIdOrderByFollowUpTimeDesc(customerId);
    }

    @Override
    public List<CustomerFollowUpEntity> getFollowUpsByFollowUpUserId(Long followUpUserId) {
        return followUpRepository.findByFollowUpUserIdOrderByFollowUpTimeDesc(followUpUserId);
    }

    @Override
    public List<CustomerFollowUpEntity> getMyPendingFollowUps(Long followUpUserId) {
        // 查询我的待跟进客户（目前简单实现，实际应该根据下次跟进时间过滤）
        return followUpRepository.findByFollowUpUserIdOrderByFollowUpTimeDesc(followUpUserId);
    }

    /**
     * 全量分页查询跟进记录，按id倒序
     * 客户名称筛选：先按名称模糊查询匹配的客户ID集合，再以 customerId IN 作为分页条件，保证分页总数正确；
     * 查询完成后批量查询客户信息并填充 customerName 字段
     *
     * @param customerName 客户名称（可选，模糊匹配）
     * @param followUpType 跟进方式（可选）
     * @param startTime    跟进时间起（可选）
     * @param endTime      跟进时间止（可选）
     * @param pageable     分页参数
     * @return 跟进记录分页结果
     */
    @Override
    public Page<CustomerFollowUpEntity> getFollowUpList(String customerName, String followUpType,
                                                        LocalDateTime startTime, LocalDateTime endTime,
                                                        Pageable pageable) {
        // 客户名称模糊筛选：先查出匹配的客户ID集合
        List<Long> matchedCustomerIds = null;
        if (customerName != null && !customerName.isBlank()) {
            matchedCustomerIds = customerRepository.findByCustomerNameContaining(customerName)
                    .stream().map(CustomerEntity::getId).collect(Collectors.toList());
            // 无匹配客户时直接返回空分页
            if (matchedCustomerIds.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        // 动态拼接查询条件
        final List<Long> customerIds = matchedCustomerIds;
        Specification<CustomerFollowUpEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (customerIds != null) {
                predicates.add(root.get("customerId").in(customerIds));
            }
            if (followUpType != null && !followUpType.isBlank()) {
                predicates.add(cb.equal(root.get("followUpType"), followUpType));
            }
            if (startTime != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("followUpTime"), startTime));
            }
            if (endTime != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("followUpTime"), endTime));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CustomerFollowUpEntity> followUpPage = followUpRepository.findAll(spec, pageable);

        // 批量查询客户信息，填充客户名称
        List<Long> pageCustomerIds = followUpPage.getContent().stream()
                .map(CustomerFollowUpEntity::getCustomerId)
                .distinct()
                .collect(Collectors.toList());
        if (!pageCustomerIds.isEmpty()) {
            Map<Long, CustomerEntity> customerMap = customerRepository.findAllById(pageCustomerIds).stream()
                    .collect(Collectors.toMap(CustomerEntity::getId, Function.identity()));
            followUpPage.getContent().forEach(followUp -> {
                CustomerEntity customer = customerMap.get(followUp.getCustomerId());
                if (customer != null) {
                    followUp.setCustomerName(customer.getCustomerName());
                }
            });
        }
        return followUpPage;
    }
}