package com.hxcoe.crm.service.impl;

import com.hxcoe.crm.entity.Lead;
import com.hxcoe.crm.repository.LeadRepository;
import com.hxcoe.crm.service.LeadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 销售线索服务实现类
 */
@Service
public class LeadServiceImpl implements LeadService {
    
    @Autowired
    private LeadRepository leadRepository;

    @Override
    public Page<Lead> getLeadsList(String leadName, String status, String rating, String ownerName, Pageable pageable) {
        // 处理空字符串，确保查询条件正确
        leadName = leadName != null ? leadName : "";
        status = status != null ? status : "";
        rating = rating != null ? rating : "";
        ownerName = ownerName != null ? ownerName : "";
        
        return leadRepository.findByLeadNameContainingAndStatusContainingAndRatingContainingAndOwnerNameContaining(
                leadName, status, rating, ownerName, pageable);
    }

    @Override
    public Lead createLead(Lead lead) {
        return leadRepository.save(lead);
    }

    @Override
    public Lead getLeadDetail(Long id) {
        return leadRepository.findById(id).orElse(null);
    }

    @Override
    public Lead updateLead(Long id, Lead lead) {
        Lead existingLead = leadRepository.findById(id).orElse(null);
        if (existingLead != null) {
            // 更新线索信息
            existingLead.setLeadName(lead.getLeadName());
            existingLead.setCompanyName(lead.getCompanyName());
            existingLead.setContactName(lead.getContactName());
            existingLead.setPhone(lead.getPhone());
            existingLead.setEmail(lead.getEmail());
            existingLead.setSource(lead.getSource());
            existingLead.setIndustry(lead.getIndustry());
            existingLead.setIntent(lead.getIntent());
            existingLead.setRating(lead.getRating());
            existingLead.setStatus(lead.getStatus());
            existingLead.setOwnerId(lead.getOwnerId());
            existingLead.setOwnerName(lead.getOwnerName());
            existingLead.setConvertTime(lead.getConvertTime());
            
            return leadRepository.save(existingLead);
        }
        return null;
    }

    @Override
    public String convertLead(Long id, Object data) {
        // 这里可以实现线索转化为客户的逻辑
        return "线索转化成功";
    }
}
