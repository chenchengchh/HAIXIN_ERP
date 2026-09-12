package com.hxcoe.srm.service;

import com.hxcoe.srm.entity.InquiryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface InquiryService {
    InquiryEntity createInquiry(InquiryEntity inquiry);
    Page<InquiryEntity> getInquiries(Pageable pageable);
    Optional<InquiryEntity> getInquiryById(Long id);
    InquiryEntity updateInquiry(Long id, InquiryEntity inquiry);
    void deleteInquiry(Long id);
    InquiryEntity publishInquiry(Long id);
    InquiryEntity closeInquiry(Long id);
}
