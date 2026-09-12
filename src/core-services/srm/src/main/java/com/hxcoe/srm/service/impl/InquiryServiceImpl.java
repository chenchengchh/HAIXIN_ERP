package com.hxcoe.srm.service.impl;

import com.hxcoe.srm.entity.InquiryEntity;
import com.hxcoe.srm.entity.InquiryItemEntity;
import com.hxcoe.srm.repository.InquiryRepository;
import com.hxcoe.srm.service.InquiryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InquiryServiceImpl implements InquiryService {

    @Autowired
    private InquiryRepository inquiryRepository;

    @Transactional
    @Override
    public InquiryEntity createInquiry(InquiryEntity inquiry) {
        if (inquiry.getItems() != null) {
            for (InquiryItemEntity item : inquiry.getItems()) {
                item.setInquiry(inquiry);
            }
        }
        inquiry.setStatus("DRAFT");
        return inquiryRepository.save(inquiry);
    }

    @Override
    public Page<InquiryEntity> getInquiries(Pageable pageable) {
        return inquiryRepository.findAll(pageable);
    }

    @Override
    public Optional<InquiryEntity> getInquiryById(Long id) {
        return inquiryRepository.findById(id);
    }

    @Transactional
    @Override
    public InquiryEntity updateInquiry(Long id, InquiryEntity inquiry) {
        return inquiryRepository.findById(id).map(existing -> {
            existing.setTitle(inquiry.getTitle());
            existing.setDeadline(inquiry.getDeadline());
            // 简单处理：如果状态允许，可以更新其他字段
            return inquiryRepository.save(existing);
        }).orElse(null);
    }

    @Override
    public void deleteInquiry(Long id) {
        inquiryRepository.deleteById(id);
    }

    @Override
    public InquiryEntity publishInquiry(Long id) {
        return inquiryRepository.findById(id).map(inquiry -> {
            inquiry.setStatus("PUBLISHED");
            return inquiryRepository.save(inquiry);
        }).orElse(null);
    }

    @Override
    public InquiryEntity closeInquiry(Long id) {
        return inquiryRepository.findById(id).map(inquiry -> {
            inquiry.setStatus("CLOSED");
            return inquiryRepository.save(inquiry);
        }).orElse(null);
    }
}
